# JMS Server Connector
The JMS Server connector can be used to listen to a topic/queue in a JNDI-based JMS provider. Following is a high-level view on how the JMS Server connector works:

![JMS_Server_Connector](../images/jms_server_connector.png)

## How to write a JMS service

### Step 1: Create the service
Create a service with unique name.

Example: 
```
service jmsService {
}
```
### Step 2: Specify the connection details

Add a service-level annotation named “jms:JMSSource”, and add the key-value pairs of these two mandatory properties to specify the JMS connection details.
The following table describes each key that can be used with the JMS service.

Key | Description | Expected Values | Default Value
------------ | ------------- | ----------- | ----------
factoryInitial | The JNDI initial context factory class. The class must implement the java.naming.spi.InitialContextFactory interface. | A valid class name depending on the JMS provider | -
providerUrl | The URL/ file path of the JNDI provider | A valid URL/ path for the JNDI provider | -

Example: 

```
import ballerina.net.jms;

@jms:JMSSource {
factoryInitial : "org.wso2.andes.jndi.PropertiesFileInitialContextFactory",
providerUrl : "/Users/Akalanka/test/jndi.properties"}
```

The following set of parameters are optional and can be provided to the JMS provider along with any other vendor specific properties by adding an annotation named "jms:ConnectionProperty"

Key | Description | Required | Expected Values | Default Value
------------ | ------------- | ---------- | ----------- | ----------
connectionFactoryType | The type of the connection factory | no | queue, topic | queue
connectionFactoryJNDIName | The JNDI name of the connection factory | no | A valid JNDI name of the connection factory. | -
destination | The JNDI name of the destination | no | A valid JNDI name of the destination | service name
sessionAcknowledgement | The JMS session acknowledgment mode | no | AUTO_ACKNOWLEDGE, CLIENT_ACKNOWLEDGE, DUPS_OK_ACKNOWLEDGE, SESSION_TRANSACTED | AUTO_ACKNOWLEDGE
connectionUsername | The JMS connection username | no | - | -
connectionPassword | The JMS connection password | no | - | -
subscriptionDurable | Whether subscription is durable | no | true/false | false (subscription is not durable)
durableSubscriberClientID | The ClientId parameter when using durable subscriptions | Required if subscriptionDurable is specified as "true" | - | -
durableSubscriberName | The name of the durable subscriber | Required if subscriptionDurable is specified as "true" | - | -
retryInterval | The retry interval (in milliseconds) if the JMS connection cannot be established at the beginning or is lost in the middle | no | A valid long value | 10000
maxRetryCount | Maximum retry count if the JMS connection cannot be established at the beginning or is lost in the middle | no | A valid integer value | 5
useReceiver | Use synchronous message receiver to receive message instead of asynchronous message listener | no | true/false | false
concurrentConsumers | Number of concurrent consumers to be spawned when the server connector is starting. | no | Integer | 1
connectionFactoryNature | The type of connection factory to use when creating consumers | no | default,cached,pooled | default

Example: 

```
import ballerina.net.jms;

@jms:ConnectionProperty{key:"connectionFactoryType", value:"queue"}
@jms:ConnectionProperty{key:"destination", value:"MyQueue"}
@jms:ConnectionProperty{key:"useReceiver", value:"true"}
@jms:ConnectionProperty{key:"connectionFactoryJNDIName", value:"QueueConnectionFactory"}
@jms:ConnectionProperty{key:"sessionAcknowledgement", value:"AUTO_ACKNOWLEDGE"}
@jms:ConnectionProperty{key:"concurrentConsumers", value:"5"}
@jms:ConnectionProperty{key:"connectionFactoryNature", value:"pooled"}
```

### Step 3: Add the resource
Add a resource to the JMS service. This is required, because whenever a message comes from the JMS provider to a specific JMS service, it will be delivered to this particular resource. You must have only one resource in a JMS service.

Example:

