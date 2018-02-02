package ballerina.net.jms;


public struct JMSMessage {
}

@Description { value:"Message acknowledgement action implementation for jms connector when using jms client acknowledgement mode"}
@Param { value:"message: message" }
@Param { value:"deliveryStatus: Specify whether message delivery is SUCCESS or ERROR" }
public native function <JMSMessage msg> acknowledge (string deliveryStatus);

@Description { value:"Session rollback action implementation for jms connector when using jms session transaction mode"}
@Param { value:"message: message" }
public native function <JMSMessage msg> rollback ();

@Description { value:"Session commit action implementation for jms connector when using jms session transaction mode"}
@Param { value:"message: message" }
public native function <JMSMessage msg> commit ();

@Description { value:"JMS Client connector properties to pass JMS client connector configurations"}
@Field {value:"initialContextFactory: Initial context factory name, specific to the provider"}
@Field {value:"providerUrl: Connection URL of the provider"}
@Field {value:"connectionFactoryName: Name of the connection factory"}
@Field {value:"connectionFactoryType: Type of the connection factory (queue/topic)"}
@Field {value:"acknowledgementMode: Ack mode (auto-ack, client-ack, dups-ok-ack, transacted, xa)"}
@Field {value:"clientCaching: Is client caching enabled (default: enabled)"}
@Field {value:"connectionUsername: Connection factory username"}
@Field {value:"connectionPassword: Connection factory password"}
@Field {value:"configFilePath: Path to be used for locating jndi configuration"}
@Field {value:"connectionCount: Number of pooled connections to be used in the transport level (default: 5)"}
@Field {value:"sessionCount: Number of pooled sessions to be used per connection in the transport level (default: 10)"}
@Field {value:"properties: Additional Properties"}
public struct ClientProperties {
    string initialContextFactory;
    string providerUrl;
    string connectionFactoryName;
    string connectionFactoryType = "queue";
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
    boolean clientCaching = true;
    string connectionUsername;
    string connectionPassword;
    string configFilePath;
    int connectionCount = 5;
    int sessionCount = 10;
    map properties;
}

@Description { value:"JMS client connector to send messages to the JMS provider."}
@Param { value:"clientProperties: Pre-defind and additional properties for the connector"}
public connector JmsClient (ClientProperties clientProperties) {

    string connectorID = "EMPTY_ID";

    @Description {value:"SEND action implementation of the JMS Connector"}
    @Param {value:"destinationName: Destination Name"}
    @Param {value:"message: Message"}
    native action send (string destinationName, JMSMessage m);

}


@Description { value:"Create JMS Text Message based on client connector"}
@Param { value:"jmsClient: clientConnector" }
public native function createTextMessage (ClientProperties jmsClient) (JMSMessage);

@Description { value:"Create JMS Bytes Message based on client connector"}
@Param { value:"jmsClient: clientConnector" }
public native function createBytesMessage (ClientProperties jmsClient) (JMSMessage);

@Description { value:"Value for persistent JMS message delivery mode"}
public const int PERSISTENT_DELIVERY_MODE = 2;

@Description { value:"Value for non persistent JMS message delivery mode"}
public const int NON_PERSISTENT_DELIVERY_MODE = 1;

@Description { value:"Value to use when acknowledge jms messages for success"}
public const string DELIVERY_SUCCESS = "Success";

@Description { value:"Value to use when acknowledge jms messages for errors"}
public const string DELIVERY_ERROR = "Error";

@Description { value:"Value for auto acknowledgement mode (default)"}
public const string AUTO_ACKNOWLEDGE = "AUTO_ACKNOWLEDGE";

@Description { value:"Value for client acknowledge mode"}
public const string CLIENT_ACKNOWLEDGE = "CLIENT_ACKNOWLEDGE";

@Description { value:"Value for dups ok acknowledgement mode"}
public const string DUPS_OK_ACKNOWLEDGE = "DUPS_OK_ACKNOWLEDGE";

@Description { value:"Value for Session transacted mode"}
public const string SESSION_TRANSACTED = "SESSION_TRANSACTED";

@Description { value:"Value for XA transcted mode"}
public const string XA_TRANSACTED = "XA_TRANSACTED";

@Description { value:"Value for JMS Queue type"}
public const string TYPE_QUEUE = "queue";

@Description { value:"Value for JMS Topic type"}
public const string TYPE_TOPIC = "topic";

@Description { value:"Sets a JMS transport string property from the message"}
@Param { value:"key: The string property name" }
@Param { value:"value: The string property value" }
public native function <JMSMessage msg> setStringProperty (string key, string value);

