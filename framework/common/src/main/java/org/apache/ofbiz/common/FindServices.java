/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.apache.ofbiz.common;

import static org.apache.ofbiz.base.util.UtilGenerics.checkList;
import static org.apache.ofbiz.base.util.UtilGenerics.checkMap;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.ObjectType;
import org.apache.ofbiz.base.util.StringUtil;
import org.apache.ofbiz.base.util.UtilDateTime;
import org.apache.ofbiz.base.util.UtilGenerics;
import org.apache.ofbiz.base.util.UtilHttp;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntity;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.condition.EntityComparisonOperator;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.condition.EntityConditionList;
import org.apache.ofbiz.entity.condition.EntityFunction;
import org.apache.ofbiz.entity.condition.EntityOperator;
import org.apache.ofbiz.entity.datasource.GenericHelperInfo;
import org.apache.ofbiz.entity.jdbc.SQLProcessor;
import org.apache.ofbiz.entity.model.ModelEntity;
import org.apache.ofbiz.entity.model.ModelField;
import org.apache.ofbiz.entity.util.EntityListIterator;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.entity.util.EntityUtil;
import org.apache.ofbiz.entity.util.EntityUtilProperties;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

/**
 * FindServices Class
 */
public class FindServices {

    public static final String module = FindServices.class.getName();
    public static final String resource = "CommonUiLabels";
    public static Map<String, EntityComparisonOperator<?, ?>> entityOperators;

    static {
        entityOperators =  new LinkedHashMap<String, EntityComparisonOperator<?, ?>>();
        entityOperators.put("between", EntityOperator.BETWEEN);
        entityOperators.put("equals", EntityOperator.EQUALS);
        entityOperators.put("greaterThan", EntityOperator.GREATER_THAN);
        entityOperators.put("greaterThanEqualTo", EntityOperator.GREATER_THAN_EQUAL_TO);
        entityOperators.put("in", EntityOperator.IN);
        entityOperators.put("not-in", EntityOperator.NOT_IN);
        entityOperators.put("lessThan", EntityOperator.LESS_THAN);
        entityOperators.put("lessThanEqualTo", EntityOperator.LESS_THAN_EQUAL_TO);
        entityOperators.put("like", EntityOperator.LIKE);
        entityOperators.put("notLike", EntityOperator.NOT_LIKE);
        entityOperators.put("not", EntityOperator.NOT);
        entityOperators.put("notEqual", EntityOperator.NOT_EQUAL);
    }

    public FindServices() {}

    public static String listToString(List<String> array, String separator) {
        StringBuilder sb = new StringBuilder();
        for (String str : array) {
            sb.append(str);
            sb.append(separator);
        }
        String result = "";
        if (sb.toString().length() > separator.length())
            result = sb.toString().substring(0, sb.length() - separator.length());

        return result;
    }