```
import ballerina.net.jms;

@jms:JMSSource {
factoryInitial : "org.wso2.andes.jndi.PropertiesFileInitialContextFactory",
providerUrl : "/Users/Akalanka/test/jndi.properties",
connectionFactoryType : "queue"}
@jms:ConnectionProperty{key:"destination", value:"MyQueue"}
@jms:ConnectionProperty{key:"useReceiver", value:"true"}
@jms:ConnectionProperty{key:"connectionFactoryJNDIName", value:"QueueConnectionFactory"}
@jms:ConnectionProperty{key:"sessionAcknowledgement", value:"AUTO_ACKNOWLEDGE"}
service jmsService {
    resource onMessage (message m) {
        // ProcessMessage
    }
```

Now you will have a JMS service that can be deployed to the Ballerina server. 

>NOTE: Before deploying the JMS service, you must copy the client-libs provided by the JMS provider to `{Ballerina_HOME}/bre/lib` 

Following is a sample JMS service.

```
import ballerina.net.jms;

@jms:JMSSource {
factoryInitial : "org.wso2.andes.jndi.PropertiesFileInitialContextFactory",
providerUrl : "/Users/Akalanka/test/jndi.properties"}
@jms:ConnectionProperty{key:"connectionFactoryType", value:"queue"}
@jms:ConnectionProperty{key:"destination", value:"MyQueue"}
@jms:ConnectionProperty{key:"useReceiver", value:"true"}
@jms:ConnectionProperty{key:"connectionFactoryJNDIName", value:"QueueConnectionFactory"}
@jms:ConnectionProperty{key:"sessionAcknowledgement", value:"AUTO_ACKNOWLEDGE"}
service jmsService {
    resource onMessage (message m) {
        //Process the message
        jms:acknowledge(m, "SUCCESS");
    }
}
```

## Ballerina Native Functions
This section describes the native functions you can use to work with JMS messages.

### Package ballerina.lang.messages

**messages:getHeader**

This function can be used to get the value of the particular JMS header. It accepts the following parameters:    

* Relevant message to get the header from
* Header name

Example:
 
```
import ballerina.lang.messages
---
string jmsMessageId;
jmsMessageId = messages:getHeader(m, "JMS_MESSAGE_ID");
```

**messages:getStringPayload**

This function can be used to get the string payload of the JMS text message or bytes message. This function accepts the relevant Ballerina message as the parameter. It will return the string payload of the relevant message.

Example:
```
import ballerina.lang.messages
---
string stringPayload;
stringPayload = messages:getStringPayload(m);
```

**messages:getStringValue**

This function can be used to get the string value of a specific map key in a map type message. This function accepts the following:

* Relevant Ballerina message to get the string value from
* Relevant map key value

Example: 

```
import ballerina.lang.messages
---
string stringValue;
stringValue = messages:getStringValue(m, “count”);
```

**messages:getProperty**

This function can be used to retrieve a property from the message.

Example:

Message type can be retrieved from the message as below.

```
import ballerina.lang.messages;
---

string msgType = messages:getProperty(m, "JMS_MESSAGE_TYPE");

```

### Package ballerina.net.jms

**jms:acknowledge**

This function can be used to acknowledge the message delivery or success, which is particularly useful when using the client acknowledgement mode. This function accepts the following parameters:

* Ballerina message that needs to be acknowledged 
* Message delivery status - “SUCCESS” or  “ERROR”

Example:

```
import ballerina.net.jms
---
jms:acknowledge(m, "SUCCESS");
```

**jms:commit**

This function can be used to commit the JMS session when using the session acknowledgement mode. This function accepts one parameter: the relevant message that the user wants to acknowledge the session with.

Example: 

```
import ballerina.net.jms
---
jms:commit();
```

**jms:rollback**

This function can be used to commit the messages when using the session acknowledgement mode. This function accepts one parameter: the relevant Ballerina message that the user wants to roll back the session with.

Example:
```
import ballerina.net.jms
---
jms:rollback();
```
