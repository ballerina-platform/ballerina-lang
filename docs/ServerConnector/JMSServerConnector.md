#JMS Server Connector
JMS Server connector can be used to listen to a topic/queue in a JNDI based JMS provider. High level view on how jms server connector works is given below,

![JMS_Server_Connector](../images/jms_server_connector.png)

## How to write a JMS Service ?
###Step 1
Create a service with unique name.

Example : 
```
service jmsService {
}
```
###Step 2
Add a service level annotation named “Source” and add the key-value pairs to specify the particular topic/queue that you wish to listen. Following section describes the each key that can be used with the jms service.

Key | Description | Required | Expected Values | Default Value
------------ | ------------- | ---------- | ----------- | ----------
protocol | The protocol this particular service belongs to. | Yes | jms | -
factoryInitial | The JNDI initial context factory class. The class must implement the java.naming.spi.InitialContextFactory interface. | Yes | A valid class name depending on the jms provider | -
providerUrl | The URL/ file path of  of the JNDI provider. | Yes | A valid url/ path for the JNDI provider | -
connectionFactoryJNDIName | The JNDI name of the connection factory. | Yes | A valid jndi name of the connection factory. | -
connectionFactoryType | The type of the connection factory. | no | queue, topic | queue
destination | The JNDI name of the destination. | no | A valid jndi name of the destination | service name
sessionAcknowledgement | The JMS session acknowledgment mode. | no | AUTO_ACKNOWLEDGE, CLIENT_ACKNOWLEDGE, DUPS_OK_ACKNOWLEDGE, SESSION_TRANSACTED | AUTO_ACKNOWLEDGE
connectionUsername | The JMS connection username | no | - | -
connectionPassword | The JMS connection password | no | - | -
subscriptionDurable | Whether subscription is durable or not. | no | true, false | false
durableSubscriberClientID | The ClientId parameter when using durable subscriptions. | Required if subscriptionDurable is specified as "true" | - | -
durableSubscriberName | The name of the durable subscriber. | Required if subscriptionDurable is specified as "true". | - | -
retryInterval | The retry interval if the jms connection cannot be established at the beginning or if the jms connection is lost in the middle. (In milliseconds) | no | A valid long value. | 10000
maxRetryCount | Maximum retry count, if the connection cannot be established, or if the jms connection is lost in the middle. | no | A valid integer value. | 5

Example : 

```
@Source (
protocol = "jms",
destination = "ballerina",
connectionFactoryJNDIName = "QpidConnectionFactory",
factoryInitial = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory",
providerUrl = "jndi.properties",
connectionFactoryType = "queue",
sessionAcknowledgement = "CLIENT_ACKNOWLEDGE")
service jmsMBService {
}
```

###Step 3
Add a resource to the JMS service. This is required as whenever a message comes from
 the jms provider to a specific JMS service, it will be delivered to this particular resource. Please note that it is
  mandatory requirement to have only single resource in JMS service.

Example :

```
@Source (
protocol = "jms",
destination = "ballerina",
connectionFactoryJNDIName = "QpidConnectionFactory",
factoryInitial = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory",
providerUrl = "jndi.properties",
connectionFactoryType = "queue",
sessionAcknowledgement = "CLIENT_ACKNOWLEDGE")
service jmsService {
    resource onMessage (message m) {
        // ProcessMessage
    }
```

Now you will have a JMS service that can be deployed to the ballerina server. Following is a sample JMS Service.

```
import ballerina.net.jms;

@Source (
protocol = "jms",
destination = "ballerina",
connectionFactoryJNDIName = "QpidConnectionFactory",
factoryInitial = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory",
providerUrl = "jndi.properties",
connectionFactoryType = "queue",
sessionAcknowledgement = "CLIENT_ACKNOWLEDGE")
service jmsService {
    resource onMessage (message m) {
        //Process the message
        jms:acknowledge(m, "SUCCESS");
    }
}
```

##Ballerina Native Functions

####Package ballerina.lang.message

**message:getHeader**  

This function can be used to get the value of the particular jms header.
This function accepts following two parameters.    
1. Relevant message to get the header from.
2. Header name.

Example :
 
```
import ballerina.lang.message
---
string jmsMessageId;
jmsMessageId = message:getHeader(m, "JMS_MESSAGE_ID");
```

**message:getStringPayload**

This function can be used to get the string payload of the jms text message or bytes message. This function accepts relevant ballerina message as the parameter. It will return the relevant string payload of the relevant message

Example :
```
import ballerina.lang.message
---
string stringPayload;
stringPayload = message:getStringPayload(m);
```

**message:getStringValue**

This function can be used to get the string value of specific map key in a map type message. This function accepts following two parameters.
1. Relevant ballerina message to get the string value from
2. Relevant map key value

Example : 

```
import ballerina.lang.message
---
string stringValue;
stringValue = message:getStringValue(m, “count”);
```

####Package ballerina.net.jms
**jms:getMessageType**

This function can be used get the jms message type of the relevant message. This function accepts relevant message as the argument. Return values will be “TextMessage”, “BytesMessage”, “MapMessage” or “ObjectMessage”.

Example :
```
import ballerina.net.jms
---

string messageType;
messageType = jms:getMessageType(m);
```

**jms:acknowledge**

This function can be used acknowledge the message delivery or success, which particular useful in when using the client acknowledgement mode. This function accepts following two parameters,
1. Ballerina message that need to acknowledged 
2. Message delivery status - “SUCCESS” or  “ERROR”

Example :
```
import ballerina.net.jms
---
jms:acknowledge(m, "SUCCESS");
```

**jms:commit**

This function can be used to commit the jms session when using the session acknowledgement mode. This function accepts the only one parameter, which is the relevant message that the user wishes to acknowledge the session with.

Example : 
```
import ballerina.net.jms
---
jms:commit();
```

**jms:rollback**

This function can be used to commit the messages when using the session acknowledgement mode.This function accepts the only one parameters, which is the relevant Ballerina message that the user wishes to rollback the session with.

Example :
```
import ballerina.net.jms
---
jms:rollback();
```


**Note** - Before deploying the jms service it is required to copy the client-libs, provided by the JMS provider to `{Ballerina_HOME}/bre/lib` 