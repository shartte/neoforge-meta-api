<#-- @ftlvariable name="version" type="net.neoforged.meta.db.BrokenSoftwareComponentVersion" -->
<#-- @ftlvariable name="errors" type="net.neoforged.meta.db.DiscoveryLogMessage[]" -->
<#import "page.ftlh" as page>

<@page.layout title="Broken Versions - NeoForge Meta API">
    <section>
        <h2>Broken Version ${version.groupId}:${version.artifactId}:${version.version}</h2>

        <h3>Summary</h3>
        <table>
            <tbody>
            <tr>
                <th style="width: 200px;">Group ID</th>
                <td>${version.groupId}</td>
            </tr>
            <tr>
                <th style="width: 200px;">Artifact ID</th>
                <td>${version.artifactId}</td>
            </tr>
            <tr>
                <th style="width: 200px;">Version</th>
                <td>${version.version}</td>
            </tr>
            <tr>
                <th style="width: 200px;">First Attempt</th>
                <td>${version.created}</td>
            </tr>
            <tr>
                <th style="width: 200px;">Last Attempt</th>
                <td>${version.lastAttempt}</td>
            </tr>
            <tr>
                <th style="width: 200px;">Attempts</th>
                <td>${version.attempts}</td>
            </tr>
            </tbody>
        </table>

        <h3>Errors</h3>
        <table>
            <tbody>
            <#list errors as error>
                <tr>
                    <td>${error.details}</td>
                </tr>
            </#list>
            </tbody>
        </table>

        <p style="margin-top: 2rem;">
            <a href="/" class="btn btn-secondary">Back to Home</a>
        </p>
    </section>
</@page.layout>
