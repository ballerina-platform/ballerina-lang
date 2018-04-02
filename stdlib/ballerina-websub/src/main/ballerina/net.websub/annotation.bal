package ballerina.net.websub;

///////////////////////////
/// Service Annotations ///
///////////////////////////
@Description {value:"Configuration for a WebSubSubscriber service"}
@Field {value:"endpoints: Array of endpoints the service would be attached to"}
@Field {value:"basePath: Path of the WebSubSubscriber service"}
@Field {value:"subscribeOnStartUp: Whether a subscription request is expected to be sent on start up"}
@Field {value:"hub: The hub at which the subscription should be registered."}
@Field {value:"topic: The topic to which this WebSub subscriber (callback) should be registered."}
@Field {value:"callback: The callback URL (part of this service) at which notifications are expected."}
@Field {value:"leaseSeconds: The period for which the subscription is expected to be active."}
@Field {value:"secret: The secret to be used for authenticated content distribution."}
public struct SubscriberServiceConfiguration {
    SubscriberServiceEndpoint[] endpoints;
    string basePath;
    boolean subscribeOnStartUp;
    string hub;
    string topic;
    int leaseSeconds;
    string secret;
}

@Description {value:"WebebSubSubscriber Configuration for service"}
public annotation <service> SubscriberServiceConfig SubscriberServiceConfiguration;
