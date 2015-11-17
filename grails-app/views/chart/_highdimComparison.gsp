<div class="highdimComparison">
    <g:each in="${subsets?.commons?.highdim}" var="highdim">
        <div class="plotrow">
	        <div class="plotcolumn">
	        <div class="plotbox">
	            ${highdim.plots[1]}
	        </div>
	        </div>
            <div class="plotcolumn">
            <div class="plotbox">
            <g:if test="highdim.plots[2]">
                ${highdim.plots[2]}
            </g:if>
            </div>
            </div>
        </div>
        <div style="clear: both;"></div>
        <table class="booktable">
        <caption>Comparison for Single Nucleotide Polymorphism (SNP) <code>${highdim.title}</code>.</caption>
        <thead>
            <tr>
                <th>Genotype</th>
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
                        <g:if test="${d.value.collect{it.value}}">
                            <g:formatNumber number="${ 100* ((d.value.get(label) ?: 0) / d.value.collect{it.value}.sum()) }" maxFractionDigits="1" />
                        </g:if>
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
