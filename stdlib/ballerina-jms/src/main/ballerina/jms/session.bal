package ballerina.jms;

public struct Session {
    SessionConnector connector;
    SessionConfiguration config;
}

public struct SessionConfiguration {
    Connection connection;
    string acknowledgementMode;
}

public struct SessionConnector {
}

public function <Session session> Session() {
    session.connector = {};
}

public function <SessionConfiguration config> SessionConfiguration() {
    config.acknowledgementMode = "AUTO_ACKNOWLEDGE";
}

public function <Session session> init(SessionConfiguration config) {
    session.config = config;
    ConnectionConnector connectionConnector = config.connection.getClient();
    session.initEndpoint(connectionConnector);
}

native function <Session session> initEndpoint(ConnectionConnector connector);

public function <Session session> register (typedesc serviceType) {
}

public function <Session session> start () {
}

public function <Session session> getClient () returns (SessionConnector) {
    return session.connector;
}

public function <Session session> stop () {
}

public native function<Session session> createTextMessage (string content) returns (Message);
