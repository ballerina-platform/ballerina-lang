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

# Represents the message, which a NATS server sends to its subscribed services.
public type Message object {
    byte[] content;
    string? replyTo;
    string subject;

    function init(string subject, byte[] content, string? replyTo = ()) {
        self.content = content;
        self.subject = subject;
        self.replyTo = replyTo;
    }

    # Gets the message content.
    #
    # + return - The data returned from the message as a 'byte[]'
    public function getData() returns byte[] {
        return self.content;
    }

    # Gets the `replyTo` subject of the message.
    #
    # + return - The subject to which the client is expected to send a reply message 
    public function getReplyTo() returns string? {
        return self.replyTo;
    }

    # Gets the subject to which the message was sent to.
    #
    # + return - The subject that this message was sent to.
    public function getSubject() returns string {
        return self.subject;
    }
};
