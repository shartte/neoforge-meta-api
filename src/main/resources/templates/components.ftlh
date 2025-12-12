<#-- @ftlvariable name="components" type="net.neoforged.meta.ui.ComponentModel[]" -->
<#import "page.ftlh" as page>

<@page.layout title="Components - NeoForge Meta API">
    <section>
        <h2>Components</h2>

        <p>Showing ${components?size} components.</p>

        <table id="components" class="display">
            <thead>
            <tr>
                <th>Group</th>
                <th>Artifact</th>
                <th>Versions</th>
                <th>Highest Version</th>
                <th>Highest Release</th>
                <th>Latest Version</th>
                <th>Latest Release</th>
            </tr>
            </thead>
            <tbody>
            <#list components as component>
                <tr>
                    <td>${component.groupId()}</td>
                    <td>${component.artifactId()}</td>
                    <td><a href="/ui/components/${component.groupId()?url}/${component.artifactId()?url}/versions">${component.versions()}</a></td>
                    <td><#if component.highestVersion()??><a href="/ui/components/${component.groupId()?url}/${component.artifactId()?url}/versions/${component.highestVersion()}">${component.highestVersion()}</a><#else>-</#if></td>
                    <td><#if component.highestVersionReleased()??>${component.highestVersionReleased()}<#else>-</#if></td>
                    <td><#if component.latestVersion()??><a href="/ui/components/${component.groupId()?url}/${component.artifactId()?url}/versions/${component.latestVersion()}">${component.latestVersion()}</a><#else>-</#if></td>
                    <td><#if component.latestVersionReleased()??>${component.latestVersionReleased()}<#else>-</#if></td>
                </tr>
            </#list>
            </tbody>
        </table>

        <p style="margin-top: 2rem;">
            <a href="/" class="btn btn-secondary">Back to Home</a>
        </p>
    </section>
</@page.layout>
