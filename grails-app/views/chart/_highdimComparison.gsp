<div class="highdimComparison">
    <g:each in="${subsets?.commons?.highdim}" var="highdim">
        ${highdim.plot}
        <table class="booktable">
        <thead>
            <tr>
                <th>Category</th>
                <g:each in ="${highdim.colnames}" var="colname">
                    <th>${colname} (n)</th>
                    <th>${colname} (%)</th>
                </g:each>
            </tr>
        </thead>
        <tbody>
        <g:each in="${highdim.rownames}" var="rowname">
            <tr>
                <td>${rowname}</td>
                <g:each in="${highdim.data}" var="d">
                    <td class="numeric">${d.value.get(rowname)}</td>
                    <td class="numeric">
                        <g:formatNumber number="${100* (d.value.get(rowname) / d.value.collect{it.value}.sum())}" maxFractionDigits="1" />
                    </td>
                </g:each>
            </tr>
        </g:each>
        </tbody>
        <tfoot>
        <tr>
            <td>Total</td>
            <g:each in="${highdim.data}" var="d">
                <td class="numeric">${d.value.collect{it.value}.sum()}</td>
                <td></td>
            </g:each>
        </tr>
        </tfoot>
        </table>
    </g:each>
</div>