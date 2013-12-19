<h2>Technical Information</h2>
<div class="message">
    <strong>Message:</strong> ${exception.message?.encodeAsHTML()} <br />
    <strong>Caused by:</strong> ${exception.cause?.message?.encodeAsHTML()} <br />
    <strong>Class:</strong> ${exception.className} <br />
    <strong>At Line:</strong> ${exception.lineNumber} <br />
    <g:if test="${exception.codeSnippet}">
        <strong>Code Snippet:</strong><br />
        <div class="snippet">
            <g:each var="cs" in="${exception.codeSnippet}">
                ${cs?.encodeAsHTML()}<br />
            </g:each>
        </div>
    </g:if>
</div>
<hr />
<h2>Stacktrace</h2>
<div class="stack">
    <g:each in="${exception.stackTraceLines}">${it.encodeAsHTML()}<br/></g:each>
</div>