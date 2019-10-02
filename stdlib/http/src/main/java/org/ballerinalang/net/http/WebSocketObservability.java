package org.ballerinalang.net.http;

import org.ballerinalang.jvm.observability.ObserveUtils;
import org.ballerinalang.jvm.observability.ObserverContext;
import org.ballerinalang.jvm.observability.metrics.DefaultMetricRegistry;
import org.ballerinalang.jvm.observability.metrics.MetricId;
import org.ballerinalang.jvm.observability.metrics.MetricRegistry;
import org.ballerinalang.jvm.observability.metrics.Tag;
import org.ballerinalang.jvm.observability.metrics.Tags;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.ballerinalang.jvm.observability.ObservabilityConstants.SERVER_CONNECTOR_WEBSOCKET;


/**
 * Providing observability functionality to WebSockets.
 *
 * @since 1.1.0
 */
public class WebSocketObservability {

    //Observability Constants
    private static final String TAG_CONNECTION_ID = "connectionID";
    private static final String TAG_KEY_RESULT = "result";
    private static final String TAG_CLIENT_OR_SERVER = "client_or_server";
    private static final String TAG_SERVICE = "service";
    private static final String TAG_MESSAGE_TYPE = "type";
    private static final String TAG_ERROR_TYPE = "error_type";

    private static final String METRIC_REQUESTS = "requests";
    private static final String METRIC_REQUESTS_DESC = "Number of WebSocket connection requests";

    private static final String METRIC_CONNECTIONS = "connections";
    private static final String METRIC_CONNECTIONS_DESC = "Number of currently active connections";

    private static final String METRIC_MESSAGES_RECEIVED = "messages_received";
    private static final String METRIC_MESSAGES_RECEIVED_DESC = "Number of messages received";

    private static final String METRIC_MESSAGES_SENT = "messages_sent";
    private static final String METRIC_MESSAGES_SENT_DESC = "Number of messages sent";

    public static final String WEBSOCKET_MESSAGE_RESULT_SUCCESS = "success";
    static final String WEBSOCKET_MESSAGE_RESULT_FAILED = "failed";

    private static final String METRIC_ERRORS = "errors";
    private static final String METRIC_ERRORS_DESC = "Number of errors";

    private static final String WEBSOCKET_CLIENT_OR_SERVER_CLIENT = "client";
    private static final String WEBSOCKET_CLIENT_OR_SERVER_SERVER = "server";

    public static final String WEBSOCKET_MESSAGE_TYPE_TEXT = "text";
    public static final String WEBSOCKET_MESSAGE_TYPE_BINARY = "binary";
    public static final String WEBSOCKET_MESSAGE_TYPE_CONTROL = "control";
    public static final String WEBSOCKET_MESSAGE_TYPE_CLOSE = "close";

    static final String WEBSOCKET_ERROR_TYPE_CONNECTION = "connection";
    public static final String WEBSOCKET_ERROR_TYPE_CLOSE = "close";
    public static final String WEBSOCKET_ERROR_TYPE_MESSAGE_SENT = "message_sent";
    static final String WEBSOCKET_ERROR_TYPE_MESSAGE_RECEIVED = "message_received";

    //Observe all WebSocket connection requests
    static void observeRequest(WebSocketOpenConnectionInfo connectionInfo, String result) {
        if (ObserveUtils.isObservabilityEnabled()) {
            ObserverContext observerContext = new ObserverContext();
            observerContext.setConnectorName(SERVER_CONNECTOR_WEBSOCKET);
            try {
                observerContext.addTag(TAG_CONNECTION_ID, connectionInfo.getWebSocketConnection().getChannelId());
            } catch (IllegalAccessException e) {
                //TODO: Handle Exception
            }

            setObserveService(observerContext, connectionInfo);

            observerContext.addTag(TAG_KEY_RESULT, result);
            Map<String, String> tags = observerContext.getTags();
            Set<Tag> allTags = new HashSet<>(tags.size());
            Tags.tags(allTags, observerContext.getTags());

            MetricRegistry metricRegistry = DefaultMetricRegistry.getInstance();

            //Increment requests metric
            metricRegistry.counter(new MetricId(SERVER_CONNECTOR_WEBSOCKET + "_" + METRIC_REQUESTS,
                                                METRIC_REQUESTS_DESC, allTags)).increment();
        }
    }