@Description { value:"Gets a JMS transport string property from the message"}
@Param { value:"key: The string property name" }
@Return { value:"string: The string property value" }
public native function <JMSMessage msg> getStringProperty (string key) (string);

@Description { value:"Sets a JMS transport integer property from the message"}
@Param { value:"key: The integer property name" }
@Param { value:"value: The integer property value" }
public native function <JMSMessage msg> setIntProperty (string key, int value);

@Description { value:"Gets a JMS transport integer property from the message"}
@Param { value:"key: The integer property name" }
@Return { value:"int: The integer property value" }
public native function <JMSMessage msg> getIntProperty (string key) (int);

@Description { value:"Sets a JMS transport boolean property from the message"}
@Param { value:"key: The boolean property name" }
@Param { value:"value: The boolean property value" }
public native function <JMSMessage msg> setBooleanProperty (string key, boolean value);

@Description { value:"Gets a JMS transport boolean property from the message"}
@Param { value:"key: The boolean property name" }
@Return { value:"boolean: The boolean property value" }
public native function <JMSMessage msg> getBooleanProperty (string key) (boolean);

@Description { value:"Sets a JMS transport float property from the message"}
@Param { value:"key: The float property name" }
@Param { value:"value: The float property value" }
public native function <JMSMessage msg> setFloatProperty (string key, float value);

@Description { value:"Gets a JMS transport float property from the message"}
@Param { value:"key: The float property name" }
@Return { value:"float: The float property value" }
public native function <JMSMessage msg> getFloatProperty (string key) (float);

@Description { value:"Sets text content for the JMS message"}
@Param { value:"content: Text Message Content" }
public native function <JMSMessage msg> setTextMessageContent (string content);

@Description { value:"Gets text content of the JMS message"}
@Return { value:"string: Text Message Content" }
public native function <JMSMessage msg> getTextMessageContent () (string);

@Description { value:"Sets bytes content for the JMS message"}
@Param { value:"content: Bytes Message Content" }
public native function <JMSMessage msg> setBytesMessageContent (blob content);

@Description { value:"Get bytes content of the JMS message"}
@Return { value:"string: Bytes Message Content" }
public native function <JMSMessage msg> getBytesMessageContent () (blob);

@Description { value:"Get JMS transport header MessageID from the message"}
@Return { value:"string: The header value" }
public native function <JMSMessage msg> getMessageID() (string);

@Description { value:"Get JMS transport header Timestamp from the message"}
@Return { value:"int: The header value" }
public native function <JMSMessage msg> getTimestamp() (int);

@Description { value:"Sets DeliveryMode JMS transport header to the message"}
@Param { value:"i: The header value" }
public native function <JMSMessage msg> setDeliveryMode(int i);

@Description { value:"Get JMS transport header DeliveryMode from the message"}
@Return { value:"int: The header value" }
public native function <JMSMessage msg> getDeliveryMode() (int);

@Description { value:"Sets Expiration JMS transport header to the message"}
@Param { value:"i: The header value" }
public native function <JMSMessage msg> setExpiration(int i);

@Description { value:"Get JMS transport header Expiration from the message"}
@Return { value:"int: The header value" }
public native function <JMSMessage msg> getExpiration() (int);

@Description { value:"Sets Priority JMS transport header to the message"}
@Param { value:"i: The header value" }
public native function <JMSMessage msg> setPriority(int i);

@Description { value:"Get JMS transport header Priority from the message"}
@Return { value:"int: The header value" }
public native function <JMSMessage msg> getPriority() (int);

@Description { value:"Get JMS transport header Redelivered from the message"}
@Return { value:"boolean: The header value" }
public native function <JMSMessage msg> getRedelivered() (boolean);

@Description { value:"Sets CorrelationID JMS transport header to the message"}
@Param { value:"s: The header value" }
public native function <JMSMessage msg> setCorrelationID(string s);

@Description { value:"Get JMS transport header CorrelationID from the message"}
@Return { value:"string: The header value" }
public native function <JMSMessage msg> getCorrelationID() (string);

@Description { value:"Sets Type JMS transport header to the message"}
@Param { value:"s: The header value" }
public native function <JMSMessage msg> setType(string s);

@Description { value:"Get JMS transport header Type from the message"}
@Return { value:"string: The header value" }
public native function <JMSMessage msg> getType() (string);

@Description { value:"Clear JMS properties of the message"}
public native function <JMSMessage msg> clearProperties();

@Description { value:"Clear body JMS of the message"}
public native function <JMSMessage msg> clearBody();
