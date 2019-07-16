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
import ballerina/'lang\.int;

# Represents ActiveMQ Artemis Message.
public type Message client object {

    private MessageType messageType = TEXT;
    private MessageConfiguration configuration;
    private MessageContent | Error content;
    private boolean created = false;
    private Session session;

    public function __init(Session session, @untainted MessageContent data, MessageConfiguration? config = ()) {
        self.session = session;
        self.content = data;
        if (config is MessageConfiguration) {
            self.configuration = config;
        } else {
            self.configuration = {};
        }
        if(!self.created) {
            self.initCreation(data);
        }
    }

    function initCreation(MessageContent data) {
        if (data is (string | json | xml | int | float | byte | boolean)) {
            self.messageType = TEXT;
        }
        if (data is io:ReadableByteChannel) {
            self.messageType = STREAM;
            self.createMessage(self.session, data, self.configuration);
        } else if (data is byte) {
            // Verify this.
            //self.createMessage(self.session, string.convert(int.convert(data)), self.configuration);
            self.createMessage(self.session, data.toString(), self.configuration);
        } else if (data is map<string | int | float | byte | boolean | byte[]>) {
            self.messageType = MAP;
            self.createMessage(self.session, data, self.configuration);
        } else if (data is xml) {
            self.createMessage(self.session, data.toString(), self.configuration);
        } else if (data is json) {
            self.createMessage(self.session, data.toString(), self.configuration);
        } else {
            self.messageType = BYTES;
            self.createMessage(self.session, data, self.configuration);
        }
    }

    function createMessage(Session session, string | byte[] | map<string | int | float | byte | boolean | byte[]> |
                            io:ReadableByteChannel data, MessageConfiguration config) = external;

    # Acknowledges reception of this message.
    #
    # + return - If an error occurred while acknowledging the message
    public remote function acknowledge() returns Error? = external;

    # Returns the size (in bytes) of this message's body.
    #
    # + return - the size of the message body
    public function getBodySize() returns int = external;

    # Add message property.
    #
    # + key - The name of the property
    # + value - The value of the property
    public function putProperty(string key, @untainted string | int | float | boolean | byte | byte[] value) = external;

    # Get a message property.
    #
    # + key - The name of the property
    # + return - The value of the property or nil if not found
    public function getProperty(string key) returns @tainted string | int | float | boolean | byte | byte[] | () |
                                                        Error = external;

    # The type of the message.
    #
    # + return - The `MessageType` of the message
    public function getType() returns MessageType = external;

    # The message payload.
    #
    # + return - The message payload or error on failure to retrieve payload or if the type is unsupported.
    #  A map payload can contain an error if the type is unsupported.
    public function getPayload() returns @tainted int | float | byte | boolean | string | map<string | int | float |
    byte | boolean | byte[]> | xml | json | byte[] | Error | () {
        var temp = self.content;
        if (temp is io:ReadableByteChannel) {
            Error err = error(message = "Use the saveToFile function for STREAM type message");
            return err;
        } else {
            return temp;
        }
    }

    # Call this function to save to a WritableByteChannel if the message is `STREAM` type.
    #
    # + ch - The byte channel to save to
    # + return - will return an `error` if the message is not of type `STREAM` or on failure
    public function saveToWritableByteChannel(@untainted io:WritableByteChannel ch) returns Error? = external;

    # Get the message configuration.
    #
    # + return - the `MessageConfiguration` of this message
    public function getConfig() returns MessageConfiguration {
        return self.configuration;
    }
};

# Represents a message sent and/or received by ActiveMQ Artemis.
#
# + expiration - The expiration time of this message 
# + timeStamp - The message timestamp
# + priority - the message priority (between 0 and 9 inclusive)
# + durable - whether the created message is durable or not
# + routingType - `RoutingType` of the message
# + groupId - used to group messages so that the same consumer receives all the messages with a particular groupId
# + groupSequence - can use to specify a sequence within the group
# + correlationId - a header for associating the current message with some previous message or application-specific ID
# + replyTo - indicates which address a JMS consumer should reply to
public type MessageConfiguration record {|
    int expiration = 0;
    int timeStamp = time:currentTime().time;
    byte priority = 0;
    boolean durable = true;
    RoutingType? routingType = ();
    string? groupId = ();
    int groupSequence = 0;
    string? correlationId = ();
    string? replyTo = ();
|};

# ActiveMQ Artemis message types.
public type MessageType TEXT | BYTES | MAP | STREAM | UNSUPPORTED;

# The text message type.
public const TEXT = "TEXT";
# The bytes message type.
public const BYTES = "BYTES";
# The map message type.
public const MAP = "MAP";
# The stream message type.
public const STREAM = "STREAM";
# If the message recieved is not of the supported message type in Ballerina it will have the type as UNSUPPORTED.
public const UNSUPPORTED = "UNSUPPORTED";
