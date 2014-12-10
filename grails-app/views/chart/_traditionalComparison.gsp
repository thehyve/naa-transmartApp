<%@ page import="org.apache.commons.lang.ArrayUtils; org.jfree.data.statistics.Statistics" %>
<table width="80%" style="text-align: center">
    <tbody>
    <tr>
        %{-- This is hardcoded badness. Multiple (>2) cohort selection should work on that --}%
        <td width="50%">
            <img src="${subsets[1]?.conceptBar}" border="0" style="min-height: ${subsets?.commons?.minimalHeight ?: 0}px" />
            <g:render template="detailedStats" model="${[subset: subsets.entrySet().find {it.key == 1}]}"/>
        </td>
        <td width="50%">
            <g:if test="${subsets[2].exists}">
                <img src="${subsets[2]?.conceptBar}" border="0" />
                <g:render template="detailedStats" model="${[subset: subsets.entrySet().find {it.key == 2}]}"/>
            </g:if>
        </td>
    </tr>
    </tbody>
</table>