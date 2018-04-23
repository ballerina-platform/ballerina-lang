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

endpoint jms:QueueSender queueSender {
    session:jmsSession,
    queueName:"MyQueue"
};

function main(string... args) {
    // Create a Text message.
    match (jmsSession.createTextMessage("Hello from Ballerina")) {
        error e => {
            log:printError("Error occurred while creating message", err = e);
        }

        jms:Message msg => {
            // Send the Ballerina message to the JMS provider.
            queueSender->send(msg) but { error e => log:printError("Error occurred while sending message", err = e) };
        }
    }
}
