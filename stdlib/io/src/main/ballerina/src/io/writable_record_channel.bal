// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Represents a channel which will allow to write records through a given WritableCharacterChannel.
public type WritableTextRecordChannel object {
    private WritableCharacterChannel characterChannel;
    private string rs;
    private string fs;

    # Constructs a DelimitedTextRecordChannel from a given WritableCharacterChannel.

    # + channel - WritableCharacterChannel which will point to the input/output resource
    # + rs - Record separator (this could be a regex)
    # + fs - Field separator (this could be a regex)
    public function __init(WritableCharacterChannel characterChannel, public string fs = "", public string rs = "",
                           public string fmt = "default") {
        self.characterChannel = characterChannel;
        self.fs = fs;
        self.rs = rs;
        initWritableTextRecordChannel(self, characterChannel, java:fromString(fs), java:fromString(rs), java:fromString(fmt));
    }

    # Writes records to a given output resource.

    # + textRecord - List of fields to be written
    # + return - An `Error` if the records could not be written properly
    public function write(string[] textRecord) returns Error? {
        handle[] records = [];
            foreach string v in textRecord {
                records.push(java:fromString(v));
            }
        return writeRecordExtern(self, records);
    }

    # Closes a given record channel.

    # + return - An `Error` if the record channel could not be closed properly
    public function close() returns Error? {
        return closeWritableTextRecordChannelExtern(self);
    }
};

function initWritableTextRecordChannel(WritableTextRecordChannel textChannel, WritableCharacterChannel charChannel,
            handle fs, handle rs, handle fmt) = @java:Method {
    name: "initRecordChannel",
    class: "org.ballerinalang.stdlib.io.nativeimpl.RecordChannelUtils"
} external;

function writeRecordExtern(WritableTextRecordChannel textChannel, handle[] textRecord) returns Error? = @java:Method {
    name: "write",
    class: "org.ballerinalang.stdlib.io.nativeimpl.RecordChannelUtils"
} external;

function closeWritableTextRecordChannelExtern(WritableTextRecordChannel textChannel) returns Error? = @java:Method {
    name: "close",
    class: "org.ballerinalang.stdlib.io.nativeimpl.RecordChannelUtils"
} external;
