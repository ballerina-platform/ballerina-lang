import ballerina/jms;
import ballerina/log;

// Initialize a JMS connection with the provider
jms:Connection jmsConnection = new({
        initialContextFactory:"wso2mbInitialContextFactory",
        providerUrl:"amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'"
    });

// Initialize a JMS session on top of the created connection
jms:Session jmsSession = new(jmsConnection, {
        acknowledgementMode:"AUTO_ACKNOWLEDGE"
    });

// Initialize a queue receiver on top of the the created sessions
endpoint jms:QueueReceiver queueReceiver {
    session:jmsSession,
    queueName:"MyQueue"
};

function main(string... args) {
    // Receive a message from the JMS provider.
    var result = queueReceiver -> receive(timeoutInMilliSeconds = 5000);

    match result {
        jms:Message msg => {
            log:printInfo("Message received " + check msg.getTextMessageContent());
        }
        () => {
            log:printInfo("Message not received");
        }
        jms:Error err => {
            log:printInfo("Error receiving message. " + err.message);
        }
    }
}
