
<div class="highdimComparison">
    <g:each in="${subsets?.commons?.highdim}" var="highdim">
        <g:if test="${highdim.plots != null}">
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
        </g:if>
        <g:if test="${highdim.data == null}">
            <div>No data of type ${highdim.dataTypeDescription} found for ${highdim.concept}.</div>
            <g:if test="${highdim.filters.size() > 0}">
                <g:each in="${highdim.filters}" var="filter">
                    <table>
                        <caption>Filter:</caption>
                        <g:each in="${filter}" var="p">
                            <tr>
                            <td>${p.key}:</td>
                            <td>${p.value}</td>
                            </tr>
                        </g:each>
                    </table>
                </g:each>
            </g:if>
        </g:if>
        <g:else>
        <table class="booktable">
        <caption>Comparison for <code>${highdim.title}</code> (${highdim.dataTypeDescription}).</caption>
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
        </g:else>
    </g:each>
</div>
