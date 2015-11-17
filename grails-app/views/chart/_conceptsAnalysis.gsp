<table width="100%" style="margin-bottom: 30px">
    <tbody>
        <g:each in="${concepts}" var="concept">
        <tr>
            <td align="center">
            <g:if test="${concept.value}">
                <hr style="margin-bottom: 30px"/>
                <div class="analysistitle">Analysis of ${concept.value.commons.conceptName}</div>
                <div style="margin-top: -15px; padding-bottom: 10px;">
                    ${concept.value?.commons?.testmessage}<br/>
                    <g:if test="${concept.value?.commons.tstat != null}">
                        With a <i>p-value of ${concept.value?.commons.pvalue}</i> for a <i>T-stat at ${concept.value?.commons.tstat}</i>
                    </g:if>
                    <g:if test="${concept.value?.commons.chisquare != null}">
                        With a <i>p-value of ${concept.value?.commons.pvalue}</i> for a <i>χ² at ${concept.value?.commons.chisquare}</i>
                    </g:if>
                </div>
                <g:render template="${concept.value.commons.type}Comparison" model="${[subsets: concept.value]}"/>
                <g:if test="${!concept.value?.commons.highdim?.empty}">
                    <g:render template="highdimComparison" model="${[subsets: concept.value]}"/>
                </g:if>
            </g:if>
            </td>
        </tr>
        </g:each>
    </tbody>
</table>