    //Observe all WebSocket connections
    static void observeConnection(WebSocketOpenConnectionInfo connectionInfo) {
        if (ObserveUtils.isObservabilityEnabled()) {

            ObserverContext observerContext = new ObserverContext();
            observerContext.setConnectorName(SERVER_CONNECTOR_WEBSOCKET);

            try {
                observerContext.addTag(TAG_CONNECTION_ID,
                                       connectionInfo.getWebSocketConnection().getChannelId());
            } catch (IllegalAccessException e) {
                //TODO: handle exception
            }
            setObserveService(observerContext, connectionInfo);

            Map<String, String> tags = observerContext.getTags();
            Set<Tag> allTags = new HashSet<>(tags.size());
            Tags.tags(allTags, observerContext.getTags());

            MetricRegistry metricRegistry = DefaultMetricRegistry.getInstance();

            //Increment current connections metric
            metricRegistry.gauge(new MetricId(SERVER_CONNECTOR_WEBSOCKET + "_" + METRIC_CONNECTIONS,
                                              METRIC_CONNECTIONS_DESC, allTags)).increment();

        }
    }

    //Observe all messages sent (pushed)
    public static void observePush(String type, String result, WebSocketOpenConnectionInfo connectionInfo) {
        if (ObserveUtils.isObservabilityEnabled()) {

            ObserverContext observerContext = new ObserverContext();
            observerContext.setConnectorName(SERVER_CONNECTOR_WEBSOCKET);

            //Define type of message (text, binary, control, clsoe) and result (successful, failed)
            observerContext.addTag(TAG_MESSAGE_TYPE, type);
            observerContext.addTag(TAG_KEY_RESULT, result);

            try {
                observerContext.addTag(TAG_CONNECTION_ID, connectionInfo.getWebSocketConnection().getChannelId());
            } catch (IllegalAccessException e) {
                //TODO: Handle exception
            }
            setObserveService(observerContext, connectionInfo);


            Map<String, String> tags = observerContext.getTags();
            Set<Tag> allTags = new HashSet<>(tags.size());
            Tags.tags(allTags, observerContext.getTags());

            MetricRegistry metricRegistry = DefaultMetricRegistry.getInstance();

            //Increment message sent metric
            metricRegistry.counter(new MetricId(SERVER_CONNECTOR_WEBSOCKET + "_" + METRIC_MESSAGES_SENT,
                                                METRIC_MESSAGES_SENT_DESC, allTags)).increment();
        }
    }

    //Observe all messages received
    static void observeOnMessage(String type, WebSocketOpenConnectionInfo connectionInfo) {
        if (ObserveUtils.isObservabilityEnabled()) {
            ObserverContext observerContext = new ObserverContext();
            observerContext.setConnectorName(SERVER_CONNECTOR_WEBSOCKET);

            //Define type of message (text, binary, control, close)
            observerContext.addTag(TAG_MESSAGE_TYPE, type);

            try {
                observerContext.addTag(TAG_CONNECTION_ID, connectionInfo.getWebSocketConnection().getChannelId());
            } catch (IllegalAccessException e) {
                //TODO: Handle Exception
            }
            setObserveService(observerContext, connectionInfo);

            Map<String, String> tags = observerContext.getTags();
            Set<Tag> allTags = new HashSet<>(tags.size());
            Tags.tags(allTags, observerContext.getTags());

            MetricRegistry metricRegistry = DefaultMetricRegistry.getInstance();

            //Increment messages received metric
            metricRegistry.counter(new MetricId(SERVER_CONNECTOR_WEBSOCKET + "_" + METRIC_MESSAGES_RECEIVED,
                                                METRIC_MESSAGES_RECEIVED_DESC, allTags)).increment();

        }
    }

