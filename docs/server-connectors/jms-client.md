# JMS Client Connector

The JMS client connector can be used to send/publish JMS messages to a JMS broker. Given below is a diagram that represents a high-level view of the connector.

![JMS_Client_Connector](../images/jms_client_connector.png)

## Defining a JMS service

### Step 1
Create a Ballerina resource or a function and import the `ballerina.net.jms` package.

### Step 2
Initialize a JMS connector instance by passing the following parameters. This can be done inside either a Ballerina function or a Ballerina resource.

### Expected values

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

Example:

```java
map properties = {
       "initialContextFactory": "wso2mbInitialContextFactory",
       "providerUrl": "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
       "connectionFactoryName": "QueueConnectionFactory",
       "connectionFactoryType" : "queue"};
// Create the JMS client Connector using the connection properties we defined earlier.
jms:ClientConnector jmsEP = create jms:ClientConnector(properties);
```

### Step 3
Invoke the send action of the JMS client connector and pass the relevant parameters as mentioned below.

**jms:ClientConnector.send**

* ClientConnector - JMS client connector instance
* string          - JMS destination
* message         - Ballerina message

Example:

```java
message queueMessage = {};
messages:setStringPayload(queueMessage, "Hello from ballerina");
jms:ClientConnector.send(jmsEP, "MyQueue", queueMessage);
```

Given below is a sample Ballerina function depicting the creation of a JMS client connector and sending a message

```java
import ballerina.net.jms;
import ballerina.lang.messages;

function main (string[] args) {
    jmsSender();
}

function jmsSender() (boolean) {
    // In this example we connect to the WSO2 MB server
    map properties = {
       "initialContextFactory": "wso2mbInitialContextFactory",
       "providerUrl": "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
       "connectionFactoryName": "QueueConnectionFactory",
       "connectionFactoryType" : "queue"};
    // Create the JMS client Connector using the connection properties we defined earlier.
    jms:ClientConnector jmsEP = create jms:ClientConnector(properties);
    // Create an empty Ballerina message
    message queueMessage = {};
    // Set a string payload to the message
    messages:setStringPayload(queueMessage, "Hello from Ballerina!");
    // Send the Ballerina message to the JMS provider.
    jms:ClientConnector.send(jmsEP, "MyQueue", queueMessage);
    return true;
}
```
