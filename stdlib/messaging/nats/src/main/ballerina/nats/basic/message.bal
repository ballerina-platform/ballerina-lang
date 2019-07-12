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

# Represents a message, which will be pushed from the NATS server to the consumer.
public type Message object {
    byte[] content;
    string? replyTo;
    string subject;


    public function __init(string subject, byte[] content, string? replyTo = ()) {
        self.content = content;
        self.subject = subject;
        self.replyTo = replyTo;
    }

    # Get the message content.
    #
    # + return - the data returned from the message as a 'byte[]'.
    public function getData() returns byte[] {
        return self.content;
    }

    # Get the replyTo subject of the message.
    #
    # + return - replyTo subject value.
    public function getReplyTo() returns string? {
        return self.replyTo;
    }

    # Get the subject of the message.
    #
    # + return - the subject that this message was sent to.
    public function getSubject() returns string {
        return self.subject;
    }
};
