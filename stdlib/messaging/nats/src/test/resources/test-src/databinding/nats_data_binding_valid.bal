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

import ballerina/io;
import ballerina/lang.'string as strings;
import ballerina/nats;

type Entry record {
    int id;
    string name;
};

nats:Connection conn = new;

listener nats:StreamingListener lis = new(conn);

 @nats:StreamingSubscriptionConfig {
     subject: "demo"
 }
 service dataBindingByteConsumerService on lis {
      resource function onMessage(nats:StreamingMessage message, byte[] data) {
         io:println("Received Message - service: " + checkpanic strings:fromBytes(data));
     }

     resource function onError(nats:StreamingMessage message, error errorVal) {
         io:println("Error occurred!!!!");
     }
 }

 @nats:StreamingSubscriptionConfig {
     subject: "demo"
 }
 service dataBindingIntConsumerService on lis {
      resource function onMessage(nats:StreamingMessage message, int data) {
         io:println("Received Message - service: " + data.toString());
     }

     resource function onError(nats:StreamingMessage message, error errorVal) {
         io:println("Error occurred!!!!");
     }
 }

 @nats:StreamingSubscriptionConfig {
     subject: "demo"
 }
 service dataBindingFloatConsumerService on lis {
      resource function onMessage(nats:StreamingMessage message, float data) {
         io:println("Received Message - service: " + data.toString());
     }

     resource function onError(nats:StreamingMessage message, error errorVal) {
         io:println("Error occurred!!!!");
     }
 }

 @nats:StreamingSubscriptionConfig {
     subject: "demo"
 }
 service dataBindingBooleanConsumerService on lis {
      resource function onMessage(nats:StreamingMessage message, boolean data) {
         io:println("Received Message - service: " + data.toString());
     }

     resource function onError(nats:StreamingMessage message, error errorVal) {
         io:println("Error occurred!!!!");
     }
 }

 @nats:StreamingSubscriptionConfig {
     subject: "demo"
 }
 service dataBindingDecimalConsumerService on lis {
      resource function onMessage(nats:StreamingMessage message, decimal data) {
         io:println("Received Message - service: " + data.toString());
     }

     resource function onError(nats:StreamingMessage message, error errorVal) {
         io:println("Error occurred!!!!");
     }
 }

 @nats:StreamingSubscriptionConfig {
     subject: "demo"
 }
 service dataBindingXmlConsumerService on lis {
      resource function onMessage(nats:StreamingMessage message, xml data) {
         io:println("Received Message - service: " + data.toString());
     }

     resource function onError(nats:StreamingMessage message, error errorVal) {
         io:println("Error occurred!!!!");
     }
 }

@nats:StreamingSubscriptionConfig {
    subject: "demo"
}
service dataBindingJsonConsumerService on lis {
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
 service dataBindingRecordConsumerService on lis {
      resource function onMessage(nats:StreamingMessage message, Entry data) {
          json | error val = data.cloneWithType(typedesc<json>);
          if (val is json) {
             io:println("Received Message - service: " + val.toString());
          }
     }

     resource function onError(nats:StreamingMessage message, error errorVal) {
         io:println("Error occurred!!!!");
     }
 }
