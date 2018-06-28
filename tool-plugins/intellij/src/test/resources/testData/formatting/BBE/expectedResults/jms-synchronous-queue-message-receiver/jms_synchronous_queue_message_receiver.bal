import ballerina/jms;
import ballerina/log;

// This initializes a JMS connection with the provider.
jms:Connection jmsConnection = new({
        initialContextFactory: "bmbInitialContextFactory",
        providerUrl: "amqp://admin:admin@carbon/carbon"
            + "?brokerlist='tcp://localhost:5672'"
    });

// This initializes a JMS session on top of the created connection.
jms:Session jmsSession = new(jmsConnection, {
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    });

// This initializes a queue receiver on top of the created sessions.
endpoint jms:QueueReceiver queueReceiver {
    session: jmsSession,
    queueName: "MyQueue"
};

function main(string... args) {
    // This keeps the JMS session alive until the message is received by the JMS provider. If the message is not received within five
    // seconds, the session times out.
    var result = queueReceiver->receive(timeoutInMilliSeconds = 5000);

    match result {
        jms:Message msg => {
            // This is executed if the message is received.
            match (msg.getTextMessageContent()) {
                string messageText => log:printInfo("Message : " + messageText);
                error e => log:printError("Error occurred while reading"
                        + "message", err = e);
            }
        }
        () => {
            // This is executed if the message is not received within five seconds.
            log:printInfo("Message not received");
        }
        error err => {
            // This is executed if an error occurs.
            log:printInfo("Error receiving message. " + err.message);
        }
    }
}
