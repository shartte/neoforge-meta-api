<#-- @ftlvariable name="versions" type="net.neoforged.meta.db.SoftwareComponentVersionDao.Summary[]" -->
<#import "page.ftlh" as page>

<@page.layout title="Component Versions - NeoForge Meta API">
    <section>
        <h2>Component Versions</h2>

        <p>Showing ${versions?size} component versions.</p>

        <table id="componentVersions" class="display">
            <thead>
            <tr>
                <th>Version</th>
                <th>Warnings</th>
                <th>Artifacts</th>
                <th>Released</th>
                <th>Discovered</th>
                <th>Repository</th>
            </tr>
            </thead>
            <tbody>
            <#list versions as version>
                <tr>
                    <td><a href="/ui/components/${groupId?url}/${artifactId?url}/versions/${version.version()?url}">${version.version()}</a></td>
                    <td>${version.warningCount()}</td>
                    <td>${version.artifactCount()}</td>
                    <td>${version.released()}</td>
                    <td>${version.discovered()}</td>
                    <td>${version.repository()}</td>
                </tr>
            </#list>
            </tbody>
        </table>

        <p style="margin-top: 2rem;">
            <a href="/" class="btn btn-secondary">Back to Home</a>
        </p>
    </section>

    <script>
        document.addEventListener('DOMContentLoaded', function () {
            jQuery("#componentVersions").DataTable({
                paging: false,
                order: [[3, 'desc']],
                fixedHeader: true,
                columnControl: [
                    {
                        target: 0,
                        content: ['order', ['searchList']]
                    },
                    {
                        target: 1,
                        content: ['search']
                    }
                ],
                ordering: {
                    indicators: false,
                    handler: false
                }
            })
        });
    </script>
</@page.layout>