    //Observe all WebSocket closes
    static void observeClose(WebSocketOpenConnectionInfo connectionInfo) {
        if (ObserveUtils.isObservabilityEnabled()) {
            ObserverContext observerContext = new ObserverContext();
            observerContext.setConnectorName(SERVER_CONNECTOR_WEBSOCKET);

            try {
                observerContext.addTag(TAG_CONNECTION_ID, connectionInfo.getWebSocketConnection().getChannelId());
            } catch (IllegalAccessException e) {
                //TODO: Handle Exception
            }

            setObserveService(observerContext, connectionInfo);

            Map<String, String> tags = observerContext.getTags();
            Set<Tag> allTags = new HashSet<>(tags.size());
            Tags.tags(allTags, observerContext.getTags());

            MetricRegistry metricRegistry = DefaultMetricRegistry.getInstance();

            //Decrement current connections metric
            metricRegistry.gauge(new MetricId(SERVER_CONNECTOR_WEBSOCKET + "_" + METRIC_CONNECTIONS,
                                              METRIC_CONNECTIONS_DESC, allTags)).decrement();
        }
    }

    //Observe all WebSocket errors (not related to message sending/receiving)
    public static void observeError(WebSocketOpenConnectionInfo connectionInfo, String errorType) {
        observeError(connectionInfo, errorType, null);
    }

    //Observe all WebSocket errors
    public static void observeError(WebSocketOpenConnectionInfo connectionInfo, String errorType, String messageType) {
        if (ObserveUtils.isObservabilityEnabled()) {
            ObserverContext observerContext = new ObserverContext();
            observerContext.setConnectorName(SERVER_CONNECTOR_WEBSOCKET);

            try {
                observerContext.addTag(TAG_CONNECTION_ID, connectionInfo.getWebSocketConnection().getChannelId());
            } catch (IllegalAccessException e) {
                //TODO: Handle Exception
            }

            setObserveService(observerContext, connectionInfo);
            observerContext.addTag(TAG_ERROR_TYPE, errorType);

            //If the error is related to sending/receiving a message, set the type of message
            if (messageType != null) {
                observerContext.addTag(TAG_MESSAGE_TYPE, messageType);
            }

            Map<String, String> tags = observerContext.getTags();
            Set<Tag> allTags = new HashSet<>(tags.size());
            Tags.tags(allTags, observerContext.getTags());

            MetricRegistry metricRegistry = DefaultMetricRegistry.getInstance();
            metricRegistry.counter(new MetricId(SERVER_CONNECTOR_WEBSOCKET + "_" + METRIC_ERRORS,
                                                METRIC_ERRORS_DESC, allTags)).increment();
        }
    }

    //Determine whether the current context is server or client and set tag respectively.
    //If server, define the associated service base path
    //If client, define the associated remote URI
    private static void setObserveService(ObserverContext observerContext, WebSocketOpenConnectionInfo connectionInfo) {
        String service = connectionInfo.getService().getBasePath();

        if (service != null) {
            //If base path is set (i.e. server)
            observerContext.addTag(TAG_CLIENT_OR_SERVER, WEBSOCKET_CLIENT_OR_SERVER_SERVER);
            observerContext.addTag(TAG_SERVICE, service);
        } else {
            //if base path is not set (i.e. client)
            observerContext.addTag(TAG_CLIENT_OR_SERVER, WEBSOCKET_CLIENT_OR_SERVER_CLIENT);
            observerContext.addTag(TAG_SERVICE, connectionInfo.getWebSocketEndpoint().getStringValue("url"));
        }
    }

    private WebSocketObservability(){

    }
}
