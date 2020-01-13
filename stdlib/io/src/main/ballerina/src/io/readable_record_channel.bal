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

import ballerinax/java;

# Represents a channel which will allow to read
public type ReadableTextRecordChannel object {

    private ReadableCharacterChannel charChannel;
    private string rs;
    private string fs;

    # Constructs a ReadableTextRecordChannel from a given ReadableCharacterChannel.
    #
    # + charChannel - CharacterChannel which will point to the input/output resource
    # + fs - Field separator (this could be a regex)
    # + rs - Record separator (this could be a regex)
    public function __init(ReadableCharacterChannel charChannel, public string fs = "", public string rs = "",
                           public string fmt = "default") {
        self.charChannel = charChannel;
        self.rs = rs;
        self.fs = fs;
        initReadableTextRecordChannel(self, charChannel, java:fromString(fs), java:fromString(rs), java:fromString(fmt));
    }

    # Checks whether there's a record left to be read.
    #
    # + return - True if there's a record left to be read
    public function hasNext() returns boolean {
        return hasNextExtern(self);
    }

    # Get next record from the input/output resource.
    #
    # + return - Set of fields included in the record or `Error` if any error occurred
    public function getNext() returns @tainted string[]|Error {
        handle[]|Error result = getNextExtern(self);
        if (result is Error) {
            return result;
        } else {
            string[] records = [];
            foreach handle v in result {
                records.push(<string>java:toString(v));
            }
            return records;
        }
    }

    # Closes a given record channel.
    #
    # + return - An `Error` if the record channel could not be closed properly
    public function close() returns Error? {
        return closeReadableTextRecordChannelExtern(self);
    }
};

function initReadableTextRecordChannel(ReadableTextRecordChannel textChannel, ReadableCharacterChannel charChannel,
            handle fs, handle rs, handle fmt) = @java:Method {
    name: "initRecordChannel",
    class: "org.ballerinalang.stdlib.io.nativeimpl.RecordChannelUtils"
} external;

function hasNextExtern(ReadableTextRecordChannel textChannel) returns boolean = @java:Method {
    name: "hasNext",
    class: "org.ballerinalang.stdlib.io.nativeimpl.RecordChannelUtils"
} external;

function getNextExtern(ReadableTextRecordChannel textChannel) returns @tainted handle[]|Error = @java:Method {
    name: "getNext",
    class: "org.ballerinalang.stdlib.io.nativeimpl.RecordChannelUtils"
} external;

function closeReadableTextRecordChannelExtern(ReadableTextRecordChannel textChannel) returns Error? = @java:Method {
    name: "close",
    class: "org.ballerinalang.stdlib.io.nativeimpl.RecordChannelUtils"
} external;
