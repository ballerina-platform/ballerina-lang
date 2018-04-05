package ballerina.jms;

public struct Connection {
    ConnectionConnector connector;
    ConnectionConfiguration config;
}

public struct ConnectionConfiguration {
    string initialContextFactory;
    string providerUrl;
    string connectionFactoryName;
    map properties;
}

public function <Connection connection> Connection() {
    connection.connector = {};
}

public function <ConnectionConfiguration config> ConnectionConfiguration() {
    config.initialContextFactory = "wso2mbInitialContextFactory";
    config.providerUrl = "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'";
    config.connectionFactoryName = "ConnectionFactory";
}

public function<Connection connection> init(ConnectionConfiguration config) {
    connection.config = config;
    connection.initEndpoint();
}

public struct ConnectionConnector {}

public native function<Connection connection> initEndpoint();

public native function <Connection connection> register (typedesc serviceType);

public native function <Connection connection> start ();

public function <Connection connection> getClient () returns (ConnectionConnector) {
    return connection.connector;
}

public native function <Connection connection> stop ();
