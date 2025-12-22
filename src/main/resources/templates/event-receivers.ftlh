<#-- @ftlvariable name="eventReceivers" type="net.neoforged.meta.triggers.EventReceiversService.EventReceiverSummary[]" -->
<#import "page.ftlh" as page>

<@page.layout title="Event Receivers - NeoForge Meta API">
    <section>
        <h2>Event Receivers</h2>

        <table id="eventReceivers" class="display">
            <thead>
            <tr>
                <th>ID</th>
                <th>Type</th>
                <th>Delivery State</th>
                <th>Paused</th>
                <th>Events Delivered</th>
                <th>Event Backlog</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <#list eventReceivers as summary>
                <#assign receiver=summary.receiver() />
                <#assign state=summary.state() />
                <tr>
                    <td><a href="/ui/event-receivers/receiver/${receiver.id?url}">${receiver.id}</a></td>
                    <td>${receiver.deliveryStrategy.class.simpleName}</td>
                    <td>${state.deliveryState}</td>
                    <th>
                        <#if state.paused>
                            <i class="fa-solid fa-pause"></i>
                            <#if state.pausedUntil??>
                                <i class="fa-solid fa-clock" title="${state.pausedUntil}"></i>
                            </#if>
                        <#else>
                            <i class="fa-solid fa-play"></i>
                        </#if>
                    </th>
                    <td>${state.eventsDelivered}</td>
                    <td>${summary.eventBacklog()}</td>
                    <td>
                        <form method="POST" action="/ui/event-receivers/receiver/${receiver.id?url}">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            <#if state.paused>
                            <input type="hidden" name="action" value="resume"/>
                            <button class="btn">Resume</button>
                            <#else>
                            <input type="hidden" name="action" value="pause"/>
                            <button class="btn">Pause</button>
                            </#if>
                        </form>
                    </td>
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
            jQuery("#eventReceivers").DataTable({
                paging: false,
                order: [[0, 'asc']],
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
            });
        });
    </script>
</@page.layout>
