<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin"/>
    <title>Create Client Applications</title>
</head>

<body>
<div class="body">
    <h1>Create New Client Application</h1>

    <form action="_ACTION_PLACE_HOLDER_" class="adm-frm">

        <div class="adm-input-group">
            <label for="mgr-oauth-client-id">Client ID</label>
            <input type="text" name="mgr-oauth-client-id" id="mgr-oauth-client-id" required maxlength="128">
        </div>

        <div  class="adm-input-group">
            <label for="mgr-oauth-client-id">Client Secret</label>
            <input type="text" name="mgr-oauth-client-id" id="mgr-oauth-client-secret" required maxlength="512">
        </div>

        <div  class="adm-input-group">
            <label>OAuth Grant Type </label>
            <div><input type="checkbox" value="grant-auth-code"> Auth Code</div>
            <div><input type="checkbox" value="grant-implicit-grant"> Implicit Grant</div>
            <div><input type="checkbox" value="grant-password"> Password</div>
            <div><input type="checkbox" value="grant-refresh-token"> Refresh Grant</div>
        </div>

        <div class="adm-input-group">
            <label for="mgr-oauth-redirect-uri">Redirect URI</label>
            <input type="text" name="mgr-oauth-client-id" id="mgr-oauth-redirect-uri" maxlength="2083">
            <button>Add</button>
        </div>

        <div class="adm-toolbar-btm">
            <input type="submit" value="Create">
        </div>
    </form>

</div>
</body>
</html>
