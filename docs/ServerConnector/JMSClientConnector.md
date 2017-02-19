#JMS Client Connector
The jms client connector can be used to send/publish JMS messages to a JMS broker. Given below is a diagram that represents a high level view of the connector.

![JMS_Client_Connector](../images/jms_client_connector.png)

## How to write a JMS Service ?
###Step1
Create a ballerina resource or a function and import “ballerina.net.jms” package.

###Step2
Initialize a JMS connector instance by passing the following parameters. This can be done inside either a ballerina function or a ballerina resource.



Expected Values

Parameter Name | Parameter Type | Description | Expected Values
---------- | ------------- | ----- | --------
FactoryInitial | string | The JNDI initial context factory class. The class must implement thejava.naming.spi.InitialContextFactoryinterface. | A valid class name depending on the jms provider
ProviderURL | string | The URL/ file path of  of the JNDI provider. | A valid url/ path for the JNDI provider

Example :-
```
jms:ClientConnector jmsConnector = create jms:ClientConnector("org.wso2.andes.jndi.PropertiesFileInitialContextFactory", "jndi.properties");
```
###Step3
Invoke the send action of the JMS client connector and pass the relevant parameters as mentioned below.

Parameter Name | Parameter type | Description | Expected Values
------------ | ------------- | ----------- | -------------
JMSConnector | JMSConnector | A JMSConnector instance. | A JMSConnector instance that have been initialized.
ConnectionFactoryName | string | The JNDI name of the connection factory. | -
DestinationName | string | The JNDI name of the destination. | The JNDI name of the destination.
DestinationType | string | The type of the destinaiton. | queue/topic. If not given taken as queue.
MessageType | string | The message type needed to be sent | TextMessage<br>BytesMessage<br>ObjectMessage<br>MapMessage
Message | message | The message conaining the payload to be sent. | A Ballerina message.
PropertyMap | map | A map of ballerina optional properties. | A valid ballerina map.

Optional parameters that can be defined in propertyMap:

Parameter Name | Parameter type | Description | Expected Values
------------- | ------------------- | ---------------- | ---------------
ConnectionUsername | string | A valid connection username to connect to the jms provider. | Valid string username.
ConnectionPassword | string | A valid connection password to connect to the jms provider. | Valid string password.
MapData | map | Map of data to send in a jms map message. | Map of data to send in a jms map message.  *Only if message type is MapMessage.
ConnectionCacheLevel | int | Caching level required when sending messages. | 0 - No caching (default) 1 - Cache Connection 2 - Cache Session 3 - Cache Consumer 4 - Cache Producer


Example :-
```
message queueMessage = {};
map dataMap;
dataMap = {};
messages:setStringPayload(queueMessage, "Hello from ballerina");
jms:JMSConnector.send(jmsEP, "QueueConnectionFactory", "MyQueue", "queue", "TextMessage", queueMessage, dataMap);
```

Given below is a sample Ballerina function depicting the creation of a JMS client connector.
```
jms:JMSConnector jmsEP = create jms:JMSConnector("org.wso2.andes.jndi.PropertiesFileInitialContextFactory", "jndi.properties");
message queueMessage = {};
map dataMap;
dataMap = { "country" : "US", "currency" : "Dollar" , "states" : "50"};
map propertyMap;
propertyMap = { "MapData" : dataMap};
jms:JMSConnector.send(jmsEP, "QueueConnectionFactory", "MyQueue", "queue", "MapMessage", queueMessage, propertyMap);
```