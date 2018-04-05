package ballerina.jms;

public type Connection object {
    public {
        ConnectionConnector connector;
        ConnectionConfiguration config;
    }

    public new() {
        connector = new;
    }

    public function init(ConnectionConfiguration config) {
        self.config = config;
        initEndpoint();
    }

    public native function initEndpoint();

    public native function register (typedesc serviceType);

    public native function start ();

    public function getClient () returns (ConnectionConnector) {
        return connector;
    }

    public native function stop ();
}

public type ConnectionConfiguration {
    string initialContextFactory = "wso2mbInitialContextFactory";
    string providerUrl = "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'";
    string connectionFactoryName = "ConnectionFactory";
    map properties;
}

public type ConnectionConnector object {}
