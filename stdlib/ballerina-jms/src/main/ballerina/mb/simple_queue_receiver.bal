package ballerina.mb;

import ballerina/jms;
import ballerina/log;

public type SimpleQueueReceiver object {

    public {
        SimpleQueueListenerEndpointConfiguration config;
    }

    private {
        jms:Connection? connection;
        jms:Session? session;
        jms:QueueConsumer? consumer;
    }

    public function init(SimpleQueueListenerEndpointConfiguration config) {
        self.config = config;
        jms:Connection conn = new({
                initialContextFactory: "wso2mbInitialContextFactory",
                providerUrl: generateBrokerURL(config),
                connectionFactoryName: config.connectionFactoryName,
                properties: config.properties
            });
        connection = conn;

        jms:Session newSession = new(conn, {
                acknowledgementMode:config.acknowledgementMode
            });
        session = newSession;

        jms:QueueConsumer queueConsumer = new;
        jms:QueueConsumerEndpointConfiguration consumerConfig = {
            session:newSession,
            queueName:config.queueName,
            messageSelector:config.messageSelector
        };
        queueConsumer.init(consumerConfig);
        consumer = queueConsumer;
    }

    public function register (typedesc serviceType) {
        match (consumer) {
            jms:QueueConsumer c => {
                c.register(serviceType);
            }
            () => {
                error e = {message:"Queue consumer cannot be nil"};
                throw e;
            }
        }
    }

    public function start () {
    }

    public function getClient() returns QueueConsumerConnector {
        match (consumer) {
            jms:QueueConsumer c => { return new QueueConsumerConnector(c.getClient()); }
            () => {
                error e = {message:"Queue consumer cannot be nil"};
                throw e;
            }
        }
    }

    public function stop () {
    }

    public function createTextMessage(string message) returns (Message|Error) {
        match (session) {
            jms:Session s => {
                var result = s.createTextMessage(message);
                match (result) {
                    jms:Message m => return new Message(m);
                    Error err => return err;
                }
            }
            () => {
                error e = {message:"Session cannot be null"};
                throw e;
            }
        }
    }
};

public type SimpleQueueListenerEndpointConfiguration {
    string username = "admin",
    string password = "admin",
    string host = "localhost",
    int port = 5672,
    string clientID = "ballerina",
    string virtualHost = "default",
    string connectionFactoryName = "ConnectionFactory",
    string acknowledgementMode = "AUTO_ACKNOWLEDGE",
    string messageSelector,
    map properties,
    string queueName,
};

public type QueueConsumerConnector object {

    public {
        jms:QueueConsumerConnector helper;
    }

    public new(helper) {}

    public function acknowledge (Message message) returns Error|() {
        return helper.acknowledge(message.getJMSMessage());
    }

    public function receive (int timeoutInMilliSeconds = 0) returns Message|Error|() {
        var result = helper.receive(timeoutInMilliSeconds = timeoutInMilliSeconds);
        match (result) {
            jms:Message m => {
                return new Message(m);
            }
            jms:Error e => return e;
            () => {}
        }
    }
};

