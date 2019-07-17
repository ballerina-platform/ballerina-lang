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

# Public Ballerina API - Ballerina RabbitMQ Message.
public type Message client object {

   # Acknowledge one or several received messages.
   #
   # + multiple - `true` to acknowledge all messages up to and including the message called on,
   #                `false` to acknowledge just the message called on.
   # + return - An error if an I/O error is encountered or nil if successful.
   public remote function basicAck(boolean? multiple = false) returns Error? = external;

   # Reject one or several received messages.
   #
   # + multiple - `true` to reject all messages up to and including the message called on;
   #                `false` to reject just the message called on.
   # + requeue - `true` if the rejected message(s) should be requeued rather than discarded/dead-lettered.
   # + return - An error if an I/O error is encountered or nil if successful.
   public remote function basicNack(boolean? multiple = false, boolean? requeue = true)
                        returns Error? = external;

   # Retrieves the delivery tag of the message.
   #
   # + return - int containing the delivery tag of the message.
   public function getDeliveryTag() returns @tainted int = external;

   # Retrieves the properties of the message (i.e., routing headers etc.).
   #
   # + return - Properties of the message or error if an error is encountered.
   public function getProperties() returns BasicProperties | Error = external;

   # Retrieves the text content of the RabbitMQ message.
   #
   # + return - string containing message data or error if an error is encountered.
   public function getTextContent() returns @tainted string | Error = external;

   # Retrieves the int content of the RabbitMQ message.
   #
   # + return - int containing message data or error if an error is encountered.
   public function getIntContent() returns @tainted int | Error = external;

   # Retrieves the float content of the RabbitMQ message.
   #
   # + return - float containing message data or error if an error is encountered.
   public function getFloatContent() returns @tainted float | Error = external;

   # Retrieves the json content of the RabbitMQ message.
   #
   # + return - json containing message data or error if an error is encountered.
   public function getJSONContent() returns @tainted json | Error = external;

   # Retrieves the xml content of the RabbitMQ message.
   #
   # + return - xml containing message data or error if an error is encountered.
   public function getXMLContent() returns @tainted xml | Error = external;

   # Retrieves the byte array content of the RabbitMQ message.
   #
   # + return - byte array containing message data or error if an error is encountered.
   public function getByteArrayContent() returns @tainted byte[] = external;
};
