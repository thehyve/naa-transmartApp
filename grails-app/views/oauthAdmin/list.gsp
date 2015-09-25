<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin"/>
    <title>Manage Client Applications</title>
</head>

<body>
    <div class="body">
        <h1>Connected Client Applications</h1>

        <p>
            Managed connected applications.
        </p>

        <div class="adm-list-toolbar">
            <a href="create">Create Application Client</a>
        </div>

        <hr style="margin: 10px 0 10px 0;">

        <table>
            <thead>
            <tr>
                <th>#</th>
                <th>Client ID</th>
                <th>Client Secret</th>
                <th>&nbsp;</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>1</td>
                <td>r-client</td>
                <td>r-client</td>
                <td>
                    <a href="edit">Edit</a>
                    <a href="delete">Delete</a>
                </td>
            </tr>
            <tr>
                <td>2</td>
                <td>r-client</td>
                <td>r-client</td>
                <td>
                    <a href="edit">Edit</a>
                    <a href="#delete">Delete</a>
                </td>
            </tr>
            <tr style="background-color: #CDCDCD;">
                <td>3</td>
                <td>r-client</td>
                <td>r-client</td>
                <td>
                    <a href="view">View</a>
                </td>
            </tr>
            <tr>
                <td>4</td>
                <td>r-client</td>
                <td>r-client</td>
                <td>
                    <a href="edit">Edit</a>
                    <a href="#delete">Delete</a>
                </td>
            </tr>
            </tbody>
        </table>

        <p>
            <h3>Notes:</h3>
            Clients in grey texts are configured in Config.grails. To configure it, you have to edit it directly
            in the file.
        </p>
    </div>
</body>
</html>
