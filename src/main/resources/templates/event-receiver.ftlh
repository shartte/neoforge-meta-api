<#-- @ftlvariable name="receiver" type="net.neoforged.meta.triggers.EventReceiver" -->
<#-- @ftlvariable name="state" type="net.neoforged.meta.db.EventReceiverState" -->
<#import "page.ftlh" as page>

<@page.layout title="Event Receiver - NeoForge Meta API">
    <section>
        <h2>Event Receiver '${receiver.id}'</h2>

        <h3>General Information</h3>
        <table>
            <tbody>
            <tr>
                <th style="width: 200px;" scope="row">ID</th>
                <td>${receiver.id}</td>
            </tr>
            <tr>
                <th style="width: 200px;" scope="row">Compact Events</th>
                <td>${receiver.compactEvents?string('yes', 'no')}</td>
            </tr>
            <tr>
                <th style="width: 200px;" scope="row">Max Batch Size</th>
                <td>${receiver.maxBatchSize}</td>
            </tr>
            <tr>
                <th style="width: 200px;" scope="row">Event Query</th>
                <td>${eventFilterSql}</td>
            </tr>
            <tr>
                <th style="width: 200px;" scope="row">Delivery Strategy</th>
                <td>${receiver.deliveryStrategy.toString()}</td>
            </tr>
            </tbody>
        </table>

        <#if state??>
            <h3>Delivery Status</h3>
            <table>
                <tbody>
                <tr>
                    <th style="width: 200px;" scope="row">Paused</th>
                    <td>
                        <p>
                            <#if state.paused && state.pausedUntil??>
                                <i class="fa fa-pause"></i> yes (until ${state.pausedUntil})
                            <#elseif state.paused>
                                <i class="fa fa-pause"></i> yes
                            <#else>
                                <i class="fa fa-play"></i> no
                            </#if>
                        </p>
                        <form method="POST" action="/ui/event-receivers/receiver/${receiver.id?url}">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            <input type="hidden" name="fromDetails" value="true"/>
                            <#if state.paused>
                                <input type="hidden" name="action" value="resume"/>
                                <button class="btn">Resume</button>
                            <#else>
                                <input type="hidden" name="action" value="pause"/>
                                <button class="btn">Pause</button>
                            </#if>
                        </form>
                        <form method="POST" action="/ui/event-receivers/receiver/${receiver.id?url}">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            <input type="hidden" name="fromDetails" value="true"/>
                            <input type="hidden" name="pauseFor" value="PT1H"/>
                            <input type="hidden" name="action" value="pause"/>
                            <button class="btn">Pause for 1H</button>
                        </form>
                        <form method="POST" action="/ui/event-receivers/receiver/${receiver.id?url}">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            <input type="hidden" name="fromDetails" value="true"/>
                            <input type="hidden" name="pauseFor" value="PT24H"/>
                            <input type="hidden" name="action" value="pause"/>
                            <button class="btn">Pause for 24H</button>
                        </form>
                    </td>
                </tr>
                <tr>
                    <th style="width: 200px;" scope="row">State</th>
                    <td>${state.deliveryState}</td>
                </tr>
                <#if state.deliveryState == 'FAILING'>
                    <tr>
                        <th style="width: 200px;" scope="row">Failing Since</th>
                        <td>${state.failingSince!"unknown"}</td>
                    </tr>
                    <tr>
                        <th style="width: 200px;" scope="row">Consecutive Failures</th>
                        <td>${state.consecutiveFailures!"unknown"}</td>
                    </tr>
                    <tr>
                        <th style="width: 200px;" scope="row">Last Failure Message</th>
                        <td>${state.lastFailureMessage!"unknown"}</td>
                    </tr>
                </#if>
                <tr>
                    <th style="width: 200px;" scope="row">Highest Event ID Observed</th>
                    <td>${state.highestEventIdSeen!"-"}</td>
                </tr>
                <tr>
                    <th style="width: 200px;" scope="row">Last Successful Delivery</th>
                    <td>${state.lastSuccess!"never"}</td>
                </tr>
                <tr>
                    <th style="width: 200px;" scope="row">Last Failed Delivery</th>
                    <td>${state.lastFailure!"never"}</td>
                </tr>
                </tbody>
            </table>
        <#else>
            No State Available
        </#if>

        <p style="margin-top: 2rem;">
            <a href="/" class="btn btn-secondary">Back to Home</a>
        </p>
    </section>
</@page.layout>
