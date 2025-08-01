package org.apache.ofbiz.invoice.mock;

import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.service.ServiceUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.ofbiz.base.util.UtilGenerics;

public class InvoiceMockWorker {

    public static final String module = InvoiceMockWorker.class.getName();
    private static String invInvoiceNumber = "";

    public static Map<String, Object> getMockedInvoices(DispatchContext dctx, Map<String, Object> context) {
        Delegator delegator = dctx.getDelegator();

        List<GenericValue> filteredMockList = new ArrayList<>();

        Boolean listIt = Boolean.TRUE.equals(context.get("listIt"));

        // Retrieve filter parameters from the context
        String invSupplierVatNumber = (String) context.get("invSupplierVatNumber");
        String invSupplierName = (String) context.get("invSupplierName");
        String invSupplierCode = (String) context.get("invSupplierCode");
        invInvoiceNumber = (String) context.get("invInvoiceNumber");

        Timestamp invInvoiceDateFrom = (Timestamp) context.get("invInvoiceDateFrom");
        Timestamp invInvoiceDateTo = (Timestamp) context.get("invInvoiceDateTo");
        Timestamp invValueDateFrom = (Timestamp) context.get("invValueDateFrom");
        Timestamp invValueDateTo = (Timestamp) context.get("invValueDateTo");
        Timestamp invPaymentDueDateFrom = (Timestamp) context.get("invPaymentDueDateFrom");
        Timestamp invPaymentDueDateTo = (Timestamp) context.get("invPaymentDueDateTo");

        String invTaxType = (String) context.get("invTaxType");
        String invTaxRateStr = (String) context.get("invTaxRate");
        String invCurrency = (String) context.get("invCurrency");
        String invPaymentMethod = (String) context.get("invPaymentMethod");

        String invNetAmountFromStr = (String) context.get("invNetAmountFrom");
        String invNetAmountToStr = (String) context.get("invNetAmountTo");

        String invConfirmed = (String) context.get("invConfirmed");
        String invPaymentDone = (String) context.get("invPaymentDone");

        // ******* INICIO DE LOGGING PARA DEPURACIÓN *******
        Debug.logInfo("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA >>>   " + listIt, module);
        Debug.logInfo("---- Incoming Filter Parameters ----", module);
        Debug.logInfo("invSupplierVatNumber: " + invSupplierVatNumber, module);
        Debug.logInfo("invSupplierName: " + invSupplierName, module);
        Debug.logInfo("invSupplierCode: " + invSupplierCode, module);
        Debug.logInfo("invInvoiceNumber: " + invInvoiceNumber, module);
        Debug.logInfo("invInvoiceDateFrom: " + invInvoiceDateFrom, module);
        Debug.logInfo("invInvoiceDateTo: " + invInvoiceDateTo, module);
        Debug.logInfo("invValueDateFrom: " + invValueDateFrom, module);
        Debug.logInfo("invValueDateTo: " + invValueDateTo, module);
        Debug.logInfo("invPaymentDueDateFrom: " + invPaymentDueDateFrom, module);
        Debug.logInfo("invPaymentDueDateTo: " + invPaymentDueDateTo, module);
        Debug.logInfo("invTaxType: " + invTaxType, module);
        Debug.logInfo("invTaxRateStr: " + invTaxRateStr, module);
        Debug.logInfo("invCurrency: " + invCurrency, module);
        Debug.logInfo("invPaymentMethod: " + invPaymentMethod, module);
        Debug.logInfo("invNetAmountFromStr: " + invNetAmountFromStr, module);
        Debug.logInfo("invNetAmountToStr: " + invNetAmountToStr, module);
        Debug.logInfo("invConfirmed: " + invConfirmed, module);
        Debug.logInfo("invPaymentDone: " + invPaymentDone, module);
        Debug.logInfo("------------------------------------", module);
        // ******* FIN DE LOGGING PARA DEPURACIÓN *******


        // Convert numeric strings to BigDecimal for comparison
        BigDecimal invTaxRate = null;
        if (UtilValidate.isNotEmpty(invTaxRateStr)) {
            try {
                invTaxRate = new BigDecimal(invTaxRateStr);
            } catch (NumberFormatException e) {
                Debug.logWarning("Invalid invTaxRate format: " + invTaxRateStr + " Error: " + e.getMessage(), module);
            }
        }

        BigDecimal invNetAmountFrom = null;
        if (UtilValidate.isNotEmpty(invNetAmountFromStr)) {
            try {
                invNetAmountFrom = new BigDecimal(invNetAmountFromStr);
            } catch (NumberFormatException e) {
                Debug.logWarning("Invalid invNetAmountFrom format: " + invNetAmountFromStr + " Error: " + e.getMessage(), module);
            }
        }

        BigDecimal invNetAmountTo = null;
        if (UtilValidate.isNotEmpty(invNetAmountToStr)) {
            try {
                invNetAmountTo = new BigDecimal(invNetAmountToStr);
            } catch (NumberFormatException e) {
                Debug.logWarning("Invalid invNetAmountTo format: " + invNetAmountToStr + " Error: " + e.getMessage(), module);
            }
        }

        Object[][] allInvoicesData = {
                {"ES12345678A", "Tech Solutions Inc.", "TS001", "IVA", 21.00, 0.00, "EUR", "Bank Transfer", "INV-2023-001", "2023-01-15", "2023-01-16", "2023-02-15", 123456L, "EUR", 1.00, 800.00, 800.00, 168.00, 968.00, true, true, "a1b2c3d4-e5f6-7890-1234-567890abcdef", "F01", 10.00, 5.00, "ES"},
                {"DE87654321B", "Global Supplies GmbH", "GS002", "VAT", 19.00, 0.00, "EUR", "Credit Card", "GSI-00554", "2023-02-01", "2023-02-02", "2023-03-01", 789012L, "EUR", 1.00, 120.50, 120.50, 22.89, 143.39, false, true, "b2c3d4e5-f6a7-8901-2345-67890abcdef0", "F02", 0.00, 0.00, "DE"},
                {"FR99887766C", "Services Pro", "SP003", "TVA", 20.00, 15.00, "EUR", "Cheque", "SP/2023/012", "2023-03-10", "2023-03-11", "2023-04-10", 345678L, "EUR", 1.00, 450.00, 450.00, 90.00, 540.00, true, false, "c3d4e5f6-a7b8-9012-3456-7890abcdef01", "F01", 5.00, 2.50, "FR"},
                {"UK11223344D", "London Tech Ltd.", "LT004", "VAT", 20.00, 0.00, "GBP", "Bank Transfer", "LT/INV/007", "2023-03-20", "2023-03-21", "2023-04-20", 987654L, "EUR", 1.17, 250.00, 250.00, 50.00, 300.00, false, true, "d4e5f6a7-b8c9-0123-4567-890abcdef012", "F03", 0.00, 1.00, "UK"},
                {"IT55667788E", "Italia SpA", "IT005", "IVA", 22.00, 0.00, "EUR", "Credit Card", "IT-2023-033", "2023-04-05", "2023-04-06", "2023-05-05", 210987L, "EUR", 1.00, 60.00, 60.00, 13.20, 73.20, true, true, "e5f6a7b8-c9d0-1234-5678-90abcdef0123", "F01", 0.00, 0.00, "IT"},
                {"PT12312312F", "Portugal Supply", "PS006", "IVA", 23.00, 0.00, "EUR", "Bank Transfer", "PT/XYZ/001", "2023-04-12", "2023-04-13", "2023-05-12", 456789L, "EUR", 1.00, 300.00, 300.00, 69.00, 369.00, false, false, "f6a7b8c9-d0e1-2345-6789-0abcdef01234", "F02", 0.00, 0.00, "PT"},
                {"BE44556677G", "Belgian Corp", "BC007", "TVA", 21.00, 0.00, "EUR", "Cheque", "BC-2023-045", "2023-05-01", "2023-05-02", "2023-06-01", 654321L, "EUR", 1.00, 900.00, 900.00, 189.00, 1089.00, true, true, "a7b8c9d0-e1f2-3456-7890-abcdef012345", "F01", 20.00, 0.00, "BE"},
                {"NL88990011H", "Dutch Dynamics", "DD008", "BTW", 21.00, 0.00, "EUR", "Credit Card", "DD-INV-009", "2023-05-10", "2023-05-11", "2023-06-10", 765432L, "EUR", 1.00, 50.00, 50.00, 10.50, 60.50, true, true, "b8c9d0e1-f2a3-4567-8901-234567890abc", "F03", 0.00, 0.00, "NL"},
                {"SE12121212I", "Sweden AB", "SA009", "MOMS", 25.00, 0.00, "SEK", "Bank Transfer", "SA-2023-010", "2023-05-18", "2023-05-19", "2023-06-18", 876543L, "EUR", 0.086, 1500.00, 1500.00, 375.00, 1875.00, false, true, "c9d0e1f2-a3b4-5678-9012-34567890abcd", "F01", 0.00, 0.00, "SE"},
                {"CH33445566J", "Swiss Systems", "SS010", "MWST", 7.70, 0.00, "CHF", "Bank Transfer", "SS-INV-011", "2023-06-01", "2023-06-02", "2023-07-01", 901234L, "EUR", 1.03, 2000.00, 2000.00, 154.00, 2154.00, false, false, "d0e1f2a3-b4c5-6789-0123-4567890abcde", "F04", 50.00, 0.00, "CH"},
                {"AT66554433K", "Austria Solutions", "AS011", "UST", 20.00, 0.00, "EUR", "Credit Card", "AT-INV-012", "2023-06-05", "2023-06-06", "2023-07-05", 112233L, "EUR", 1.00, 75.00, 75.00, 15.00, 90.00, true, true, "e1f2a3b4-c5d6-7890-1234-567890abcdef", "F01", 0.00, 0.00, "AT"},
                {"ES12345678A", "Tech Solutions Inc.", "TS001", "IVA", 21.00, 0.00, "EUR", "Bank Transfer", "INV-2023-002", "2023-01-20", "2023-01-21", "2023-02-20", 223344L, "EUR", 1.00, 300.00, 300.00, 63.00, 363.00, false, true, "f2a3b4c5-d6e7-8901-2345-67890abcdef0", "F01", 0.00, 0.00, "ES"},
                {"DE87654321B", "Global Supplies GmbH", "GS002", "VAT", 19.00, 0.00, "EUR", "Bank Transfer", "GSI-00555", "2023-02-05", "2023-02-06", "2023-03-05", 334455L, "EUR", 1.00, 50.00, 50.00, 9.50, 59.50, true, false, "a3b4c5d6-e7f8-9012-3456-7890abcdef01", "F02", 0.00, 0.00, "DE"},
                {"FR99887766C", "Services Pro", "SP003", "TVA", 20.00, 15.00, "EUR", "Cheque", "SP/2023/013", "2023-03-15", "2023-03-16", "2023-04-15", 445566L, "EUR", 1.00, 1200.00, 1200.00, 240.00, 1440.00, true, true, "b4c5d6e7-f8a9-0123-4567-890abcdef012", "F01", 10.00, 5.00, "FR"}
        };

        for (int i = 0; i < allInvoicesData.length; i++) {
            Object[] invoiceData = allInvoicesData[i];

            // Extract values with appropriate types
            String currentSupplierVatNumber = (String) invoiceData[0];
            String currentSupplierName = (String) invoiceData[1];
            String currentSupplierCode = (String) invoiceData[2];
            String currentTaxType = (String) invoiceData[3];
            BigDecimal currentTaxRate = new BigDecimal(String.valueOf(invoiceData[4]));
            BigDecimal currentIrpf = new BigDecimal(String.valueOf(invoiceData[5]));
            String currentCurrency = (String) invoiceData[6];
            String currentPaymentMethod = (String) invoiceData[7];
            String currentInvoiceNumber = (String) invoiceData[8];
            Timestamp currentInvoiceDate = Timestamp.valueOf(invoiceData[9] + " 00:00:00.000");
            Timestamp currentValueDate = Timestamp.valueOf(invoiceData[10] + " 00:00:00.000");
            Timestamp currentPaymentDueDate = Timestamp.valueOf(invoiceData[11] + " 00:00:00.000");
            Long currentProof = (Long) invoiceData[12];
            String currentAccountingCurrency = (String) invoiceData[13];
            Double currentExchangeRate = (Double) invoiceData[14];
            BigDecimal currentNetAmount = new BigDecimal(String.valueOf(invoiceData[15]));
            BigDecimal currentAmountWithoutVat = new BigDecimal(String.valueOf(invoiceData[16]));
            BigDecimal currentTotalVatAmount = new BigDecimal(String.valueOf(invoiceData[17]));
            BigDecimal currentTotalAmountWithVat = new BigDecimal(String.valueOf(invoiceData[18]));
            String currentPaymentDone = ((Boolean) invoiceData[19]) ? "Y" : "N";
            String currentConfirmed = ((Boolean) invoiceData[20]) ? "Y" : "N";
            String currentUuid = (String) invoiceData[21];
            String currentDocumentTypeCode = (String) invoiceData[22];
            BigDecimal currentTotalDiscounts = new BigDecimal(String.valueOf(invoiceData[23]));
            BigDecimal currentTotalCharges = new BigDecimal(String.valueOf(invoiceData[24]));
            String currentCountry = (String) invoiceData[25];

            // Apply Filters
            boolean matches = true;

            // Text fields (case-insensitive contains)
            if (UtilValidate.isNotEmpty(invSupplierVatNumber) && !currentSupplierVatNumber.toLowerCase().contains(invSupplierVatNumber.toLowerCase())) {
                matches = false;
            }
            if (matches && UtilValidate.isNotEmpty(invSupplierName) && !currentSupplierName.toLowerCase().contains(invSupplierName.toLowerCase())) {
                matches = false;
            }
            if (matches && UtilValidate.isNotEmpty(invSupplierCode) && !currentSupplierCode.toLowerCase().contains(invSupplierCode.toLowerCase())) {
                matches = false;
            }
            if (matches && UtilValidate.isNotEmpty(invInvoiceNumber) && !currentInvoiceNumber.toLowerCase().contains(invInvoiceNumber.toLowerCase())) {
                matches = false;
            }
            if (matches && UtilValidate.isNotEmpty(invTaxType) && !currentTaxType.toLowerCase().contains(invTaxType.toLowerCase())) {
                matches = false;
            }
            if (matches && UtilValidate.isNotEmpty(invCurrency) && !currentCurrency.toLowerCase().contains(invCurrency.toLowerCase())) {
                matches = false;
            }
            if (matches && UtilValidate.isNotEmpty(invPaymentMethod) && !currentPaymentMethod.toLowerCase().contains(invPaymentMethod.toLowerCase())) {
                matches = false;
            }

            // Date fields (from/to range)
            if (matches && invInvoiceDateFrom != null && currentInvoiceDate.before(invInvoiceDateFrom)) {
                matches = false;
            }
            if (matches && invInvoiceDateTo != null && currentInvoiceDate.after(invInvoiceDateTo)) {
                matches = false;
            }
            if (matches && invValueDateFrom != null && currentValueDate.before(invValueDateFrom)) {
                matches = false;
            }
            if (matches && invValueDateTo != null && currentValueDate.after(invValueDateTo)) {
                matches = false;
            }
            if (matches && invPaymentDueDateFrom != null && currentPaymentDueDate.before(invPaymentDueDateFrom)) {
                matches = false;
            }
            if (matches && invPaymentDueDateTo != null && currentPaymentDueDate.after(invPaymentDueDateTo)) {
                matches = false;
            }

            // Numeric fields
            if (matches && invTaxRate != null && currentTaxRate.compareTo(invTaxRate) != 0) { // Exact match for tax rate
                matches = false;
            }
            if (matches && invNetAmountFrom != null && currentNetAmount.compareTo(invNetAmountFrom) < 0) {
                matches = false;
            }
            if (matches && invNetAmountTo != null && currentNetAmount.compareTo(invNetAmountTo) > 0) {
                matches = false;
            }

            // Dropdown fields (Y/N)
            if (matches && UtilValidate.isNotEmpty(invConfirmed) && !currentConfirmed.equals(invConfirmed)) {
                matches = false;
            }
            if (matches && UtilValidate.isNotEmpty(invPaymentDone) && !currentPaymentDone.equals(invPaymentDone)) {
                matches = false;
            }

            if (matches) {
                // Si todos los filtros coinciden, crea el GenericValue y añádelo a la lista
                Map<String, Object> fields = new HashMap<>(); // Se inicializa fields aquí para cada factura
                fields.put("invoiceId", "MOCK_INV_" + String.format("%03d", i + 1));
                fields.put("invCompany", "DEMO");
                fields.put("invCountry", currentCountry);

                fields.put("invSupplierVatNumber", currentSupplierVatNumber);
                fields.put("invSupplierName", currentSupplierName);
                fields.put("invSupplierCode", currentSupplierCode);
                fields.put("invTaxType", currentTaxType);
                fields.put("invIva", currentTaxRate);
                fields.put("invIrpf", currentIrpf);
                fields.put("invCurrency", currentCurrency);
                fields.put("invPaymentMethod", currentPaymentMethod);
                fields.put("invInvoiceNumber", currentInvoiceNumber);
                fields.put("invInvoiceDate", new Date(currentInvoiceDate.getTime()));
                fields.put("invValueDate", new Date(currentValueDate.getTime()));
                fields.put("invPaymentDueDate", new Date(currentPaymentDueDate.getTime()));
                fields.put("invProof", currentProof);
                fields.put("invAccountingCurrency", currentAccountingCurrency);
                fields.put("invExchangeRate", currentExchangeRate);
                fields.put("invNetAmount", currentNetAmount);
                fields.put("invAmountWithoutVat", currentAmountWithoutVat);
                fields.put("invTotalVatAmount", currentTotalVatAmount);
                fields.put("invTotalAmountWithVat", currentTotalAmountWithVat);
                fields.put("invPaymentDone", currentPaymentDone);
                fields.put("invConfirmed", currentConfirmed);
                fields.put("invUuid", currentUuid);
                fields.put("invDocumentTypeCode", currentDocumentTypeCode);
                fields.put("invTotalDiscounts", currentTotalDiscounts);
                fields.put("invTotalCharges", currentTotalCharges);

                GenericValue invoice = delegator.makeValue("FinInvoice", fields);
                filteredMockList.add(invoice);
            }
        }

        Map<String, Object> result = ServiceUtil.returnSuccess();
        result.put("entityList", filteredMockList);

        Debug.logInfo("Generated " + filteredMockList.size() + " filtered mock invoices.", module);

        return result;
    }

