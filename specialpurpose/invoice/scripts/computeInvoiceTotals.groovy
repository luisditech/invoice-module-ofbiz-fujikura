/*
 * Calcula totales de las líneas y deja en contexto:
 *  - invoiceLines: List<GenericValue> (no iterador)
 *  - totalsList  : List<Map> con una fila de totales para el form InvoiceLinesTotals
 */
def it = context.findLines?.listIt
List lines = []
if (it) {
    lines = it.getCompleteList()
    it.close()
}

// Exponer la lista para el form ListInvoiceLines
context.invoiceLines = lines

// Totales
BigDecimal totalNet = BigDecimal.ZERO
BigDecimal totalWithVat = BigDecimal.ZERO

lines.each { gv ->
    def net = gv.get("lineNetAmount")
    def with = gv.get("lineTotalAmountWithVat")
    if (net != null) totalNet = totalNet.add(gv.getBigDecimal("lineNetAmount"))
    if (with != null) totalWithVat = totalWithVat.add(gv.getBigDecimal("lineTotalAmountWithVat"))
}

// Fila para el form de totales
context.totalsList = [[
                              totalsLabel : "Total Net Amount:",
                              totalNet    : totalNet,
                              totalWithVat: totalWithVat
                      ]]
