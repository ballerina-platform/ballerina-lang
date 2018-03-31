# JMS Server Connector
The JMS Server connector can be used to listen to a topic/queue in a JNDI-based JMS provider. Following is a high-level view on how the JMS Server connector works:

![JMS_Server_Connector](../images/jms_server_connector.png)

## How to write a JMS service

### Step 1: Create the service
Create a service with unique name.

Example: 
```
service<jms> jmsService {
}
```
### Step 2: Specify the connection details

Add a service-level annotation named “jms:config”, and add the relevant key-value pairs depending on the jms client to specify the JMS connection details.
The following table describes supported keys that can be used with the @jms:config annotation.

Key | Description | Expected Values | Default Value
------------ | ------------- | ----------- | ----------
initialContextFactory | The JNDI initial context factory class. The class must implement the java.naming.spi.InitialContextFactory interface. | Depends on the JMS provider. When using WSO2 MB the value can be set as `wso2mbInitialContextFactory` | -
providerUrl | The URL to connect to the JMS brroker | A valid URL depending on the JMS provider. | -
configFilePath | specify a configuration file instead of provider url | file path to a jndi.properties file | -
connectionFactoryType | The type of the connection factory | queue, topic | queue
connectionFactoryName | The JNDI name of the connection factory | - | -
destination | The name of the destination | - | service name
acknowledgmentMode | The JMS session acknowledgment mode | AUTO_ACKNOWLEDGE, CLIENT_ACKNOWLEDGE, DUPS_OK_ACKNOWLEDGE, SESSION_TRANSACTED | AUTO_ACKNOWLEDGE
clientID | The clientId parameter when using durable subscriptions | - | -
durableSubscriptionId | For topic subscriptions, setting the value will create a durable topic subscription | any valid subscription name depending on the JMS provider | -
connectionFactoryNature | The type of connection factory to use when creating consumers | default,cached,pooled | default
properties | Key values that are not in the above set of keys can be set using the properties array | key value pair array with key values seperated by an `=` charachter. `["key1=value1", "key2=value2"]` | -

Example: 

```java
import ballerina.net.jms;

@jms:config {
    initialContextFactory:"wso2mbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    connectionFactoryType:"queue",
    connectionFactoryName:"QueueConnectionFactory",
    destination:"MyQueue",
    acknowledgmentMode:"CLIENT_ACKNOWLEDGE"
}
```

### Step 3: Add the resource
Add a resource to the JMS service. This is required, because whenever a message comes from the JMS provider to a specific JMS service, it will be delivered to this particular resource. You must have only one resource in a JMS service.

Example:

```java
import ballerina.net.jms;
@jms:config {
    initialContextFactory:"wso2mbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    connectionFactoryType:"queue",
    connectionFactoryName:"QueueConnectionFactory",
    destination:"MyQueue",
    acknowledgmentMode:"CLIENT_ACKNOWLEDGE"
}
service<jms> jmsService {
    resource onMessage (message m) {
        // ProcessMessage
    }
```

Now you will have a JMS service that can be deployed to the Ballerina server. 

>NOTE: Before deploying the JMS service, you must copy the client-libs provided by the JMS provider to `{Ballerina_HOME}/bre/lib` 

Following is a sample JMS service.

```java
import ballerina.net.jms;

@jms:config {
    initialContextFactory:"wso2mbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    connectionFactoryType:"queue",
    connectionFactoryName:"QueueConnectionFactory",
    destination:"MyQueue",
    acknowledgmentMode:"CLIENT_ACKNOWLEDGE"
}
service<jms> jmsService {
    resource onMessage (message m) {
        //Process the message
        jms:acknowledge(m, jms:DELIVERY_SUCCESS);
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
import ballerina.lang.messages;
import ballerina.net.jms;
---
string jmsMessageId;
jmsMessageId = messages:getHeader(m, jms:HDR_MESSAGE_ID);
```
**messages:setHeader**

This function can be used to set the value of the particular JMS header. It accepts the following parameters:    

* Relevant message to get the header from
* Header name
* Header value

Example:
 
```
import ballerina.lang.messages;
import ballerina.net.jms;
---
string jmsMessageId;
messages:setHeader(responseMessage, jms:HDR_CORRELATION_ID, "response-001");
```
Following are the supported JMS headers in Ballerina 

JMS Header | Ballerina Header | Supported Operations
-----------| ---------------- | -------------------
JMSDestination | HEADER_DESTINATION | get only, set by JMS provider
JMSDeliveryMode | HEADER_DELIVERY_MODE | get only, set by JMS provider
JMSExpiration | HEADER_EXPIRATION | get and set
JMSPriority | HEADER_PRIORITY | get and set
JMSMessageID | HEADER_MESSAGE_ID | get only, set by JMS provider
JMSTimestamp | HEADER_TIMESTAMP | get only, set by JMS provider
JMSCorrelationID | HEADER_CORRELATION_ID | get and set
JMSReplyTo | HEADER_REPLY_TO | get and set
JMSType | HEADER_MESSAGE_TYPE | get and set
JMSRedelivered | HEADER_REDELIVERED | get only, set by JMS provider

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
* Message delivery status - jms:DELIVERY_SUCCESS or jms:DELIVERY_ERROR

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
