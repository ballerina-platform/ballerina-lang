package mb;

public type BrokerURLConfig {
    string host = "localhost",
    int port = 5672,
    string clientID = "ballerina",
    string virtualHost = "default",
};

function generateBrokerURL(BrokerURLConfig config) returns string {
    return "amqp://admin:admin@" + config.clientID + "/" + config.virtualHost
        + "?brokerlist='tcp://" + config.host + ":" + config.port + "'";
}
