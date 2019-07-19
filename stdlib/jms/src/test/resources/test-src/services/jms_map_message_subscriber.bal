import ballerinax/java.jms;
import ballerina/io;

string msgVal = "";
jms:Connection conn2 = new ({
        initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616"
    });

jms:Session jmsSession2 = new (conn2, {
    acknowledgementMode: "AUTO_ACKNOWLEDGE"
});

listener jms:TopicListener topicSubscriber2 = new(jmsSession2, "testMapMessageSubscriber");

service jmsListener2 on topicSubscriber2 {

    resource function onMessage(jms:TopicSubscriberCaller subscriber, jms:Message message) {
        var messageRetrieved = <@untainted> message.getPayload();
        if (messageRetrieved is map<any>) {
             msgVal += io:sprintf("%s", <int>messageRetrieved["a"]);
             msgVal += <string>messageRetrieved["b"];
             msgVal += io:sprintf("%s", <boolean>messageRetrieved["c"]);
             msgVal += io:sprintf("%s", <float>messageRetrieved["d"]);
             msgVal += io:sprintf("%s", <byte[]>messageRetrieved["e"]);
        }
        io:println(msgVal);
    }
}

function getMsgVal() returns string {
    return msgVal;
}

