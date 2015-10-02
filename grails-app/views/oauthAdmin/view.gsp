<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin"/>
    %{--include js lib for box plot dynamically--}%
    <r:require modules="oauthAdmin"/>
    <title>View client applications</title>
</head>

<body>
    <div class="body">
        <h1>View client application</h1>

        <div class="adm-frm">
            <div class="adm-input-group">
                <label>Client ID</label>
                <span class="adm-view-val">${client.clientId}</span>
            </div>

            <div class="adm-input-group">
                <label>Client secret</label>
                <span class="adm-view-val">${client.clientSecret}</span>
            </div>

            <div class="adm-input-group">
                <label>OAuth grant types</label>
                <ol class="adm-view-val" style="display: inline-block;">
                <g:each in="${client.authorizedGrantTypes}" status="i" var="grantType">
                    <li>${grantType}</li>
                </g:each>
                </ol>
            </div>

            <div class="adm-input-group">
                <label>Redirect URIs</label>
                <ol class="adm-view-val" style="display: inline-block;">
                <g:each in="${client.redirectUris}" status="i" var="uri">
                    <li><code>${uri}</code></li>
                </g:each>
                </ol>
            </div>
        </div>
    </div>
</body>
</html>
