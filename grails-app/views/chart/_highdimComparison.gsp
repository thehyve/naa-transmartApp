<div class="highdimComparison">
    <g:each in="${subsets?.commons?.highdim}" var="highdim">
        ${highdim.plot}
        <table class="booktable">
        <thead>
            <tr>
                <th>Category</th>
                <g:each in ="${highdim.series}" var="element">
                    <th>${element} (n)</th>
                    <th>${element} (%)</th>
                </g:each>
            </tr>
        </thead>
        <tbody>
        <g:each in="${highdim.labels}" var="label">
            <tr>
                <td>${label}</td>
                <g:each in="${highdim.data}" var="d">
                    <td class="numeric">${d.value.get(label)}</td>
                    <td class="numeric">
                        <g:formatNumber number="${100* (d.value.get(label) / d.value.collect{it.value}.sum())}" maxFractionDigits="1" />
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