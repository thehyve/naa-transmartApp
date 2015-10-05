<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="utilities"/>
    <title>Manage client applications</title>
</head>

<body>
<div class="body">
    <h1>User's connected application settings</h1>

    <p>
        You have granted the following applications access
        to your account.
    </p>

    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>

    <div class="adm-list-toolbar">
        <g:remoteLink before="return confirm('Are you sure you want to revoke all access tokens?');" action="revokeAll">Revoke all</g:remoteLink>
    </div>

    <table class="list">
        <thead>
        <tr>
            <th>#</th>
            <g:sortableColumn property="clientId" title="Client ID" />
            <g:sortableColumn property="tokenType" title="Type" />
            <g:sortableColumn property="expiration" title="Expiry date" />
            <g:sortableColumn property="username" title="Username" />
            <th>&nbsp;</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${tokens}" status="i" var="token">
        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
            <td>${token.id}</td>
            <td>${token.clientId}</td>
            <td>${token.tokenType}</td>
            <td>${token.expiration}</td>
            <td>${token.username}</td>
            <td>
                <g:remoteLink before="return confirm('Are you sure you want to revoke the access token?');" 
                    action="revoke" id="${token.id}">Revoke</g:remoteLink>
            </td>
        </tr>
        </g:each>
        </tbody>
    </table>
</div>
</body>
</html>