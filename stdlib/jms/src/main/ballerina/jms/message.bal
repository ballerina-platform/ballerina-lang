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
    # + return - The string containing this message's data or a JMS error
    public function getTextMessageContent() returns @tainted string|error = external;

    # Gets map content of the JMS message
    #
    # + return - The string containing this message's data or a JMS error
    public function getMapMessageContent() returns @tainted map<any>|error = external;

    # Sets a JMS transport string property from the message
    #
    # + key - The string property name
    # + value - The string property value
    # + return - Nil or a JMS error
    public function setStringProperty(@untainted string key, string value) returns error? = external;

    # Gets a JMS transport string property from the message
    #
    # + key - The string property name
    # + return - The string property value, JMS error or nil if there is no property by this name
    public function getStringProperty(@untainted string key) returns @tainted string|error? = external;

    # Sets a JMS transport integer property from the message
    #
    # + key - The integer property name
    # + value - The integer property value
    # + return - Nil or a JMS error
    public function setIntProperty(@untainted string key, int value) returns error? = external;

    # Gets a JMS transport integer property from the message
    #
    # + key - The integer property name
    # + return - The integer property value or JMS error
    public function getIntProperty(@untainted string key) returns @tainted int|error = external;

    # Sets a JMS transport boolean property from the message
    #
    # + key - The boolean property name
    # + value - The boolean property value
    # + return - Nil or a JMS error
    public function setBooleanProperty(@untainted string key, boolean value) returns error? = external;

    # Gets a JMS transport boolean property from the message
    #
    # + key - The boolean property name
    # + return - The boolean property value or JMS error
    public function getBooleanProperty(@untainted string key) returns @tainted boolean|error = external;

    # Sets a JMS transport float property from the message
    #
    # + key - The float property name
    # + value - The float property value
    # + return - Nil or a JMS error
    public function setFloatProperty(@untainted string key, float value) returns error? = external;

    # Gets a JMS transport float property from the message
    #
    # + key - The float property name
    # + return - The float property value or JMS error
    public function getFloatProperty(@untainted string key) returns @tainted float|error = external;

    # Gets JMS transport header MessageID from the message
    #
    # + return - The header value or JMS error
    public function getMessageID() returns @tainted string|error = external;

    # Gets JMS transport header Timestamp from the message
    #
    # + return - The timestamp header value or JMS error
    public function getTimestamp() returns @tainted int|error = external;

    # Sets DeliveryMode JMS transport header to the message
    #
    # + mode - The header value
    # + return - nil or a JMS error
    public function setDeliveryMode(int mode) returns error? = external;

    # Get JMS transport header DeliveryMode from the message
    #
    # + return - The delivery mode header value or JMS error
    public function getDeliveryMode() returns @tainted int|error = external;

    # Sets Expiration JMS transport header to the message
    #
    # + time - The expiration time header value
    # + return - nil or a JMS error
    public function setExpiration(int time) returns error? = external;

    # Gets JMS transport header Expiration from the message
    #
    # + return - The expiration header value or JMS error
    public function getExpiration() returns @tainted int|error = external;

    # Sets Type JMS transport header to the message
    #
    # + messageType - The message type header value
    # + return - nil or an JMS error if any JMS provider level internal error occur
    public function setType(string messageType) returns error? = external;

    # Gets JMS transport header Type from the message
    #
    # + return - The JMS message type header value or JMS error
    public function getType() returns @tainted string|error? = external;

    # Clears JMS properties of the message
    #
    # + return - nil or error if any JMS provider level internal error occur
    public function clearProperties() returns error? = external;

    # Clears body of the JMS message
    #
    # + return - nil or a JMS error
    public function clearBody() returns error? = external;

    # Sets priority JMS transport header to the message
    #
    # + priority - The priority header value
    # + return - nil or a JMS error
    public function setPriority(int priority) returns error? = external;

    # Gets JMS transport header Priority from the message
    #
    # + return - The JMS priority header value or error
    public function getPriority() returns @tainted int|error = external;

    # Gets JMS transport header Redelivered from the message
    #
    # + return - The JMS redelivered header value or JMS error
    public function getRedelivered() returns @tainted boolean|error = external;

    # Sets CorrelationID JMS transport header to the message
    #
    # + correlationId - The correlationId header value
    # + return - nil or a JMS error
    public function setCorrelationID(string correlationId) returns error? = external;

    # Gets JMS transport header CorrelationID from the message
    #
    # + return - The JMS correlation ID header value or JMS error or nil if header is not set
    public function getCorrelationID() returns @tainted string|error? = external;

    # Gets JMS replyTo header from the message
    #
    # + return - The JMS replyTo Destination or JMS error or nil if header is not set
    public function getReplyTo() returns @tainted Destination|error? = external;

    # Set the replyTo destination from the message
    #
    # + replyTo - replyTo destination.
    # + return - nil or JMS error
    public function setReplyTo(Destination replyTo) returns error? = external;
};
