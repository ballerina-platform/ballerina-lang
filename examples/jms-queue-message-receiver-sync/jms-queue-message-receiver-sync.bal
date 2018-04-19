import ballerina/jms;
import ballerina/log;

// Initialize a JMS connection with the provider.
jms:Connection jmsConnection = new({
        initialContextFactory:"wso2mbInitialContextFactory",
        providerUrl:"amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'"
    });

// Initialize a JMS session on top of the created connection.
jms:Session jmsSession = new(jmsConnection, {
        acknowledgementMode:"AUTO_ACKNOWLEDGE"
    });

// Initialize a queue receiver on top of the the created sessions.
endpoint jms:QueueReceiver queueReceiver {
    session:jmsSession,
    queueName:"MyQueue"
};

function main(string... args) {
    // Wait for the message to be received by the JMS provider. If the message is not received within 5
    // seconds, it times out.
    var result = queueReceiver->receive(timeoutInMilliSeconds = 5000);

    match result {
        jms:Message msg => {
        // If the message is received, this block is executed.
            log:printInfo("Message received " + check msg.getTextMessageContent());
        }
        () => {
            // If the message is not received within 5 seconds, this block is executed.
            log:printInfo("Message not received");
        }
        jms:Error err => {
            // If an error occurs, this block is executed.
            log:printInfo("Error receiving message. " + err.message);
        }
    }
}
