package ballerina.net.websub;

///////////////////////////
/// Service Annotations ///
///////////////////////////
@Description {value:"Configuration for a WebSubSubscriber service"}
@Field {value:"endpoints: Array of endpoints the service would be attached to"}
@Field {value:"basePath: Path of the WebSubSubscriber service"}
@Field {value:"subscribeOnStartUp: Whether a subscription request is expected to be sent on start up"}
@Field {value:"resourceUrl: The resource URL for which discovery will be initiated to identify hub and topic if not
specified."}
@Field {value:"hub: The hub at which the subscription should be registered."}
@Field {value:"topic: The topic to which this WebSub subscriber (callback) should be registered."}
@Field {value:"callback: The callback URL (part of this service) at which notifications are expected."}
@Field {value:"leaseSeconds: The period for which the subscription is expected to be active."}
@Field {value:"secret: The secret to be used for authenticated content distribution."}
@Field {value:"callback: The callback to use when registering, if unspecified host:port/path will be used."}
public struct SubscriberServiceConfiguration {
    SubscriberServiceEndpoint[] endpoints;
    string basePath;
    boolean subscribeOnStartUp;
    string resourceUrl;
    string hub;
    string topic;
    int leaseSeconds;
    string secret;
    string callback;
}

@Description {value:"WebebSubSubscriber Configuration for service"}
public annotation <service> SubscriberServiceConfig SubscriberServiceConfiguration;