    public static Map<String, Object> getInvoiceDetailData(DispatchContext dctx, Map<String, Object> context) {
        Map<String, Object> result = new HashMap<>();
        String invoiceId = (String) context.get("invoiceId");

        if (invoiceId == null) {
            result.put("responseMessage", "error");
            result.put("errorMessage", "Missing invoiceId");
            return result;
        }

        Map<String, Object> mockResponse = getMockedInvoices(dctx, context);
        List<Map<String, Object>> invoices = UtilGenerics.checkList(mockResponse.get("entityList"));

        for (Map<String, Object> invoice : invoices) {
            if (invoiceId.equals(invoice.get("invInvoiceNumber"))) {

                result.put("invoiceId", invoiceId);
                result.put("invSupplierCode", (String) invoice.get("invSupplierCode"));
                result.put("invSupplierName", (String) invoice.get("invSupplierName"));
                result.put("invSupplierVatNumber", (String) invoice.get("invSupplierVatNumber"));
                result.put("invInvoiceNumber", (String) invoice.get("invInvoiceNumber"));

                // Fechas: convertir java.sql.Date → Timestamp
                result.put("invInvoiceDate", toTimestamp(invoice.get("invInvoiceDate")));
                result.put("invValueDate", toTimestamp(invoice.get("invValueDate")));
                result.put("invPaymentDueDate", toTimestamp(invoice.get("invPaymentDueDate")));

                result.put("invTaxType", (String) invoice.get("invTaxType"));
                result.put("invIva", toBigDecimal(invoice.get("invIva")));
                result.put("invIrpf", toBigDecimal(invoice.get("invIrpf")));
                result.put("invCurrency", (String) invoice.get("invCurrency"));
                result.put("invPaymentMethod", (String) invoice.get("invPaymentMethod"));

                result.put("invNetAmount", toBigDecimal(invoice.get("invNetAmount")));
                result.put("invAmountWithoutVat", toBigDecimal(invoice.get("invAmountWithoutVat")));
                result.put("invTotalVatAmount", toBigDecimal(invoice.get("invTotalVatAmount")));
                Object rawValue = invoice.get("invTotalVatAmountInAccountingCurrency");
                BigDecimal value = rawValue != null ? toBigDecimal(rawValue) : BigDecimal.ZERO;
                result.put("invTotalVatAmountInAccountingCurrency", value);
                result.put("invTotalAmountWithVat", toBigDecimal(invoice.get("invTotalAmountWithVat")));
                result.put("invExchangeRate", toBigDecimal(invoice.get("invExchangeRate")));

                result.put("invAccountingCurrency", (String) invoice.get("invAccountingCurrency"));
                result.put("invPaymentDone", String.valueOf(invoice.get("invPaymentDone")));
                result.put("invConfirmed", String.valueOf(invoice.get("invConfirmed")));
                result.put("invUuid", (String) invoice.get("invUuid"));
                result.put("invProof", String.valueOf(invoice.get("invProof")));
                result.put("invCompany", (String) invoice.get("invCompany"));
                result.put("invCountry", (String) invoice.get("invCountry"));
                result.put("invDocumentTypeCode", (String) invoice.get("invDocumentTypeCode"));
                result.put("invTotalCharges", toBigDecimal(invoice.get("invTotalCharges")));
                result.put("invTotalDiscounts", toBigDecimal(invoice.get("invTotalDiscounts")));

                result.put("responseMessage", "success");
                return result;
            }
        }

        result.put("responseMessage", "error");
        result.put("errorMessage", "Invoice not found");
        return result;
    }

    private static BigDecimal toBigDecimal(Object val) {
        if (val instanceof BigDecimal) return (BigDecimal) val;
        if (val instanceof Double) return BigDecimal.valueOf((Double) val);
        if (val instanceof Long) return BigDecimal.valueOf((Long) val);
        if (val instanceof String) return new BigDecimal((String) val);
        return null;
    }

    private static Timestamp toTimestamp(Object val) {
        if (val instanceof java.sql.Date) {
            return new Timestamp(((java.sql.Date) val).getTime());
        }
        if (val instanceof java.util.Date) {
            return new Timestamp(((java.util.Date) val).getTime());
        }
        return null;
    }

}