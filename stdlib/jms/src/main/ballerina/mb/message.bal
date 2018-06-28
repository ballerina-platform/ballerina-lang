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

documentation {
Represent the MB message used to send and receive content from the a Ballerina Message Broker.

A message consist of a header and a body. The header contains fields used for message routing and
identification; the body contains the application data being sent.
}
public type Message object {

    private jms:Message message;

    public new(message) {}

    documentation { Gets the internal JMS message
        R{{}} JMS message}
    function getJMSMessage() returns @tainted jms:Message {
        return self.message;
    }

    documentation { Gets text content of the MB message
        R{{}} the string containing this message's data or an JMS error
    }
    public function getTextMessageContent() returns @tainted string|error {
        return self.message.getTextMessageContent();
    }

    documentation { Attach a string property to the message
        P{{key}} The string property name
        P{{value}} The string property value
        R{{}} nil or an MB error
    }
    public function setStringProperty(@sensitive string key, string value) returns error? {
        return self.message.setStringProperty(key, value);
    }

    documentation { Gets the attached string property from the message
        P{{key}} The string property name
        R{{}} The string property value, JMS error or nil if there is no property by this name
    }
    public function getStringProperty(@sensitive string key) returns @tainted (string|error)? {
        return self.message.getStringProperty(key);
    }

    documentation { Attach an integer property to the message
        P{{key}} The integer property name
        P{{value}} The integer property value
        R{{}} nil or an MB error
    }
    public function setIntProperty(@sensitive string key, int value) returns error? {
        return self.message.setIntProperty(key, value);
    }

    documentation { Gets the attached integer property from the message
        P{{key}} The integer property name
        R{{}} The integer property value or MB error
    }
    public function getIntProperty(@sensitive string key) returns @tainted int|error {
        return self.message.getIntProperty(key);
    }

    documentation { Attach an boolean property to the message
        P{{key}} The boolean property name
        P{{value}} The boolean property value
        R{{}} nil or an MB error
    }
    public function setBooleanProperty(@sensitive string key, boolean value) returns error? {
        return self.message.setBooleanProperty(key, value);
    }

    documentation { Gets the attached boolean property from the message
        P{{key}} The boolean property name
        R{{}} The boolean property value or MB error
    }
    public function getBooleanProperty(@sensitive string key) returns @tainted boolean|error {
        return self.message.getBooleanProperty(key);
    }

    documentation { Attach a float property to the message
        P{{key}} The float property name
        P{{value}} The float property value
        R{{}} nil or an MB error
    }
    public function setFloatProperty(@sensitive string key, float value) returns error? {
        return self.message.setFloatProperty(key, value);
    }

    documentation { Gets the attached float property from the message
        P{{key}} The float property name
        R{{}} The float property value or MB error
    }
    public function getFloatProperty(@sensitive string key) returns @tainted float|error {
        return self.message.getFloatProperty(key);
    }

    documentation { Gets the MessageID header from the message
        R{{}} The header value or MB error
    }
    public function getMessageID() returns @tainted string|error {
        return self.message.getMessageID();
    }

    documentation { Gets the Timestamp header from the message
        R{{}} The timestamp header value or MB error
    }
    public function getTimestamp() returns @tainted int|error {
        return self.message.getTimestamp();
    }

    documentation { Sets the DeliveryMode header in the message
        P{{mode}} The header value
        R{{}} nil or an MB error
    }
    public function setDeliveryMode(@sensitive int mode) returns error? {
        return self.message.setDeliveryMode(mode);
    }

    documentation { Gets the transport header DeliveryMode from the message
        R{{}} The delivery mode header value or MB error
    }
    public function getDeliveryMode() returns @tainted int|error {
        return self.message.getDeliveryMode();
    }

    documentation { Sets Expiration header to the message
        P{{value}} The header value
        R{{}} nil or an MB error
    }
    public function setExpiration(@sensitive int value) returns error? {
        return self.message.setExpiration(value);
    }

    documentation { Gets expiration header from the message
        R{{}} The expiration header value or MB error
    }
    public function getExpiration() returns @tainted int|error {
        return self.message.getExpiration();
    }

    documentation { Sets message type header to the message
        P{{messageType}} The message type header value
        R{{}} nil or an MB error if any JMS provider level internal error occur
    }
    public function setType(string messageType) returns error? {
        return self.message.setType(messageType);
    }

    documentation { Gets message type header from the message
        R{{}} The JMS message type header value or JMS error
    }
    public function getType() returns @tainted string|error {
        return self.message.getType();
    }

    documentation { Clear properties of the message
        R{{}} nil or an MB error
    }
    public function clearProperties() returns error? {
        return self.message.clearProperties();
    }

    documentation { Clears body of the message
        R{{}} nil or an MB error
    }
    public function clearBody() returns error? {
        return self.message.clearBody();
    }

    documentation { Sets priority header to the message
        P{{value}} The header value
        R{{}} nil or an MB error
    }
    public function setPriority(@sensitive int value) returns error? {
        return self.message.setPriority(value);
    }

    documentation { Gets priority header from the message
        R{{}} The priority header value or an MB error
    }
    public function getPriority() returns @tainted int|error {
        return self.message.getPriority();
    }

    documentation { Gets the redelivered header from the message
        R{{}} The redelivered header value or an MB error
    }
    public function getRedelivered() returns @tainted boolean|error {
        return self.message.getRedelivered();
    }

    documentation { Sets CorrelationID header to the message
        P{{value}} The header value
        R{{}} nil or an MB error
    }
    public function setCorrelationID(@sensitive string value) returns error? {
        return self.message.setCorrelationID(value);
    }

    documentation { Gets CorrelationID header from the message
        R{{}} The correlation ID header value or MB error or nil if header is not set
    }
    public function getCorrelationID() returns @tainted (string|error)? {
        return self.message.getCorrelationID();
    }
};
