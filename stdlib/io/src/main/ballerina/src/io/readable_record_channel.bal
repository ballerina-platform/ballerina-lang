// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Represents a channel which will allow to read
public class ReadableTextRecordChannel {

    private ReadableCharacterChannel charChannel;
    private string rs;
    private string fs;

    # Constructs a ReadableTextRecordChannel from a given ReadableCharacterChannel.
    #
    # + charChannel - CharacterChannel which will point to the input/output resource
    # + fs - Field separator (this could be a regex)
    # + rs - Record separator (this could be a regex)
    public function init(ReadableCharacterChannel charChannel, string fs = "", string rs = "",
                           string fmt = "default") {
        self.charChannel = charChannel;
        self.rs = rs;
        self.fs = fs;
        initReadableTextRecordChannel(self, charChannel, fs, rs, fmt);
    }

# Checks whether there's a record left to be read.
# ```ballerina
# boolean hasNext = readableRecChannel.hasNext();
# ```
#
# + return - True if there's a record left to be read
    public function hasNext() returns boolean {
        return hasNextExtern(self);
    }

# Get the next record from the input/output resource.
# ```ballerina
# string[]|io:Error record = readableRecChannel.getNext();
# ```
#
# + return - Set of fields included in the record or else `io:Error`
    public function getNext() returns @tainted string[]|Error {
        return getNextExtern(self);
    }

# Closes a given record channel.
# ```ballerina
# io:Error err = readableRecChannel.close();
# ```
#
# + return - An `io:Error` if the record channel could not be closed properly
    public function close() returns Error? {
        return closeReadableTextRecordChannelExtern(self);
    }
}

function initReadableTextRecordChannel(ReadableTextRecordChannel textChannel, ReadableCharacterChannel charChannel,
                                       string fs, string rs, string fmt) = @java:Method {
    name: "initRecordChannel",
    'class: "org.ballerinalang.stdlib.io.nativeimpl.RecordChannelUtils"
} external;

function hasNextExtern(ReadableTextRecordChannel textChannel) returns boolean = @java:Method {
    name: "hasNext",
    'class: "org.ballerinalang.stdlib.io.nativeimpl.RecordChannelUtils"
} external;

function getNextExtern(ReadableTextRecordChannel textChannel) returns @tainted string[]|Error = @java:Method {
    name: "getNext",
    'class: "org.ballerinalang.stdlib.io.nativeimpl.RecordChannelUtils"
} external;

function closeReadableTextRecordChannelExtern(ReadableTextRecordChannel textChannel) returns Error? = @java:Method {
    name: "close",
    'class: "org.ballerinalang.stdlib.io.nativeimpl.RecordChannelUtils"
} external;
