import ballerina/jms;
import ballerina/log;

// Initialize a JMS connection with the provider.
jms:Connection jmsConnection = new({
        initialContextFactory:"wso2mbInitialContextFactory",
        providerUrl:"amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'"
    });

// Initialize a JMS session on top of the created connection.
jms:Session jmsSession = new(jmsConnection, {
        acknowledgementMode:"CLIENT_ACKNOWLEDGE"
    });

// Initialize a Queue sender on top of the the created session.
endpoint jms:QueueReceiver queueReceiver {
    session:jmsSession,
    queueName:"MyQueue"
};

public function main(string[] args) {
    // Waits for 1 seconds for the message to be received by the JMS provider.
    var result = queueReceiver -> receive(timeoutInMilliSeconds = 1000);

    match result {
        jms:Message msg => {
            // If the message is received, this block is executed. It checks for client acknowledgement.
            log:printInfo("Message received " + check msg.getTextMessageContent());
            check queueReceiver -> acknowledge(msg);
        }
        () => {
            // If the message is not received within 1 second, this block is executed.
            log:printInfo("Message not received");
        }
        jms:Error err => {
            // If an error occurs, this block is executed.
            log:printInfo("Error receiving message. " + err.message);
        }
    }
}
