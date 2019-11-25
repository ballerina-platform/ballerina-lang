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

import ballerinax/java;

# Represents the message a NATS Streaming Server sends to a subscription service.
public type StreamingMessage client object {
   private byte[] content;
   private string subject;

   function __init(string subject, byte[] content) {
       self.subject = subject;
       self.content = content;
   }

   # Get the message content.
   #
   # + return - The data from the message as a 'byte[]'.
   public function getData() returns byte[] {
       return self.content;
   }

   # Get the subject.
   #
   # + return - The subject that this message was sent to.
   public function getSubject() returns string {
       return self.subject;
   }

   # Acknowledge the NATS Streaming server upon the receipt of the message.
   #
   # + return - Returns () if the acknowledgment completes successfully or an error.
   public remote function ack() returns Error? {
       return externAck(self);
   }
};

function externAck(StreamingMessage message) returns Error? =
@java:Method {
    class: "org.ballerinalang.nats.streaming.message.Ack"
} external;
