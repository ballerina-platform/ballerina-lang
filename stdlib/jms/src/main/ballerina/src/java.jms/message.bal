// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

# Represent the JMS message used to send and receive content from the a JMS provider.
#
# Most message-oriented middleware (MOM) products treat messages as lightweight entities that consist of a header
# and a body. The header contains fields used for message routing and identification; the body contains the
# application data being sent.
public type Message client object {
    private Session session;
    private MessageType msgType;

    # The `Message` object is initialized with a `Session` object and `MessageType` indicating the type of the message.
    # 
    # + session - The session from which the message would be created
    # + msgType - The type of the message
    # + return - `error` if `Message` creation fails
    public function __init(Session session, MessageType msgType) returns error? {
        self.session = session;
        self.msgType = msgType;
        return self.createMessage();
    }

    function createMessage() returns error? = external;

    # Sets the payload/body of the message. 
    # If the `MessageType` is `TEXT_MESSAGE`, an attempt would be made to convert the data to string type and the
    # body/content of the `Message` would be replaced.
    # If the `MessageType` is `MAP_MESSAGE`, then only `map` type content is allowed and the data would be replaced.
    # Otherwise, an error is returned.
    # If the `MessageType` is `BYTES_MESSAGE` or `STREAM_MESSAGE`, this method can be called multiple times to populate
    # the message without replacing the content. If the content is of types `map`, `xml`, or `json`, they will be
    # converted to string. 
    # The data types `json`, `xml`, and `map` are converted to `string` before sending except for the `MAP_MESSAGE`
    # type, which functions as described above.
    # 
    # + content - the content of the message
    # + return - if an error occurred while setting the payload or on content mismatch
    public function setPayload(@untainted MessageContent content) returns error? = external;

    # Returns the payload of the message.
    # If the `MessageType` is  `TEXT_MESSAGE`, returns string data or attempts to convert the message to the given
    # optional `DataType`. If the  `bytesLength` parameter is set it is ignored.
    # If the `MessageType` is `MAP_MESSAGE`, then a `map` is returned regardless of the `DataType` and `bytesLength`.
    # If the `MessageType` is `BYTES_MESSAGE` or `STREAM_MESSAGE`, the `DataType` has to be mentioned as it cannot
    # be deduced. This method can be called multiple times to receive the available data. If the `DataType` is `BYTES`,
    # then the `bytesLength` has to be specified. Otherwise the number of bytes to be read cannot be deduced.
    #
    # + dataType - this is optional for `TEXT_MESSAGE` and `MAP_MESSAGE` types and mandatory for `BYTES_MESSAGE` and
    # `STREAM_MESSAGE` types to deduce the data to be returned.
    # + bytesLength - this is optional and is ignored for other dataTypes except for `BYTES` dataType in 
    # a `BYTES_MESSAGE` or `STREAM_MESSAGE`
    # + return - If an error occurred when retrieving the payload from the `Message` or if the required parameters are
    # not set
    public function getPayload(DataType? dataType = (), int? bytesLength = ())
        returns @tainted MessageContent | error = external;

    # Sets a JMS transport property to the `Message`. The `json` and `xml` values are converted to `string` properties.
    # 
    # + key - The unique property name.
    # + value - The property value.
    # + return - If an error occurred while setting the property.
    public function setProperty(string key, @untainted string | int | float | boolean | byte | json | xml value)
        returns error? = external;

    # Retrieves the JMS transport property from the `Message` for the given name.
    #
    # + key - The property name
    # + return - The respective property, `nil` if not found, or the `error` if  an error occurred when retrieving
    # the property.
    public function getProperty(string key) returns @tainted string | int | float | boolean | byte | () |
        error = external;

    # Returns an `string[]` of all the property names.
    #
    # + return - an array of all the names of property values.
    public function getPropertyNames() returns string[]? = external;

    # Sets the `CustomHeaders` to the `Message`.
    #
    # + headers - The `CustomHeaders` record to be set
    # + return - If an error occurred while setting the `CustomHeaders`
    public function setCustomHeaders(@untainted CustomHeaders headers) returns error? = external;

    # Returns the `CustomHeaders` of the `Message`
    #
    # + return - If an error occurred while retrieving the `CustomHeaders`
    public function getCustomHeaders() returns CustomHeaders | error = external;

    # Returns the `Headers` of the `Message`
    #
    # + return - The `Headers` or the `error` if an error occurred while setting the `Headers`
    public function getHeaders() returns Headers | error = external;

    # Acknowledges the reception of this message. This is used when the consumer has chosen CLIENT_ACKNOWLEDGE as its
    # acknowledgment mode.
    #
    # + return - If an error occurred while acknowledging the message
    public remote function acknowledge() returns error? = external;

    # Returns the `MessageType` of the `Message`
    #
    # + return - the `MessageType` of the `Message`
    public function getType() returns MessageType {
        return self.msgType;
    }

};

