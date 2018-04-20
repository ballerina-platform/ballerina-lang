
import ballerina/doc;

@doc:Description { value:"Message acknowledgement action implementation for jms connector when using jms client acknowledgement mode"}
@doc:Param { value:"message: message" }
@doc:Param { value:"deliveryStatus: Specify whether message delivery is SUCCESS or ERROR" }
native function acknowledge (message m, string deliveryStatus);

@doc:Description { value:"Session rollback action implementation for jms connector when using jms session transaction mode"}
@doc:Param { value:"message: message" }
native function rollback (message m);

@doc:Description { value:"Session commit action implementation for jms connector when using jms session transaction mode"}
@doc:Param { value:"message: message" }
native function commit (message m);

@doc:Description { value:"JMS client connector to send messages to the JMS provider."}
@doc:Param { value:"connection and optional properties for the connector"}
connector ClientConnector (map properties) {

    @doc:Description {value:"SEND action implementation of the JMS Connector"}
    @doc:Param {value:"connector: Connector"}
    @doc:Param {value:"destinationName: Destination Name"}
    @doc:Param {value:"message: Message"}
    native action send (ClientConnector jmsClientConnector, string destinationName, message m) (boolean);

}

@doc:Description { value:"Header key for message ID. This is not a required header and jms client application might set the value when sending the message to the JMS broker."}
const string HEADER_MESSAGE_ID = "JMS_MESSAGE_ID";

@doc:Description { value:"JMS priority value header for the message. Priority value can range from 0 to 9. 0 is the lowest priority. 9 is the highest priority"}
const string HEADER_PRIORITY = "JMS_PRIORITY";

@doc:Description { value:"Header for JMS message expiry. "}
const string HEADER_EXPIRATION = "JMS_EXPIRATION";

@doc:Description { value:"Header to check the message delivery status. Whether the message is being redelivered from the JMS broker. This cannot be used to set the redilvered header for the message"}
const string HEADER_REDELIVERED = "JMS_REDELIVERED";

@doc:Description { value:"JMS correlation id message header"}
const string HEADER_CORRELATION_ID = "JMS_CORRELATION_ID";

@doc:Description { value:"JMS destination message header"}
const string HEADER_DESTINATION = "JMS_DESTINATION";

@doc:Description { value:"JMS timestamp message header"}
const string HEADER_TIMESTAMP = "JMS_TIMESTAMP";

@doc:Description { value:"JMS reply to message header"}
const string HEADER_REPLY_TO = "JMS_REPLY_TO";

@doc:Description { value:"Header to set the jms message type. The JMS API does not define a standard message definition repository and it is upto the user to set this header. Some JMS providers may have restricted set of message type values."}
const string HEADER_MESSAGE_TYPE = "JMS_TYPE";

@doc:Description { value:"JMS delivery mode message header. Can select either persistent or non persitent message delivery for the message"}
const string HEADER_DELIVERY_MODE = "JMS_DELIVERY_MODE";

@doc:Description { value:"Value for persistent JMS message delivery mode"}
const string PERSISTENT_DELIVERY_MODE = "2";

@doc:Description { value:"Value for non persistent JMS message delivery mode"}
const string NON_PERSISTENT_DELIVERY_MODE = "1";

const string DELIVERY_SUCCESS = "Success";

const string DELIVERY_ERROR = "Error";
