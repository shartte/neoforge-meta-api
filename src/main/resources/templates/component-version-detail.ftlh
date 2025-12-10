<#-- @ftlvariable name="details" type="net.neoforged.meta.db.SoftwareComponentVersion" -->
<#-- @ftlvariable name="warnings" type="net.neoforged.meta.db.DiscoveryLogMessage[]" -->
<#-- @ftlvariable name="artifacts" type="net.neoforged.meta.db.SoftwareComponentArtifact[]" -->
<#import "page.ftlh" as page>

<@page.layout title="Component Version - NeoForge Meta API">
    <section>
        <h3>Component ${groupId}:${artifactId} Version ${version}</h3>

        <h3>Version Information</h3>
        <table>
            <tbody>
            <tr>
                <th style="width: 200px;">Group ID</th>
                <td>${groupId}</td>
            </tr>
            <tr>
                <th style="width: 200px;">Artifact ID</th>
                <td>${artifactId}</td>
            </tr>
            <tr>
                <th style="width: 200px;">Version</th>
                <td>${version}</td>
            </tr>
            <tr>
                <th style="width: 200px;">Repository</th>
                <td>${details.repository}</td>
            </tr>
            <tr>
                <th style="width: 200px;">Released</th>
                <td>${details.released}</td>
            </tr>
            <tr>
                <th style="width: 200px;">Discovered</th>
                <td>${details.discovered}</td>
            </tr>
            <tr>
                <th style="width: 200px;">Changelog</th>
                <td><#if details.changelog??>${details.changelog.changelog}</#if></td>
            </tr>
            <tr>
                <th style="width: 200px;">Artifacts</th>
                <td>
                    <#if artifacts?has_content>
                        <table>
                            <tr>
                                <th>File</th>
                                <th>Size</th>
                                <th>Last Modified</th>
                                <th>MD5</th>
                                <th>SHA1</th>
                                <th>SHA256</th>
                                <th>SHA512</th>
                            </tr>
                            <#list artifacts as artifact>
                                <tr>
                                    <td><a href="${repository.getDownloadUrl(artifact)}" download>${artifact.classifier!""}.${artifact.extension!""}</a></td>
                                    <td>${artifact.size}</td>
                                    <td>${artifact.lastModified}</td>
                                    <td>${artifact.md5Checksum???then("Y", "N")}</td>
                                    <td>${artifact.sha1Checksum???then("Y", "N")}</td>
                                    <td>${artifact.sha256Checksum???then("Y", "N")}</td>
                                    <td>${artifact.sha512Checksum???then("Y", "N")}</td>
                                </tr>
                            </#list>
                        </table>
                    <#else>
                        No Artifacts
                    </#if>
                </td>
            </tr>
            <#if warnings?has_content>
                <#list warnings as warning>
                    <tr>
                        <#if warning_index == 0>
                            <th style="width: 200px;" rowspan="${warnings?size}">Warnings</th>
                        </#if>
                        <td>${warning.details}</td>
                    </tr>
                </#list>
            <#else>
                <tr>
                    <th style="width: 200px;">Warnings</th>
                    <td>No Warnings</td>
                </tr>
            </#if>
            </tbody>
        </table>

        <p style="margin-top: 2rem;">
            <a href="/" class="btn btn-secondary">Back to Home</a>
        </p>
    </section>
</@page.layout>