    public static String stack2string(Exception e) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return "------\r\n" + sw.toString() + "------\r\n";
        } catch (Exception e2) {
            return "bad stack2string";
        }
    }

    /**
     * prepareField, analyse inputFields to created normalizedFields a map with field name and operator.
     *
     * This is use to the generic method that expects entity data affixed with special suffixes
     * to indicate their purpose in formulating an SQL query statement.
     * @param inputFields     Input parameters run thru UtilHttp.getParameterMap
     * @return a map with field name and operator
     */
    public static Map<String, Map<String, Map<String, Object>>> prepareField(Map<String, ?> inputFields, Map<String, Object> queryStringMap, Map<String, List<Object[]>> origValueMap) {
        Map<String, Map<String, Map<String, Object>>> normalizedFields = new LinkedHashMap<String, Map<String, Map<String, Object>>>();
        for (String fieldNameRaw: inputFields.keySet()) {
            String fieldNameRoot = null;
            String fieldPair = null;
            Object fieldValue = null;
            int iPos = -1;
            int iPos2 = -1;
            Map<String, Map<String, Object>> subMap = null;
            Map<String, Object> subMap2 = null;
            String fieldMode = null;

            fieldValue = inputFields.get(fieldNameRaw);
            if (ObjectType.isEmpty(fieldValue)) {
                continue;
            }

            queryStringMap.put(fieldNameRaw, fieldValue);
            iPos = fieldNameRaw.indexOf("_");

            if (iPos >= 0) {
                String suffix = fieldNameRaw.substring(iPos + 1);
                iPos2 = suffix.indexOf("_");
                if (iPos2 == 1) {
                    continue;
                }
            }

            if (iPos < 0) {
                fieldNameRoot = fieldNameRaw;
                fieldPair = "fld0";
                fieldMode = "value";
            } else {
                fieldNameRoot = fieldNameRaw.substring(0, iPos);
                String suffix = fieldNameRaw.substring(iPos + 1);
                iPos2 = suffix.indexOf("_");
                if (iPos2 < 0) {
                    if (suffix.startsWith("fld")) {
                        fieldPair = suffix;
                        fieldMode = "value";
                    } else {
                        fieldPair = "fld0";
                        fieldMode = suffix;
                    }
                } else {
                    String tkn0 = suffix.substring(0, iPos2);
                    String tkn1 = suffix.substring(iPos2 + 1);
                    if (tkn0.startsWith("fld")) {
                        fieldPair = tkn0;
                        fieldMode = tkn1;
                    } else {
                        fieldPair = tkn1;
                        fieldMode = tkn0;
                    }
                }
            }
            subMap = normalizedFields.get(fieldNameRoot);
            if (subMap == null) {
                subMap = new LinkedHashMap<String, Map<String, Object>>();
                normalizedFields.put(fieldNameRoot, subMap);
            }
            subMap2 = subMap.get(fieldPair);
            if (subMap2 == null) {
                subMap2 = new LinkedHashMap<String, Object>();
                subMap.put(fieldPair, subMap2);
            }
            subMap2.put(fieldMode, fieldValue);

            List<Object[]> origList = origValueMap.get(fieldNameRoot);
            if (origList == null) {
                origList = new LinkedList<Object[]>();
                origValueMap.put(fieldNameRoot, origList);
            }
            Object [] origValues = {fieldNameRaw, fieldValue};
            origList.add(origValues);
        }
        return normalizedFields;
    }

    /**
     * Parses input parameters and returns an EntityCondition list.
     */
    public static List<EntityCondition> createConditionList(Map<String, ? extends Object> parameters, List<ModelField> fieldList, Map<String, Object> queryStringMap, Delegator delegator, Map<String, ?> context) {
        Set<String> processed = new LinkedHashSet<String>();
        Set<String> keys = new LinkedHashSet<String>();
        Map<String, ModelField> fieldMap = new LinkedHashMap<String, ModelField>();
        for (ModelField modelField : fieldList) {
            fieldMap.put(modelField.getName(), modelField);
        }
        List<EntityCondition> result = new LinkedList<EntityCondition>();
        for (Map.Entry<String, ? extends Object> entry : parameters.entrySet()) {
            String parameterName = entry.getKey();
            if (processed.contains(parameterName)) {
                continue;
            }
            keys.clear();
            String fieldName = parameterName;
            Object fieldValue = null;
            String operation = null;
            boolean ignoreCase = false;
            if (parameterName.endsWith("_ic") || parameterName.endsWith("_op")) {
                fieldName = parameterName.substring(0, parameterName.length() - 3);
            } else if (parameterName.endsWith("_value")) {
                fieldName = parameterName.substring(0, parameterName.length() - 6);
            }
            String key = fieldName.concat("_ic");
            if (parameters.containsKey(key)) {
                keys.add(key);
                ignoreCase = "Y".equals(parameters.get(key));
            }
            key = fieldName.concat("_op");
            if (parameters.containsKey(key)) {
                keys.add(key);
                operation = (String) parameters.get(key);
            }
            key = fieldName.concat("_value");
            if (parameters.containsKey(key)) {
                keys.add(key);
                fieldValue = parameters.get(key);
            }
            if (fieldName.endsWith("_fld0") || fieldName.endsWith("_fld1")) {
                if (parameters.containsKey(fieldName)) {
                    keys.add(fieldName);
                }
                fieldName = fieldName.substring(0, fieldName.length() - 5);
            }
            if (parameters.containsKey(fieldName)) {
                keys.add(fieldName);
            }
            processed.addAll(keys);
            ModelField modelField = fieldMap.get(fieldName);
            if (modelField == null) {
                continue;
            }
            if (fieldValue == null) {
                fieldValue = parameters.get(fieldName);
            }
            if (ObjectType.isEmpty(fieldValue) && !"empty".equals(operation)) {
                continue;
            }
            result.add(createSingleCondition(modelField, operation, fieldValue, ignoreCase, delegator, context));
            for (String mapKey : keys) {
                queryStringMap.put(mapKey, parameters.get(mapKey));
            }
        }
        return result;
    }

    /**
     * Creates a single EntityCondition based on a set of parameters.
     */
    public static EntityCondition createSingleCondition(ModelField modelField, String operation, Object fieldValue, boolean ignoreCase, Delegator delegator, Map<String, ?> context) {
        EntityCondition cond = null;
        String fieldName = modelField.getName();
        Locale locale = (Locale) context.get("locale");
        TimeZone timeZone = (TimeZone) context.get("timeZone");
        EntityComparisonOperator<?, ?> fieldOp = null;
        if (operation != null) {
            if (operation.equals("contains")) {
                fieldOp = EntityOperator.LIKE;
                fieldValue = "%" + fieldValue + "%";
            } else if ("not-contains".equals(operation) || "notContains".equals(operation)) {
                fieldOp = EntityOperator.NOT_LIKE;
                fieldValue = "%" + fieldValue + "%";
            } else if (operation.equals("empty")) {
                return EntityCondition.makeCondition(fieldName, EntityOperator.EQUALS, null);
            } else if (operation.equals("like")) {
                fieldOp = EntityOperator.LIKE;
                fieldValue = fieldValue + "%";
            } else if ("not-like".equals(operation) || "notLike".equals(operation)) {
                fieldOp = EntityOperator.NOT_LIKE;
                fieldValue = fieldValue + "%";
            } else if ("opLessThan".equals(operation)) {
                fieldOp = EntityOperator.LESS_THAN;
            } else if ("upToDay".equals(operation)) {
                fieldOp = EntityOperator.LESS_THAN;
            } else if ("upThruDay".equals(operation)) {
                fieldOp = EntityOperator.LESS_THAN_EQUAL_TO;
            } else if (operation.equals("greaterThanFromDayStart")) {
                String timeStampString = (String) fieldValue;
                Object startValue = modelField.getModelEntity().convertFieldValue(modelField, dayStart(timeStampString, 0, timeZone, locale), delegator, context);
                return EntityCondition.makeCondition(fieldName, EntityOperator.GREATER_THAN_EQUAL_TO, startValue);
            } else if (operation.equals("sameDay")) {
                String timeStampString = (String) fieldValue;
                Object startValue = modelField.getModelEntity().convertFieldValue(modelField, dayStart(timeStampString, 0, timeZone, locale), delegator, context);
                EntityCondition startCond = EntityCondition.makeCondition(fieldName, EntityOperator.GREATER_THAN_EQUAL_TO, startValue);
                Object endValue = modelField.getModelEntity().convertFieldValue(modelField, dayStart(timeStampString, 1, timeZone, locale), delegator, context);
                EntityCondition endCond = EntityCondition.makeCondition(fieldName, EntityOperator.LESS_THAN, endValue);
                return EntityCondition.makeCondition(startCond, endCond);
            } else {
                fieldOp = entityOperators.get(operation);
            }
        } else {
            if (UtilValidate.isNotEmpty(UtilGenerics.toList(fieldValue))) {
                fieldOp = EntityOperator.IN;
            } else {
                fieldOp = EntityOperator.EQUALS;
            }
        }
        Object fieldObject = fieldValue;
        if ((fieldOp != EntityOperator.IN && fieldOp != EntityOperator.NOT_IN ) || !(fieldValue instanceof Collection<?>)) {
            fieldObject = modelField.getModelEntity().convertFieldValue(modelField, fieldValue, delegator, context);
        }
        if (ignoreCase && fieldObject instanceof String) {
            cond = EntityCondition.makeCondition(EntityFunction.UPPER_FIELD(fieldName), fieldOp, EntityFunction.UPPER(((String)fieldValue).toUpperCase()));
        } else {
            if (fieldObject.equals(GenericEntity.NULL_FIELD.toString())) {
                fieldObject = null;
            }
            cond = EntityCondition.makeCondition(fieldName, fieldOp, fieldObject);
        }
        if (EntityOperator.NOT_EQUAL.equals(fieldOp) && fieldObject != null) {
            cond = EntityCondition.makeCondition(UtilMisc.toList(cond, EntityCondition.makeCondition(fieldName, null)), EntityOperator.OR);
        }
        return cond;
    }

    /**
     * createCondition, comparing the normalizedFields with the list of keys.
     */
    public static List<EntityCondition> createCondition(ModelEntity modelEntity, Map<String, Map<String, Map<String, Object>>> normalizedFields, Map<String, Object> queryStringMap, Map<String, List<Object[]>> origValueMap, Delegator delegator, Map<String, ?> context) {
        Map<String, Map<String, Object>> subMap = null;
        Map<String, Object> subMap2 = null;
        Object fieldValue = null;
        EntityCondition cond = null;
        List<EntityCondition> tmpList = new LinkedList<EntityCondition>();
        String opString = null;
        boolean ignoreCase = false;
        List<ModelField> fields = modelEntity.getFieldsUnmodifiable();
        for (ModelField modelField: fields) {
            String fieldName = modelField.getName();
            subMap = normalizedFields.get(fieldName);
            if (subMap == null) {
                continue;
            }
            subMap2 = subMap.get("fld0");
            fieldValue = subMap2.get("value");
            opString = (String) subMap2.get("op");
            if (fieldValue == null && !"empty".equals(opString)) {
                continue;
            }
            ignoreCase = "Y".equals(subMap2.get("ic"));
            cond = createSingleCondition(modelField, opString, fieldValue, ignoreCase, delegator, context);
            tmpList.add(cond);
            subMap2 = subMap.get("fld1");
            if (subMap2 == null) {
                continue;
            }
            fieldValue = subMap2.get("value");
            opString = (String) subMap2.get("op");
            if (fieldValue == null && !"empty".equals(opString)) {
                continue;
            }
            ignoreCase = "Y".equals(subMap2.get("ic"));
            cond = createSingleCondition(modelField, opString, fieldValue, ignoreCase, delegator, context);
            tmpList.add(cond);
            List<Object[]> origList = origValueMap.get(fieldName);
            if (UtilValidate.isNotEmpty(origList)) {
                for (Object[] arr: origList) {
                    queryStringMap.put((String) arr[0], (Object) arr[1]);
                }
            }
        }
        return tmpList;
    }

    /**
     * performFindList: same as performFind but returning a list instead of an iterator.
     */
    public static Map<String, Object> performFindList(DispatchContext dctx, Map<String, Object> context) {
        Integer viewSize = (Integer) context.get("viewSize");
        if (viewSize == null) viewSize = Integer.valueOf(20);
        context.put("viewSize", viewSize);
        Integer viewIndex = (Integer) context.get("viewIndex");
        if (viewIndex == null)  viewIndex = Integer.valueOf(0);
        context.put("viewIndex", viewIndex);

        Map<String, Object> result = performFind(dctx,context);

        int start = viewIndex.intValue() * viewSize.intValue();
        List<GenericValue> list = null;
        Integer listSize = 0;
        try {
            EntityListIterator it = (EntityListIterator) result.get("listIt");
            list = it.getPartialList(start+1, viewSize);
            listSize = it.getResultsSizeAfterPartialList();
            it.close();
        } catch (Exception e) {
            Debug.logInfo("Problem getting partial list" + e,module);
        }

        result.put("listSize", listSize);
        result.put("list",list);
        result.remove("listIt");
        return result;
    }

    /**
     * performFind
     */
    public static Map<String, Object> performFind(DispatchContext dctx, Map<String, ?> context) {
        String entityName = (String) context.get("entityName");
        String orderBy = (String) context.get("orderBy");
        Map<String, ?> inputFields = checkMap(context.get("inputFields"), String.class, Object.class);
        String noConditionFind = (String) context.get("noConditionFind");
        String distinct = (String) context.get("distinct");
        List<String> fieldList =  UtilGenerics.<String>checkList(context.get("fieldList"));
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");
        Delegator delegator = dctx.getDelegator();
        if (UtilValidate.isEmpty(noConditionFind)) {
            noConditionFind = (String) inputFields.get("noConditionFind");
        }
        if (UtilValidate.isEmpty(noConditionFind)) {
            noConditionFind = EntityUtilProperties.getPropertyValue("widget", "widget.defaultNoConditionFind", delegator);
        }
        String filterByDate = (String) context.get("filterByDate");
        if (UtilValidate.isEmpty(filterByDate)) {
            filterByDate = (String) inputFields.get("filterByDate");
        }
        Timestamp filterByDateValue = (Timestamp) context.get("filterByDateValue");
        String fromDateName = (String) context.get("fromDateName");
        if (UtilValidate.isEmpty(fromDateName)) {
            fromDateName = (String) inputFields.get("fromDateName");
        }
        String thruDateName = (String) context.get("thruDateName");
        if (UtilValidate.isEmpty(thruDateName)) {
            thruDateName = (String) inputFields.get("thruDateName");
        }

        Integer viewSize = (Integer) context.get("viewSize");
        Integer viewIndex = (Integer) context.get("viewIndex");
        Integer maxRows = null;
        if (viewSize != null && viewIndex != null) {
            maxRows = viewSize * (viewIndex + 1);
        }

        LocalDispatcher dispatcher = dctx.getDispatcher();

        Map<String, Object> prepareResult = null;
        try {
            prepareResult = dispatcher.runSync("prepareFind", UtilMisc.toMap("entityName", entityName, "orderBy", orderBy,
                    "inputFields", inputFields, "filterByDate", filterByDate, "noConditionFind", noConditionFind,
                    "filterByDateValue", filterByDateValue, "userLogin", userLogin, "fromDateName", fromDateName, "thruDateName", thruDateName,
                    "locale", context.get("locale"), "timeZone", context.get("timeZone")));
        } catch (GenericServiceException gse) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource, "CommonFindErrorPreparingConditions", UtilMisc.toMap("errorString", gse.getMessage()), locale));
        }
        EntityConditionList<EntityCondition> exprList = UtilGenerics.cast(prepareResult.get("entityConditionList"));
        List<String> orderByList = checkList(prepareResult.get("orderByList"), String.class);
        Map<String, Object> executeResult = null;
        try {
            executeResult = dispatcher.runSync("executeFind", UtilMisc.toMap("entityName", entityName, "orderByList", orderByList,
                    "fieldList", fieldList, "entityConditionList", exprList,
                    "noConditionFind", noConditionFind, "distinct", distinct,
                    "locale", context.get("locale"), "timeZone", context.get("timeZone"),
                    "maxRows", maxRows));
        } catch (GenericServiceException gse) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource, "CommonFindErrorRetrieveIterator", UtilMisc.toMap("errorString", gse.getMessage()), locale));
        }
        if (executeResult.get("listIt") == null) {
            if (Debug.verboseOn()) Debug.logVerbose("No list iterator found for query string + [" + prepareResult.get("queryString") + "]", module);
        }
        Map<String, Object> results = ServiceUtil.returnSuccess();
        results.put("listIt", executeResult.get("listIt"));
        results.put("listSize", executeResult.get("listSize"));
        results.put("queryString", prepareResult.get("queryString"));
        results.put("queryStringMap", prepareResult.get("queryStringMap"));
        return results;
    }

    /** getUserLoginId get user login for the context */
    public static String getUserLoginId(Map<String, ?> context){
        String userLoginId = "";
        try{
            GenericValue gv = (GenericValue) context.get("userLogin");
            Map<String, Object> rmap = gv.getAllFields();
            userLoginId = (String) rmap.get("userLoginId");
        }catch (Exception e){
            Debug.logError("ERROR FindServices.getUserLoginId:" + e.getMessage(), module);
        }
        return userLoginId;
    }

    /** getUserGroups get group for user login */
    public static Set<String> getUserGroups(DispatchContext dctx, String userLoginId, boolean fromDispatch, boolean lUseCache){
        Boolean lLog = false;
        Set<String> userGroupsList = new LinkedHashSet<String>();

        if (fromDispatch){
            @SuppressWarnings({ "deprecation", "rawtypes" })
            Iterator iter = dctx.getSecurity().findUserLoginSecurityGroupByUserLoginId(userLoginId);
            try{
                if (iter != null) {
                    while(iter.hasNext()) {
                        GenericValue element = (GenericValue) iter.next();
                        String groupId = (String) element.get("groupId");
                        userGroupsList.add(groupId);
                    }
                }
            }catch (Exception e){
                Debug.logError("------------------Exception"+e.getMessage(), module);
                userGroupsList.clear();
            }
        }

        if (!fromDispatch || userGroupsList.isEmpty()){
            try {
                Delegator delegator = dctx.getDelegator();
                List<GenericValue> groups = null;
                List<EntityCondition> conds = new ArrayList<>();
                conds.add(EntityCondition.makeCondition("userLoginId", userLoginId));
                EntityCondition whereCond = EntityCondition.makeCondition(conds, EntityOperator.AND);
                groups = delegator.findList("UserLoginSecurityGroup", whereCond, null, null, null, lUseCache);
                if (groups != null && !groups.isEmpty()) {
                    for (GenericValue group : groups) {
                        String groupId = (String) group.get("groupId");
                        userGroupsList.add(groupId);
                    }
                }
            } catch (GenericEntityException e) {
                Debug.logError("ERROR FindServices.getUserGroups:" + e.getMessage(), module);
            }
        }
        if (lLog){
            for (String s : userGroupsList) {
                Debug.logInfo("================ group for user "+ userLoginId +": " + s, module);
            }
        }
        return userGroupsList;
    }

    /** getComponentName get plugin name from passed inputFields */
    public static String getComponentName(Map<String, ?> inputFields){
        String componentName = "";
        try{
            for (Entry<String, ?> entry : inputFields.entrySet()) {
                if (entry.getKey().equalsIgnoreCase("componentName")){
                    componentName = (String) entry.getValue();
                }
            }
        }catch (Exception e){
            Debug.logError("ERROR FindServices.getUserLoginId:" + e.getMessage(), module);
        }
        return componentName;
    }

    /** Helper: true si el modelo de entidad existe en el delegator */
    private static boolean modelExists(Delegator delegator, String entityName) {
        try {
            delegator.getModelEntity(entityName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** getDefaultCondition get condition to be overlapped or created for user, plugin and entity from table entity_condition */
    public static Map<String,Map<String,String>> getDefaultCondition( String userLoginId, String componentName, String entityName, Delegator delegator, Boolean lUseCache){
        Boolean lLog = false;
        Map<String,Map<String,String>> conditionsMap = new HashMap<String,Map<String,String>>();
        if (lLog){
            Debug.logInfo("------------------------------ Searching userLoginId: "+ userLoginId + " componentName: "+ componentName + " entityName:" + entityName, module);
        }

        // Admin has the power
        if (!userLoginId.equalsIgnoreCase("admin")){
            // Si la entidad no existe, no aplicamos condiciones por defecto
            if (!modelExists(delegator, "entityCondition")) {
                if (lLog) {
                    Debug.logInfo("getDefaultCondition: entityCondition model not found; skipping.", module);
                }
                return conditionsMap;
            }
            try{
                List<GenericValue> resultEntityList = null;
                List<EntityCondition> conds = new ArrayList<>();
                conds.add(EntityCondition.makeCondition("cndPlugin", componentName));
                conds.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("cndUser"), EntityOperator.EQUALS, userLoginId.toUpperCase()));
                conds.add(EntityCondition.makeCondition("cndEntity", entityName));
                conds.add(EntityCondition.makeCondition("cndDll", "SELECT"));
                EntityCondition whereCond = EntityCondition.makeCondition(conds, EntityOperator.AND);

                try{
                    resultEntityList = delegator.findList("entityCondition", whereCond, null, null, null, lUseCache);
                } catch (GenericEntityException ex) {
                    resultEntityList = null;
                    Debug.logError("getDefaultCondition: "+ ex.getMessage() + " Stack:" + FindServices.stack2string(ex), module);
                } catch (Exception ex) { // p.ej. GenericModelException si no existe la entidad
                    resultEntityList = null;
                    Debug.logInfo("getDefaultCondition: entityCondition not defined; skipping.", module);
                }

                if (resultEntityList != null && !resultEntityList.isEmpty()) {
                    int i = 0;
                    for (GenericValue resultEntity : resultEntityList) {
                        i = i + 1;
                        String cndField   = (String) resultEntity.get("cndField");
                        String cndValue   = (String) resultEntity.get("cndValue");
                        String cndCondition = (String) resultEntity.get("cndCondition");
                        if ( (cndCondition == null) || (cndCondition != null && cndCondition.isEmpty())){
                            cndCondition = "LIKE";
                        }
                        if (conditionsMap.get(cndField) == null){
                            Map<String,String> fieldConditionMap = new HashMap<String,String>();
                            fieldConditionMap.put(cndValue, cndCondition);
                            conditionsMap.put(cndField,fieldConditionMap);
                        }else{
                            Map<String,String> fieldConditionMap = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
                            fieldConditionMap = conditionsMap.get(cndField);
                            fieldConditionMap.put(cndValue, cndCondition);
                            conditionsMap.put(cndField,fieldConditionMap);
                        }
                    }
                }
            }catch (Exception e){
                Debug.logError("------------------------------ getDefaultCondition ERROR: "+ e.getMessage(), module);
            }
        }
        if (lLog){
            for (Map.Entry<String,Map<String,String>> entry : conditionsMap.entrySet()) {
                Debug.logInfo("------------------------------ getDefaultCondition Map:" + entry.getKey() +" --> "+entry.getValue().toString(), module);
            }
        }
        return conditionsMap;
    }

    /** getDefaultCondition (SQL) – se omite si no existe el modelo/tabla */
    public static Map<String,Map<String,String>> getDefaultConditionBySql( String userLoginId, String componentName, String entityName, Delegator delegator){
        Boolean lLog = false;
        Map<String,Map<String,String>> conditionsMap = new HashMap<String,Map<String,String>>();
        if (!userLoginId.equalsIgnoreCase("admin")){
            // Si no hay modelo, tampoco intentamos acceder por SQL crudo
            if (!modelExists(delegator, "entityCondition")) {
                if (lLog) {
                    Debug.logInfo("getDefaultConditionBySql: entityCondition model not found; skipping.", module);
                }
                return conditionsMap;
            }
            try{
                GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.apache.ofbiz");
                SQLProcessor sqlproc = new SQLProcessor(delegator,helperInfo);
                String sqlStr = "select cnd_Field,cnd_Value,cnd_condition "+
                        "  from entity_condition "+
                        " where cnd_plugin ='"+ componentName + "' " +
                        "   and cnd_user ='"+ userLoginId + "' " +
                        "   and cnd_entity ='" + entityName + "' "+
                        "   and cnd_dll ='SELECT'";

                sqlproc.prepareStatement(sqlStr);
                ResultSet rs1 = sqlproc.executeQuery();

                while (rs1.next()) {
                    String cndField   = rs1.getString(1);
                    String cndValue = rs1.getString(2);
                    String cndCondition = rs1.getString(3);
                    if ((cndCondition == null) || (cndCondition != null && cndCondition.isEmpty())){
                        cndCondition = "LIKE";
                    }
                    if (conditionsMap.get(cndField) == null){
                        Map<String,String> fieldConditionMap = new HashMap<String,String>();
                        fieldConditionMap.put(cndValue, cndCondition);
                        conditionsMap.put(cndField,fieldConditionMap);
                    }else{
                        Map<String,String> fieldConditionMap = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
                        fieldConditionMap = conditionsMap.get(cndField);
                        fieldConditionMap.put(cndValue, cndCondition);
                        conditionsMap.put(cndField,fieldConditionMap);
                    }
                }
                rs1.close();
                sqlproc.close();

            }catch (Exception e){
                Debug.logError("------------------------------ getDefaultConditionBySql ERROR: "+ e.getMessage(), module);
            }
        }
        if (lLog){
            for (Map.Entry<String,Map<String,String>> entry : conditionsMap.entrySet()) {
                Debug.logError("------------------------------ getDefaultConditionBySql user: " + userLoginId + " Map:" + entry.getKey() +" --> "+entry.getValue().toString(), module);
            }
        }
        return conditionsMap;
    }

    public static List<String> getFieldNames(Map<String,FieldMetadata> allFieldMap){
        List<String> result = new ArrayList<String>();
        try{
            for (Map.Entry<String, FieldMetadata> entry : allFieldMap.entrySet()) {
                result.add(entry.getKey());
            }
        }catch (Exception e){
            Debug.logError("------------------------------ getFieldNames error:" + e.getMessage(), module);
        }
        return result;
    }

    /** changeListEntityCondition overlap or create entity conditions for the search form */
    public static void changeListEntityCondition(String userLoginId, List<EntityCondition> tmpList, Map<String,Map<String,String>> conditionsMap, String entityName, Delegator delegator, Boolean lDeleteOldQuery){
        Boolean lLog = false;
        String allrecords = "_allrecords";

        final String[] FIELDTYPES = new String[] {"id", "id-long", "id-vlong", "indicator", "very-short",
                "short-varchar", "long-varchar", "very-long", "comment",
                "description", "name", "value", "credit-card-number",
                "credit-card-date", "email", "url", "id-ne",
                "id-long-ne", "id-vlong-ne", "tel-number"};

        for (Map.Entry<String, Map<String,String>> entry : conditionsMap.entrySet()) {
            String cndField   = entry.getKey();

            Map<String,FieldMetadata> allFieldMap = new TreeMap<String,FieldMetadata>(String.CASE_INSENSITIVE_ORDER);
            String table_name = getEntityData(userLoginId, entityName, delegator, allFieldMap);

            if (lLog){
                Debug.logInfo("------------------------------ changeListEntityCondition0 user: " + userLoginId + " cndField: "+ cndField + " table_name: "+ table_name + " entityName: " + entityName + " allFieldMap:" + allFieldMap.size(), module);
            }

            FieldMetadata fieldMetadata = allFieldMap.get(cndField);
            if (fieldMetadata != null){
                String cndFieldEntity = fieldMetadata.getFieldNameEntity();
                String cndFieldTable = fieldMetadata.getFieldNameBBDD();
                String fieldTypeEntity = fieldMetadata.getFieldTypeEntity();

                boolean isMatched = Arrays.asList(FIELDTYPES).stream().anyMatch(fieldTypeEntity::equalsIgnoreCase);
                if (isMatched){
                    Map<String,String> cndValueMap = entry.getValue();

                    if (lDeleteOldQuery){
                        List<EntityCondition> deleteCandidates = new ArrayList<>();
                        for (EntityCondition entityCondition : tmpList) {
                            if (entityCondition.toString().toUpperCase().contains(cndFieldTable.toUpperCase())){
                                deleteCandidates.add(entityCondition);
                            }
                        }
                        for (EntityCondition deleteCandidate : deleteCandidates) {
                            tmpList.remove(deleteCandidate);
                            if (lLog){
                                Debug.logInfo("+++++++++++++++++++++++++++changeListEntityCondition user: " + userLoginId + " Removed:" + deleteCandidate, module);
                            }
                        }
                    }

                    Boolean lAllrecords = false;
                    for (Map.Entry<String, String> entryValues : cndValueMap.entrySet()) {
                        if (entryValues.getKey().equalsIgnoreCase(allrecords)){
                            lAllrecords = true;
                            break;
                        }
                    }

                    if (!lAllrecords){
                        String sqlString = "";
                        for (Map.Entry<String, String> entryValues : cndValueMap.entrySet()) {
                            String cndValue = entryValues.getKey();
                            String cndCondition = entryValues.getValue();
                            if( (sqlString == null) || (sqlString != null && sqlString.isEmpty()) ) {
                                if (cndCondition.equalsIgnoreCase("EQUAL")){
                                    sqlString = cndFieldTable + " = '" + cndValue +"'";
                                }else{
                                    sqlString = cndFieldTable + " LIKE '%" + cndValue +"%'";
                                }
                            }else{
                                if (cndCondition.equalsIgnoreCase("EQUAL")){
                                    sqlString = sqlString + " or " + cndFieldTable + " = '" + cndValue + "'";
                                }else{
                                    sqlString = sqlString + " or " + cndFieldTable + " LIKE '%" + cndValue +"%'";
                                }
                            }
                        }
                        boolean sqlStatus = ((sqlString == null) || (sqlString != null && sqlString.isEmpty()));
                        if (!sqlStatus && cndValueMap.entrySet().size() > 1){
                            sqlString = "(" + sqlString + ")";
                        }
                        EntityCondition newEntityCondition = EntityCondition.makeConditionWhere(sqlString);
                        tmpList.add(newEntityCondition);
                        if (lLog){
                            Debug.logInfo("+++++++++++++++++++++++++++changeListEntityCondition user: " + userLoginId + " Added:" + newEntityCondition, module);
                        }
                    }else{
                        Debug.logInfo("HINT: ChangeListEntityCondition user: " + userLoginId + " lAllrecords found for field:" + cndFieldTable, module);
                    }
                }else{
                    Debug.logInfo("------------------------------ changeListEntityCondition user: " + userLoginId + " Field: " +  cndField + " NOT MATCH TYPE IN PERMITTED TYPES: " +
                            FindServices.listToString(Arrays.asList(FIELDTYPES), ","), module);
                }
            }else{
                Debug.logInfo("------------------------------ changeListEntityCondition user: " + userLoginId + " Field: " +  cndField + " NOT FOUND IN METADATA FOR ENTITYNAME " + entityName + ": " +
                        FindServices.listToString(getFieldNames(allFieldMap), ","), module);
            }
        }

        if (lLog){
            Debug.logInfo("=== changeListEntityCondition List conditions user: " + userLoginId + ":", module);
            for (EntityCondition temp : tmpList) {
                Debug.logInfo("+++++++++++++++++++++++++++ " + temp.toString(), module);
            }
        }
    }

    static class FieldMetadata{
        private String  fieldNameEntity;
        private String  fieldNameBBDD;
        private String  fieldTypeEntity;

        public String getFieldNameEntity() { return fieldNameEntity; }
        public void setFieldNameEntity(String fieldNameEntity) { this.fieldNameEntity = fieldNameEntity; }
        public String getFieldNameBBDD() { return fieldNameBBDD; }
        public void setFieldNameBBDD(String fieldNameBBDD) { this.fieldNameBBDD = fieldNameBBDD; }
        public String getFieldTypeEntity() { return fieldTypeEntity; }
        public void setFieldTypeEntity(String fieldTypeEntity) { this.fieldTypeEntity = fieldTypeEntity; }

        @Override
        public String toString() {
            return "FieldMetadata2 [fieldNameEntity=" + fieldNameEntity + ", fieldNameBBDD=" + fieldNameBBDD
                    + ", fieldTypeEntity=" + fieldTypeEntity + "]";
        }
    }

    public static String getEntityData(String userLoginId, String entityName, Delegator delegator, Map<String,FieldMetadata> allFieldMap){
        Boolean lLog = false;
        ModelEntity m = delegator.getModelEntity(entityName);
        GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.apache.ofbiz");
        String tableName = m.getTableName(helperInfo.getHelperBaseName());

        if (lLog){
            Debug.logInfo("================ user: " + userLoginId + " TABLENAME entityName: "+ entityName + " tableName:" + tableName, module);
        }

        List<String> allFieldNamesList = m.getAllFieldNames();
        for (String fieldNameEntity : allFieldNamesList) {
            String fieldNameBBDD = m.getColNameOrAlias(fieldNameEntity);
            ModelField mf = m.getField(fieldNameEntity);
            String fieldTypeEntity = mf.getType();

            FieldMetadata fieldMetadata = new FieldMetadata();
            fieldMetadata.setFieldNameEntity(fieldNameEntity);
            fieldMetadata.setFieldNameBBDD(fieldNameBBDD);
            fieldMetadata.setFieldTypeEntity(fieldTypeEntity);
            allFieldMap.put(fieldNameEntity, fieldMetadata);

            if (lLog){
                Debug.logInfo("     ================ user: " + userLoginId + " FieldNameEntity:" + fieldNameEntity + " fieldNameBBDD:" + fieldNameBBDD + " FieldTypeEntity:"+fieldTypeEntity, module);
            }
        }
        return tableName;
    }

    public static String getEntityDataByDatabase(String userLoginId, String entityName, Delegator delegator, Map<String,FieldMetadata> allFieldMap){
        Boolean lLog = false;
        ModelEntity m = delegator.getModelEntity(entityName);
        GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.apache.ofbiz");
        String tableName = m.getTableName(helperInfo.getHelperBaseName());
        if (lLog){
            Debug.logInfo("================ getEntityDataByDatabase user: " + userLoginId + " TABLENAME entityName: "+ entityName + " tableName:" + tableName, module);
        }

        List<String> allFieldNamesList = m.getAllFieldNames();
        for (String fieldNameEntity : allFieldNamesList) {
            String fieldNameBBDD = m.getColNameOrAlias(fieldNameEntity);
            ModelField mf = m.getField(fieldNameEntity);
            String fieldTypeEntity = mf.getType();

            FieldMetadata fieldMetadata = new FieldMetadata();
            fieldMetadata.setFieldNameEntity(fieldNameEntity);
            fieldMetadata.setFieldNameBBDD(fieldNameBBDD);
            fieldMetadata.setFieldTypeEntity(fieldTypeEntity);
            allFieldMap.put(fieldNameBBDD, fieldMetadata);
            if (lLog){
                Debug.logInfo("     ================ user: " + userLoginId + " FieldNameEntity:" + fieldNameEntity + " fieldNameBBDD:" + fieldNameBBDD + " FieldTypeEntity:"+fieldTypeEntity, module);
            }
        }
        return tableName;
    }

    /**
     * prepareFind
     */
    public static Map<String, Object> prepareFind(DispatchContext dctx, Map<String, ?> context) {
        Boolean lLog = false;
        String entityName = (String) context.get("entityName");
        Delegator delegator = dctx.getDelegator();
        String orderBy = (String) context.get("orderBy");
        Map<String, ?> inputFields = checkMap(context.get("inputFields"), String.class, Object.class);
        String noConditionFind = (String) context.get("noConditionFind");
        if (UtilValidate.isEmpty(noConditionFind)) {
            noConditionFind = (String) inputFields.get("noConditionFind");
        }
        if (UtilValidate.isEmpty(noConditionFind)) {
            noConditionFind = EntityUtilProperties.getPropertyValue("widget", "widget.defaultNoConditionFind", delegator);
        }
        String filterByDate = (String) context.get("filterByDate");
        if (UtilValidate.isEmpty(filterByDate)) {
            filterByDate = (String) inputFields.get("filterByDate");
        }
        Timestamp filterByDateValue = (Timestamp) context.get("filterByDateValue");
        String fromDateName = (String) context.get("fromDateName");
        String thruDateName = (String) context.get("thruDateName");

        Map<String, Object> queryStringMap = new LinkedHashMap<String, Object>();
        ModelEntity modelEntity = delegator.getModelEntity(entityName);
        List<EntityCondition> tmpList = createConditionList(inputFields, modelEntity.getFieldsUnmodifiable(), queryStringMap, delegator, context);

        // Filtro por fecha opcional
        if (tmpList.size() > 0 || "Y".equals(noConditionFind)) {
            if ("Y".equals(filterByDate)) {
                queryStringMap.put("filterByDate", filterByDate);
                if (UtilValidate.isEmpty(fromDateName)) fromDateName = "fromDate";
                else queryStringMap.put("fromDateName", fromDateName);
                if (UtilValidate.isEmpty(thruDateName)) thruDateName = "thruDate";
                else queryStringMap.put("thruDateName", thruDateName);
                if (UtilValidate.isEmpty(filterByDateValue)) {
                    EntityCondition filterByDateCondition = EntityUtil.getFilterByDateExpr(fromDateName, thruDateName);
                    tmpList.add(filterByDateCondition);
                } else {
                    queryStringMap.put("filterByDateValue", filterByDateValue);
                    EntityCondition filterByDateCondition = EntityUtil.getFilterByDateExpr(filterByDateValue, fromDateName, thruDateName);
                    tmpList.add(filterByDateCondition);
                }
            }
        }

        // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        // IMPORTANTE: deshabilitamos por completo los defaults por usuario/grupo
        // (entityCondition). Así evitamos cualquier acceso a una entidad que no existe.
        // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

        EntityConditionList<EntityCondition> exprList = null;
        if (tmpList.size() > 0) {
            exprList = EntityCondition.makeCondition(tmpList);
        }

        List<String> orderByList = null;
        if (UtilValidate.isNotEmpty(orderBy)) {
            orderByList = StringUtil.split(orderBy,"|");
        }

        Map<String, Object> results = ServiceUtil.returnSuccess();
        queryStringMap.put("noConditionFind", noConditionFind);
        String queryString = UtilHttp.urlEncodeArgs(queryStringMap);
        results.put("queryString", queryString);
        results.put("queryStringMap", queryStringMap);
        results.put("orderByList", orderByList);
        results.put("entityConditionList", exprList);
        return results;
    }

    /**
     * executeFind
     */
    public static Map<String, Object> executeFind(DispatchContext dctx, Map<String, ?> context) {
        String entityName = (String) context.get("entityName");
        EntityConditionList<EntityCondition> entityConditionList = UtilGenerics.cast(context.get("entityConditionList"));
        List<String> orderByList = checkList(context.get("orderByList"), String.class);
        boolean noConditionFind = "Y".equals(context.get("noConditionFind"));
        boolean distinct = "Y".equals(context.get("distinct"));
        List<String> fieldList =  UtilGenerics.checkList(context.get("fieldList"));
        Locale locale = (Locale) context.get("locale");
        Set<String> fieldSet = null;
        if (fieldList != null) {
            fieldSet = UtilMisc.makeSetWritable(fieldList);
        }
        Integer maxRows = (Integer) context.get("maxRows");
        maxRows = maxRows != null ? maxRows : -1;
        Delegator delegator = dctx.getDelegator();

        EntityListIterator listIt = null;
        int listSize = 0;
        try {
            if (noConditionFind || (entityConditionList != null && entityConditionList.getConditionListSize() > 0)) {
                listIt = EntityQuery.use(delegator)
                        .select(fieldSet)
                        .from(entityName)
                        .where(entityConditionList)
                        .orderBy(orderByList)
                        .cursorScrollInsensitive()
                        .maxRows(maxRows)
                        .distinct(distinct)
                        .queryIterator();
                listSize = listIt.getResultsSizeAfterPartialList();
            }
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource, "CommonFindErrorRunning", UtilMisc.toMap("entityName", entityName, "errorString", e.getMessage()), locale));
        }
        Map<String, Object> results = ServiceUtil.returnSuccess();
        results.put("listIt", listIt);
        results.put("listSize", listSize);
        return results;
    }

    private static String dayStart(String timeStampString, int daysLater, TimeZone timeZone, Locale locale) {
        String retValue = null;
        Timestamp ts = null;
        Timestamp startTs = null;
        try {
            ts = Timestamp.valueOf(timeStampString);
        } catch (IllegalArgumentException e) {
            timeStampString += " 00:00:00.000";
            try {
                ts = Timestamp.valueOf(timeStampString);
            } catch (IllegalArgumentException e2) {
                return retValue;
            }
        }
        startTs = UtilDateTime.getDayStart(ts, daysLater, timeZone, locale);
        retValue = startTs.toString();
        return retValue;
    }

    public static Map<String, Object> buildReducedQueryString(Map<String, ?> inputFields, String entityName, Delegator delegator) {
        ModelEntity modelEntity = delegator.getModelEntity(entityName);
        Map<String, Object> normalizedFields = new LinkedHashMap<String, Object>();
        for (String fieldNameRaw: inputFields.keySet()) {
            String fieldNameRoot = null;
            Object fieldValue = null;
            int iPos = -1;
            int iPos2 = -1;

            fieldValue = inputFields.get(fieldNameRaw);
            if (ObjectType.isEmpty(fieldValue)) {
                continue;
            }
            iPos = fieldNameRaw.indexOf("_");
            if (iPos >= 0) {
                String suffix = fieldNameRaw.substring(iPos + 1);
                iPos2 = suffix.indexOf("_");
                if (iPos2 == 1) {
                    continue;
                }
            }
            if (iPos < 0) {
                fieldNameRoot = fieldNameRaw;
            } else {
                fieldNameRoot = fieldNameRaw.substring(0, iPos);
            }
            if (modelEntity.isField(fieldNameRoot)) {
                normalizedFields.put(fieldNameRaw, fieldValue);
            }
        }
        return normalizedFields;
    }

    /**
     * Returns the first generic item of the service 'performFind'
     */
    public static Map<String, Object> performFindItem(DispatchContext dctx, Map<String, Object> context) {
        context.put("viewSize", 1);
        context.put("viewIndex", 0);
        Map<String, Object> result = org.apache.ofbiz.common.FindServices.performFind(dctx,context);

        List<GenericValue> list = null;
        GenericValue item= null;
        try {
            EntityListIterator it = (EntityListIterator) result.get("listIt");
            list = it.getPartialList(1, 1);
            if (UtilValidate.isNotEmpty(list)) {
                item = list.get(0);
            }
            it.close();
        } catch (Exception e) {
            Debug.logInfo("Problem getting list Item" + e,module);
        }

        if (UtilValidate.isNotEmpty(item)) {
            result.put("item",item);
        }
        result.remove("listIt");

        if (result.containsKey("listSize")) {
            result.remove("listSize");
        }
        return result;
    }
}