# The type that represents the union of the allowed types for the `Message`
public type MessageContent int | float | byte | boolean | string | map<string | byte | int | float | boolean | byte[] |
    ()> | xml | json | byte[];

# The JMS message types.
public type MessageType MESSAGE | TEXT_MESSAGE | BYTES_MESSAGE | STREAM_MESSAGE | MAP_MESSAGE;

public const MESSAGE = "MESSAGE";
public const TEXT_MESSAGE = "TEXT_MESSAGE";
public const BYTES_MESSAGE = "BYTES_MESSAGE";
public const STREAM_MESSAGE = "STREAM_MESSAGE";
public const MAP_MESSAGE = "MAP_MESSAGE";

# The data types allowed for the `BYTES_MESSAGE` and `STREAM_MESSAGE` types.
public type DataType INT | FLOAT | BYTE | BOOLEAN | STRING | BYTES | XML | JSON;

public const INT = "INT";
public const FLOAT = "FLOAT";
public const BYTE = "BYTE";
public const BOOLEAN = "BOOLEAN";
public const STRING = "STRING";
public const XML = "XML";
public const JSON = "JSON";
public const BYTES = "BYTES";

# While many of the JMS headers are set automatically when the message is delivered, several others must be set 
# explicitly on the Message object before it is delivered by the producer.
#
# + replyTo - JMSReplyTo header destination
# + correlationId - JMSCorrelationID header
# + jmsType - JMSType header
public type CustomHeaders record {|
    Destination replyTo?;
    string correlationId?;
    string jmsType?;
|};

# Most JMS headers are automatically assigned; their value is set by the JMS provider when the message is delivered.
#
# + destination - The JMSDestination header identifies the destination with either a Topic or Queue
# + deliveryMode - JMSDeliveryMode for the `DeliveryMode` in JMS
# + messageId - The JMSMessageID which uniquely identifies a message
# + timestamp - The JMSTimestamp is set automatically by the message producer when the send() operation is invoked.
# + expiration - A Message object’s expiration date prevents the message from being delivered to consumers after it 
# has expired.
# + redelivered - The JMSRedelivered header indicates that the message was redelivered to the consumer.
# + priority - The message producer may assign a priority to a message when it is delivered.
public type Headers record {|
   Destination destination;
   DeliveryMode deliveryMode;
   string messageId;
   int timestamp;
   int expiration;
   boolean redelivered;
   int priority;
|};

# The two types of delivery modes in JMS.
public type DeliveryMode PERSISTENT | NON_PERSISTENT;

# A persistent message is delivered once-and-only-once which means that if the JMS provider fails,
# the message is not lost; it will be delivered after the server recovers.
public const PERSISTENT = "PERSISTENT";
# A non-persistent message is delivered at-most-once which means that it can be lost permanently if the JMS
# provider fails.
public const NON_PERSISTENT = "NON_PERSISTENT";
