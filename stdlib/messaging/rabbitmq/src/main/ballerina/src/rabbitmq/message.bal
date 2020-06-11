// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/java;

# Provides the functionality to manipulate the messages received by the consumer services.
public type Message client object {
   handle amqpChannel = JAVA_NULL;
   private byte[] messageContent = [];
   private int deliveryTag = -1;
   private BasicProperties? properties = ();
   private boolean ackStatus = false;
   private boolean autoAck = true;

    # Acknowledges one or several received messages.
    # ```ballerina
    # rabbitmq:Error? ackResult = message->basicAck(true);
    # ```
    #
    # + multiple - `true` to acknowledge all messages up to and including the called on message and
    #              `false` to acknowledge just the called on message
    # + return - A `rabbitmq:Error` if an I/O error is encountered or else `()`
    public remote function basicAck(boolean multiple = false) returns Error? {
        var result = nativeBasicAck(self.amqpChannel, self.deliveryTag, multiple, self.autoAck, self.ackStatus);
        self.ackStatus = true;
        return result;
    }

    # Rejects one or several received messages.
    # ```ballerina
    # rabbitmq:Error? nackResult = message->basicNack(true, requeue = false);
    # ```
    #
    # + multiple - `true` to reject all messages up to and including the called on message and
    #              `false` to reject just the called on message
    # + requeue - `true` if the rejected message(s) should be re-queued rather than discarded/dead-lettered
    # + return - A `rabbitmq:Error` if an I/O error is encountered or else `()`
    public remote function basicNack(boolean multiple = false, public boolean requeue = true)
                            returns Error? {
        var result = nativeBasicNack(self.amqpChannel, self.deliveryTag, self.autoAck, self.ackStatus,
                                multiple, requeue);
        self.ackStatus = true;
        return result;
    }

    # Retrieves the delivery tag of the message.
    # ```ballerina
    # int deliveryTag = message.getDeliveryTag();
    # ```
    #
    # + return - The delivery tag of the message
    public function getDeliveryTag() returns @tainted int {
        if (self.deliveryTag > -1) {
            return self.deliveryTag;
        } else {
            RabbitMqError e = RabbitMqError("Delivery tag not properly initiliazed.");
            panic e;
        }
    }

    # Retrieves the properties of the message (i.e., routing headers etc.).
    # ```ballerina
    # rabbitmq:BasicProperties|rabbitmq:Error properties = message.getProperties();
    # ```
    #
    # + return - Properties of the message or else a `rabbitmq:Error` if an error is encountered
    public function getProperties() returns BasicProperties | Error {
        var basicProperties = self.properties;
        if (basicProperties is BasicProperties) {
            return basicProperties;
        }
        RabbitMqError e = RabbitMqError("Properties not properly initialized.");
        return e;
    }

# Retrieves the text content of the RabbitMQ message.
# ```ballerina
# string|rabbitmq:Error msgContent = message.getTextContent();
# ```
#
# + return - Message data as string value or else a `rabbitmq:Error` if an error is encountered
   public function getTextContent() returns @tainted string | Error {
        return nativeGetTextContent(self.messageContent);
   }

    # Retrieves the float content of the RabbitMQ message.
    # ```ballerina
    # float|rabbitmq:Error msgContent = message.getFloatContent();
    # ```
    #
    # + return - Message data as a float value or else a `rabbitmq:Error` if an error is encountered
    public function getFloatContent() returns @tainted float | Error {
        return  nativeGetFloatContent(self.messageContent);
    }

    # Retrieves the int content of the RabbitMQ message.
    # ```ballerina
    # int|rabbitmq:Error msgContent = message.getIntContent();
    # ```
    #
    # + return - Message data as an int value or else a `rabbitmq:Error` if an error is encountered
    public function getIntContent() returns @tainted int | Error {
       return nativeGetIntContent(self.messageContent);
    }

    # Retrieves the byte array content of the RabbitMQ message.
    # ```ballerina
    # byte[] msgContent = message.getIntContent();
    # ```
    #
    # + return - Message data as a byte array
    public function getByteArrayContent() returns @tainted byte[] {
        return self.messageContent;
    }

    # Retrieves the JSON content of the RabbitMQ message.
    # ```ballerina
    # json|rabbitmq:Error msgContent = message.getJSONContent();
    # ```
    #
    # + return - Message data as a JSON value or else a `rabbitmq:Error` if an error is encountered
    public function getJSONContent() returns @tainted json | Error {
        return nativeGetJSONContent(self.messageContent);
    }

    # Retrieves the XML content of the RabbitMQ message.
    # ```ballerina
    # xml|rabbitmq:Error msgContent = message.getXMLContent();
    # ```
    #
    # + return - Message data as an XML value or else a `rabbitmq:Error` if an error is encountered
    public function getXMLContent() returns @tainted xml | Error {
        return nativeGetXMLContent(self.messageContent);
    }
};

function nativeBasicAck(handle amqpChannel, int deliveryTag, boolean multiple, boolean ackMode, boolean ackStatus)
returns Error? =
@java:Method {
    name: "basicAck",
    class: "org.ballerinalang.messaging.rabbitmq.util.MessageUtils"
} external;

function nativeBasicNack(handle amqpChannel, int deliveryTag, boolean ackMode, boolean ackStatus, boolean multiple,
boolean requeue) returns Error? =
@java:Method {
    name: "basicNack",
    class: "org.ballerinalang.messaging.rabbitmq.util.MessageUtils"
} external;

function nativeGetTextContent(byte[] messageContent) returns string | Error =
@java:Method {
    name: "getTextContent",
    class: "org.ballerinalang.messaging.rabbitmq.util.MessageUtils"
} external;

function nativeGetFloatContent(byte[] messageContent) returns float | Error  =
@java:Method {
    name: "getFloatContent",
    class: "org.ballerinalang.messaging.rabbitmq.util.MessageUtils"
} external;

function nativeGetIntContent(byte[] messageContent) returns int | Error  =
@java:Method {
    name: "getIntContent",
    class: "org.ballerinalang.messaging.rabbitmq.util.MessageUtils"
} external;

function nativeGetXMLContent(byte[] messageContent) returns xml | Error  =
@java:Method {
    name: "getXMLContent",
    class: "org.ballerinalang.messaging.rabbitmq.util.MessageUtils"
} external;

function nativeGetJSONContent(byte[] messageContent) returns json | Error  =
@java:Method {
    name: "getJSONContent",
    class: "org.ballerinalang.messaging.rabbitmq.util.MessageUtils"
} external;
