<script type="text/javascript">
(function () {
  function bindDblClick(tableId, invoiceIdColIndex) {
    var t = document.getElementById(tableId) || document.getElementById(tableId + "_table");
    if (!t) return;
    var rows = t.querySelectorAll("tbody tr");
    for (var i = 0; i < rows.length; i++) {
      rows[i].style.cursor = "pointer";
      rows[i].title = "Double-click to view details";
      rows[i].addEventListener("dblclick", function () {
        // toma el valor de la 1ª columna (invoiceId) —ajusta índice si cambias el orden
        var cell = this.cells[invoiceIdColIndex] || this.querySelector("td:nth-child(" + (invoiceIdColIndex + 1) + ")");
        var txt = cell ? cell.textContent.trim() : "";
        if (!txt) return;
        // por si la celda es un link:
        var a = cell.querySelector("a");
        if (a && a.textContent) txt = a.textContent.trim();
        // navega al detalle (cabecera)
        window.location.href = "<@ofbizUrl>viewInvoiceDetail</@ofbizUrl>?invoiceId=" + encodeURIComponent(txt);
      });
    }
  }
  // Los forms se llaman así por defecto
  bindDblClick("ListInvoicesForm_table", 0);
  bindDblClick("ListInvoicesFormFull_table", 0);
})();
</script>
