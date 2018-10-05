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

import ballerina/jms;

# Represent the MB message used to send and receive content from the a Ballerina Message Broker.
#
# A message consist of a header and a body. The header contains fields used for message routing and
# identification; the body contains the application data being sent.
public type Message object {

    private jms:Message message;

    public new(message) {}

    # Gets the internal JMS message
    #
    # + return - JMS message}
    function getJMSMessage() returns @tainted jms:Message {
        return self.message;
    }

    # Gets text content of the MB message
    #
    # + return - the string containing this message's data or an JMS error
    public function getTextMessageContent() returns @tainted string|error {
        return self.message.getTextMessageContent();
    }

    # Attach a string property to the message
    #
    # + key - The string property name
    # + value - The string property value
    # + return - nil or an MB error
    public function setStringProperty(@sensitive string key, string value) returns error? {
        return self.message.setStringProperty(key, value);
    }

    # Gets the attached string property from the message
    #
    # + key - The string property name
    # + return - The string property value, JMS error or nil if there is no property by this name
    public function getStringProperty(@sensitive string key) returns @tainted (string|error)? {
        return self.message.getStringProperty(key);
    }

    # Attach an integer property to the message
    #
    # + key - The integer property name
    # + value - The integer property value
    # + return - nil or an MB error
    public function setIntProperty(@sensitive string key, int value) returns error? {
        return self.message.setIntProperty(key, value);
    }

    # Gets the attached integer property from the message
    #
    # + key - The integer property name
    # + return - The integer property value or MB error
    public function getIntProperty(@sensitive string key) returns @tainted int|error {
        return self.message.getIntProperty(key);
    }

    # Attach an boolean property to the message
    #
    # + key - The boolean property name
    # + value - The boolean property value
    # + return - nil or an MB error
    public function setBooleanProperty(@sensitive string key, boolean value) returns error? {
        return self.message.setBooleanProperty(key, value);
    }

    # Gets the attached boolean property from the message
    #
    # + key - The boolean property name
    # + return - The boolean property value or MB error
    public function getBooleanProperty(@sensitive string key) returns @tainted boolean|error {
        return self.message.getBooleanProperty(key);
    }

    # Attach a float property to the message
    #
    # + key - The float property name
    # + value - The float property value
    # + return - nil or an MB error
    public function setFloatProperty(@sensitive string key, float value) returns error? {
        return self.message.setFloatProperty(key, value);
    }

    # Gets the attached float property from the message
    #
    # + key - The float property name
    # + return - The float property value or MB error
    public function getFloatProperty(@sensitive string key) returns @tainted float|error {
        return self.message.getFloatProperty(key);
    }

    # Gets the MessageID header from the message
    #
    # + return - The header value or MB error
    public function getMessageID() returns @tainted string|error {
        return self.message.getMessageID();
    }

    # Gets the Timestamp header from the message
    #
    # + return - The timestamp header value or MB error
    public function getTimestamp() returns @tainted int|error {
        return self.message.getTimestamp();
    }

    # Sets the DeliveryMode header in the message
    #
    # + mode - The header value
    # + return - nil or an MB error
    public function setDeliveryMode(@sensitive int mode) returns error? {
        return self.message.setDeliveryMode(mode);
    }

    # Gets the transport header DeliveryMode from the message
    #
    # + return - The delivery mode header value or MB error
    public function getDeliveryMode() returns @tainted int|error {
        return self.message.getDeliveryMode();
    }

    # Sets Expiration header to the message
    #
    # + value - The header value
    # + return - nil or an MB error
    public function setExpiration(@sensitive int value) returns error? {
        return self.message.setExpiration(value);
    }

    # Gets expiration header from the message
    #
    # + return - The expiration header value or MB error
    public function getExpiration() returns @tainted int|error {
        return self.message.getExpiration();
    }

    # Sets message type header to the message
    #
    # + messageType - The message type header value
    # + return - nil or an MB error if any JMS provider level internal error occur
    public function setType(string messageType) returns error? {
        return self.message.setType(messageType);
    }

    # Gets message type header from the message
    #
    # + return - The JMS message type header value or JMS error
    public function getType() returns @tainted string|error {
        return self.message.getType();
    }

    # Clear properties of the message
    #
    # + return - nil or an MB error
    public function clearProperties() returns error? {
        return self.message.clearProperties();
    }

    # Clears body of the message
    #
    # + return - nil or an MB error
    public function clearBody() returns error? {
        return self.message.clearBody();
    }

    # Sets priority header to the message
    #
    # + value - The header value
    # + return - nil or an MB error
    public function setPriority(@sensitive int value) returns error? {
        return self.message.setPriority(value);
    }

    # Gets priority header from the message
    # + return - The priority header value or an MB error
    public function getPriority() returns @tainted int|error {
        return self.message.getPriority();
    }

    # Gets the redelivered header from the message
    #
    # + return - The redelivered header value or an MB error
    public function getRedelivered() returns @tainted boolean|error {
        return self.message.getRedelivered();
    }

    # Sets CorrelationID header to the message
    #
    # + value - The header value
    # + return - nil or an MB error
    public function setCorrelationID(@sensitive string value) returns error? {
        return self.message.setCorrelationID(value);
    }

    # Gets CorrelationID header from the message
    #
    # + return - The correlation ID header value or MB error or nil if header is not set
    public function getCorrelationID() returns @tainted (string|error)? {
        return self.message.getCorrelationID();
    }
};
