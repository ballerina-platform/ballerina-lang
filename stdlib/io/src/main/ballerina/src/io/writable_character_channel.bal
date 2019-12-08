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

# Represents a channel which could be used to write characters through a given WritableCharacterChannel.
public type WritableCharacterChannel object {

    private WritableByteChannel bChannel;
    private string charset;

    # Constructs a WritableByteChannel from a given WritableByteChannel and Charset.

    # + bChannel - WritableByteChannel which would be used to write characters
    # + charset - Character-Set which would be used to encode given bytes to characters
    public function __init(WritableByteChannel bChannel, string charset) {
        self.bChannel = bChannel;
        self.charset = charset;
        initWritableCharacterChannel(self, bChannel, java:fromString(charset));
    }

    # Writes a given sequence of characters (string).
    #
    # + content - Content which should be written
    # + startOffset - Number of characters which should be offset when writing content
    # + return - Content length that written or `Error` if any error occurred
    public function write(string content, int startOffset) returns int|Error {
        return writeExtern(self, java:fromString(content), startOffset);
    }

    # Writes a given json to the given channel.
    #
    # + content - The json which should be written
    # + return - If an `Error` occurred while writing
    public function writeJson(json content) returns Error? {
        return writeJsonExtern(self, content);
    }

    # Writes a given xml to the channel.
    #
    # + content - The XML which should be written
    # + return - Nil or `Error` if any error occurred
    public function writeXml(xml content) returns Error? {
        return writeXmlExtern(self, content);
    }

    # Closes a given WritableCharacterChannel channel.
    #
    # + return - Nil or `Error` if any error occurred
    public function close() returns Error? {
        return closeWritableCharacterChannel(self);
    }
};

function initWritableCharacterChannel(WritableCharacterChannel characterChannel, WritableByteChannel byteChannel,
            handle charset) = @java:Method {
    name: "initCharacterChannel",
    class: "org.ballerinalang.stdlib.io.nativeimpl.CharacterChannelUtils"
} external;

function writeExtern(WritableCharacterChannel characterChannel, handle content, int startOffset) returns
            int|Error = @java:Method {
    name: "write",
    class: "org.ballerinalang.stdlib.io.nativeimpl.CharacterChannelUtils"
} external;

function writeJsonExtern(WritableCharacterChannel characterChannel, json content) returns Error? = @java:Method {
    name: "writeJson",
    class: "org.ballerinalang.stdlib.io.nativeimpl.CharacterChannelUtils"
} external;

function writeXmlExtern(WritableCharacterChannel characterChannel, xml content) returns Error? = @java:Method {
    name: "writeXml",
    class: "org.ballerinalang.stdlib.io.nativeimpl.CharacterChannelUtils"
} external;

function closeWritableCharacterChannel(WritableCharacterChannel characterChannel) returns Error? = @java:Method {
    name: "close",
    class: "org.ballerinalang.stdlib.io.nativeimpl.CharacterChannelUtils"
} external;
