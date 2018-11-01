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
public type Message object {

    # Gets text content of the JMS message
    #
    # + return - the string containing this message's data or an JMS error
    public extern function getTextMessageContent() returns @tainted string|error;

    # Gets map content of the JMS message
    #
    # + return - the string containing this message's data or an JMS error
    public extern function getMapMessageContent() returns @tainted map|error;

    # Sets a JMS transport string property from the message
    #
    # + key - The string property name
    # + value - The string property value
    # + return - nil or an JMS error
    public extern function setStringProperty(@sensitive string key, string value) returns error?;

    # Gets a JMS transport string property from the message
    #
    # + key - The string property name
    # + return - The string property value, JMS error or nil if there is no property by this name
    public extern function getStringProperty(@sensitive string key) returns @tainted (string|error)?;

    # Sets a JMS transport integer property from the message
    #
    # + key - The integer property name
    # + value - The integer property value
    # + return - nil or an JMS error
    public extern function setIntProperty(@sensitive string key, int value) returns error?;

    # Gets a JMS transport integer property from the message
    #
    # + key - The integer property name
    # + return - The integer property value or JMS error
    public extern function getIntProperty(@sensitive string key) returns @tainted int|error;

    # Sets a JMS transport boolean property from the message
    #
    # + key - The boolean property name
    # + value - The boolean property value
    # + return - nil or an JMS error
    public extern function setBooleanProperty(@sensitive string key, boolean value) returns error?;

    # Gets a JMS transport boolean property from the message
    #
    # + key - The boolean property name
    # + return - The boolean property value or JMS error
    public extern function getBooleanProperty(@sensitive string key) returns @tainted boolean|error;

    # Sets a JMS transport float property from the message
    #
    # + key - The float property name
    # + value - The float property value
    # + return - nil or an JMS error
    public extern function setFloatProperty(@sensitive string key, float value) returns error?;

    # Gets a JMS transport float property from the message
    #
    # + key - The float property name
    # + return - The float property value or JMS error
    public extern function getFloatProperty(@sensitive string key) returns @tainted float|error;

    # Gets JMS transport header MessageID from the message
    #
    # + return - The header value or JMS error
    public extern function getMessageID() returns @tainted string|error;

    # Gets JMS transport header Timestamp from the message
    #
    # + return - The timestamp header value or JMS error
    public extern function getTimestamp() returns @tainted int|error;

    # Sets DeliveryMode JMS transport header to the message
    #
    # + mode - The header value
    # + return - nil or an JMS error
    public extern function setDeliveryMode(int mode) returns error?;

    # Get JMS transport header DeliveryMode from the message
    #
    # + return - The delivery mode header value or JMS error
    public extern function getDeliveryMode() returns @tainted int|error;

    # Sets Expiration JMS transport header to the message
    #
    # + value - The header value
    # + return - nil or an JMS error
    public extern function setExpiration(int value) returns error?;

    # Gets JMS transport header Expiration from the message
    #
    # + return - The expiration header value or JMS error
    public extern function getExpiration() returns @tainted int|error;

    # Sets Type JMS transport header to the message
    #
    # + messageType - The message type header value
    # + return - nil or an JMS error if any JMS provider level internal error occur
    public extern function setType(string messageType) returns error?;

    # Gets JMS transport header Type from the message
    #
    # + return - The JMS message type header value or JMS error
    public extern function getType() returns @tainted string|error;

    # Clears JMS properties of the message
    #
    # + return - nil or error if any JMS provider level internal error occur
    public extern function clearProperties() returns error?;

    # Clears body of the JMS message
    #
    # + return - nil or an JMS error
    public extern function clearBody() returns error?;

    # Sets priority JMS transport header to the message
    #
    # + value - The header value
    # + return - nil or an JMS error
    public extern function setPriority(int value) returns error?;

    # Gets JMS transport header Priority from the message
    #
    # + return - The JMS priority header value or error
    public extern function getPriority() returns @tainted int|error;

    # Gets JMS transport header Redelivered from the message
    #
    # + return - The JMS redelivered header value or JMS error
    public extern function getRedelivered() returns @tainted boolean|error;

    # Sets CorrelationID JMS transport header to the message
    #
    # + value - The header value
    # + return - nil or an JMS error
    public extern function setCorrelationID(string value) returns error?;

    # Gets JMS transport header CorrelationID from the message
    #
    # + return - The JMS correlation ID header value or JMS error or nil if header is not set
    public extern function getCorrelationID() returns @tainted (string|error)?;

    # Gets JMS replyTo header from the message
    #
    # + return - The JMS replyTo Destination or JMS error or nil if header is not set
    public extern function getReplyTo() returns @tainted (Destination|error)?;

    # Set the replyTo destination from the message
    #
    # + replyTo - replyTo destination.
    # + return - nil or JMS error
    public extern function setReplyTo(Destination replyTo) returns error?;
};
