import ballerina/jms;
import ballerina/log;

// Initialize a JMS connection with the provider.
jms:Connection jmsConnection = new({
    initialContextFactory:"bmbInitialContextFactory",
    providerUrl:"amqp://admin:admin@carbon/carbon"
                + "?brokerlist='tcp://localhost:5672'"
});

// Initialize a JMS session on top of the created connection.
jms:Session jmsSession = new(jmsConnection, {
    acknowledgementMode:"SESSION_TRANSACTED"
});

endpoint jms:QueueSender queueSender {
    session:jmsSession,
    queueName:"MyQueue"
};

function main(string... args) {
    // Message is published within the transaction block.
    transaction {
    // Create a Text message.
        match (jmsSession.createTextMessage("Hello from Ballerina")) {
            error e => {
                log:printError("Error occurred while creating message",
                               err = e);
            }

            jms:Message msg => {
                // Send the Ballerina message to the JMS provider.
                queueSender->send(msg) but {
                    error e => log:printError("Error occurred while sending "
                                              + "message", err = e)
                };
            }
        }
    }
}
