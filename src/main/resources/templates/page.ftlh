<#--
  Page layout macro for NeoForge Meta API

  Usage:
    <#import "page.ftlh" as page>
    <@page.layout title="Page Title">
      <section>
        <h2>Your content here</h2>
      </section>
    </@page.layout>
-->

<#macro layout title="NeoForge Meta API">
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${title}</title>

    <link rel="stylesheet" href="/app.css">
    <link rel="stylesheet" href="/webjars/datatables.net-dt/2.3.5/css/dataTables.dataTables.min.css">
    <link rel="stylesheet" href="/webjars/datatables.net-fixedheader-dt/4.0.5/css/fixedHeader.dataTables.min.css">
    <link rel="stylesheet" href="/webjars/datatables.net-columncontrol-dt/1.1.1/css/columnControl.dataTables.min.css">
    <link rel="stylesheet" href="/webjars/font-awesome/7.1.0/css/all.min.css">
</head>
<body>
    <header>
        <h1><a href="/">NeoForge Meta API</a></h1>
        <nav>
            <#if user??>
                <div class="user-info">
                    <span>Welcome, ${user.name!user.principal}</span>
                    <#if roles?? && roles?size gt 0>
                        <span>
                            <#list roles as role>
                                <span class="role-badge <#if role == 'admin'>admin</#if>">${role}</span>
                            </#list>
                        </span>
                    </#if>
                    <form action="/ui/logout" method="POST">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                        <button class="btn btn-secondary">Logout</button>
                    </form>
                </div>
            <#else>
                <a href="/oauth2/authorization/dex" class="btn">Login</a>
            </#if>
        </nav>
    </header>

    <main>
        <#if successMessage??>
            <div class="alert alert-success">${successMessage}</div>
        </#if>
        <#if errorMessage??>
            <div class="alert alert-danger">${errorMessage}</div>
        </#if>
        <#nested>
    </main>


    <script src="/webjars/jquery/3.7.1/dist/jquery.slim.min.js"></script>
    <script src="/webjars/datatables.net/2.3.5/js/dataTables.min.js"></script>
    <script src="/webjars/datatables.net-fixedheader/4.0.5/js/dataTables.fixedHeader.min.js"></script>
    <script src="/webjars/datatables.net-columncontrol/1.1.1/js/dataTables.columnControl.min.js"></script>
    <script src="/app.js"></script>
</body>
</html>
</#macro>
