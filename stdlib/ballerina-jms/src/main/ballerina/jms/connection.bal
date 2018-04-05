package ballerina.jms;

public type Connection object {
    public {
        ConnectionConfiguration config;
    }

    public new(config) {
        createConnection();
    }

    public native function createConnection();

    public native function start ();

    public native function stop ();
};

public type ConnectionConfiguration {
    string initialContextFactory = "wso2mbInitialContextFactory";
    string providerUrl = "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'";
    string connectionFactoryName = "ConnectionFactory";
    map properties;
};
