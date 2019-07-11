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

import ballerina/nats;
import ballerina/io;
import ballerina/encoding;

type Entry record {
    int id;
    string name;
};

nats:Connection conn = new("localhost:4222");

listener nats:StreamingListener lisBytes = new(conn, "test-cluster", "bytes");
listener nats:StreamingListener lisString = new(conn, "test-cluster", "strings");
listener nats:StreamingListener lisInt = new(conn, "test-cluster", "ints");
listener nats:StreamingListener lisFloat = new(conn, "test-cluster", "floats");
listener nats:StreamingListener lisBoolean = new(conn, "test-cluster", "booleans");
listener nats:StreamingListener lisDecimal = new(conn, "test-cluster", "decimals");
listener nats:StreamingListener lisXml = new(conn, "test-cluster", "xmls");
listener nats:StreamingListener lisJson = new(conn, "test-cluster", "jsons");
listener nats:StreamingListener lisRecord = new(conn, "test-cluster", "records");

 @nats:StreamingSubscriptionConfig {
     subject: "demo"
 }
 service dataBindingByteConsumerService on lisBytes {
      resource function onMessage(nats:StreamingMessage message, byte[] data) {
         io:println("Received Message - service: " + encoding:byteArrayToString(data));
     }

     resource function onError(nats:StreamingMessage message, error errorVal) {
         io:println("Error occurred!!!!");
     }
 }

 @nats:StreamingSubscriptionConfig {
     subject: "demo"
 }
 service dataBindingIntConsumerService on lisInt {
      resource function onMessage(nats:StreamingMessage message, int data) {
         io:println("Received Message - service: " + data);
     }

     resource function onError(nats:StreamingMessage message, error errorVal) {
         io:println("Error occurred!!!!");
     }
 }

 @nats:StreamingSubscriptionConfig {
     subject: "demo"
 }
 service dataBindingFloatConsumerService on lisFloat {
      resource function onMessage(nats:StreamingMessage message, float data) {
         io:println("Received Message - service: " + data);
     }

     resource function onError(nats:StreamingMessage message, error errorVal) {
         io:println("Error occurred!!!!");
     }
 }

 @nats:StreamingSubscriptionConfig {
     subject: "demo"
 }
 service dataBindingBooleanConsumerService on lisBoolean {
      resource function onMessage(nats:StreamingMessage message, boolean data) {
         io:println("Received Message - service: " + data);
     }

     resource function onError(nats:StreamingMessage message, error errorVal) {
         io:println("Error occurred!!!!");
     }
 }

 @nats:StreamingSubscriptionConfig {
     subject: "demo"
 }
 service dataBindingDecimalConsumerService on lisDecimal {
      resource function onMessage(nats:StreamingMessage message, decimal data) {
         io:println("Received Message - service: " + data);
     }

     resource function onError(nats:StreamingMessage message, error errorVal) {
         io:println("Error occurred!!!!");
     }
 }

 @nats:StreamingSubscriptionConfig {
     subject: "demo"
 }
 service dataBindingXmlConsumerService on lisXml {
      resource function onMessage(nats:StreamingMessage message, xml data) {
         io:println("Received Message - service: " + string.convert(data));
     }

     resource function onError(nats:StreamingMessage message, error errorVal) {
         io:println("Error occurred!!!!");
     }
 }

@nats:StreamingSubscriptionConfig {
    subject: "demo"
}
service dataBindingJsonConsumerService on lisJson {
     resource function onMessage(nats:StreamingMessage message, json data) {
        io:println("Received Message - service");
    }

    resource function onError(nats:StreamingMessage message, error errorVal) {
        io:println("Error occurred!!!!");
    }
}

 @nats:StreamingSubscriptionConfig {
     subject: "demo"
 }
 service dataBindingRecordConsumerService on lisRecord {
      resource function onMessage(nats:StreamingMessage message, Entry data) {
          json | error val = json.convert(data);
          if (val is json) {
             io:println("Received Message - service: " + checkpanic string.convert(val));
          }
     }

     resource function onError(nats:StreamingMessage message, error errorVal) {
         io:println("Error occurred!!!!");
     }
 }