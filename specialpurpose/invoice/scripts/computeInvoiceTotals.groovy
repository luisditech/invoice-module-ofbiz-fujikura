import java.math.BigDecimal
import org.apache.ofbiz.entity.GenericValue
import org.apache.ofbiz.entity.util.EntityListIterator

def linesSource = context.invoiceLines ?: context.findLines?.listIt
List<GenericValue> lines = []

if (linesSource) {
    if (linesSource instanceof EntityListIterator) {
        EntityListIterator eli = (EntityListIterator) linesSource
        try {
            GenericValue row
            while ((row = eli.next()) != null) {
                lines.add(row)
            }
        } finally {
            try { eli.close() } catch (ignored) {}
        }
        if (context.findLines) {
            context.findLines.listIt = null       // ← MUY IMPORTANTE
            context.findLines.list   = lines      // ← opcional, por comodidad
        }
    } else if (linesSource instanceof List) {
        lines = (List<GenericValue>) linesSource
    } else if (linesSource.metaClass.respondsTo(linesSource, "getCompleteList")) {
        try {
            lines = (List<GenericValue>) linesSource.getCompleteList()
        } finally {
            if (linesSource instanceof Closeable) {
                try { linesSource.close() } catch (ignored) {}
            }
        }
        if (context.findLines) {
            context.findLines.listIt = null
            context.findLines.list   = lines
        }
    }
}

context.invoiceLines = lines

BigDecimal ZERO = BigDecimal.ZERO
BigDecimal totalNet = ZERO
BigDecimal totalWithVat = ZERO

for (GenericValue gv : lines) {
    totalNet     = totalNet.add(gv.getBigDecimal("lineNetAmount") ?: ZERO)
    totalWithVat = totalWithVat.add(gv.getBigDecimal("lineTotalAmountWithVat") ?: ZERO)
}

context.totalsList = [[
                              totalsLabel  : "Totals",
                              totalNet     : totalNet,
                              totalWithVat : totalWithVat
                      ]]
