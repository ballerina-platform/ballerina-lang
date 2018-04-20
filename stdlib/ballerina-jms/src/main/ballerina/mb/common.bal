
public type BrokerURLConfig {
    string username = "admin",
    string password = "admin",
    string host = "localhost",
    int port = 5672,
    string clientID = "ballerina",
    string virtualHost = "default",
};

function generateBrokerURL(BrokerURLConfig config) returns string {
    return "amqp://" + config.username + ":" + config.password + "@" + config.clientID + "/" + config.virtualHost
        + "?brokerlist='tcp://" + config.host + ":" + config.port + "'";
}
