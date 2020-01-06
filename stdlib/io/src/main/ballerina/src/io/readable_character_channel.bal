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

#Represents a channel which could be used to read characters through a given ReadableByteChannel.
public type ReadableCharacterChannel object {

    private ReadableByteChannel byteChannel;
    private string charset;

    # Constructs a ReadableCharacterChannel from a given ReadableByteChannel and Charset.

    # + channel - ReadableByteChannel which would be used to read characters
    # + charset - Character-Set which would be used to encode/decode given bytes to characters
    public function __init(ReadableByteChannel byteChannel, string charset) {
        self.byteChannel = byteChannel;
        self.charset = charset;
        initReadableCharacterChannel(self, byteChannel, java:fromString(charset));
    }

    # Reads a given number of characters. This will attempt read up to `numberOfChars` characters from the channel.
    # `io:EofError` will return once channel reach to it end.
    #
    # + numberOfChars - Number of characters which should be read
    # + return - Content which is read or `EofError` once channel reach to it end. `Error` if any error occurred.
    public function read(@untainted int numberOfChars) returns @tainted string|Error {
        handle|Error result = readExtern(self, numberOfChars);
        if (result is handle) {
            return <string>java:toString(result);
        } else {
            return result;
        }
    }

    # Reads a json from the given channel.
    #
    # + return - Read json string or `Error` if any error occurred
    public function readJson() returns @tainted json|Error {
        return readJsonExtern(self);
    }

    # Reads a XML from the given channel.
    #
    # + return - Read xml or `Error` if any error occurred
    public function readXml() returns @tainted xml|Error {
        return readXmlExtern(self);
    }

    # Closes a given character channel.
    #
    # + return - If an error occurred while writing
    public function close() returns Error? {
        return closeReadableCharacterChannel(self);
    }
};

function initReadableCharacterChannel(ReadableCharacterChannel characterChannel, ReadableByteChannel byteChannel,
            handle charset) = @java:Method {
    name: "initCharacterChannel",
    class: "org.ballerinalang.stdlib.io.nativeimpl.CharacterChannelUtils"
} external;

function readExtern(ReadableCharacterChannel characterChannel, @untainted int numberOfChars) returns
            @tainted handle|Error = @java:Method {
    name: "read",
    class: "org.ballerinalang.stdlib.io.nativeimpl.CharacterChannelUtils"
} external;

function readJsonExtern(ReadableCharacterChannel characterChannel) returns @tainted json|Error = @java:Method {
    name: "readJson",
    class: "org.ballerinalang.stdlib.io.nativeimpl.CharacterChannelUtils"
} external;

function readXmlExtern(ReadableCharacterChannel characterChannel) returns @tainted xml|Error = @java:Method {
    name: "readXml",
    class: "org.ballerinalang.stdlib.io.nativeimpl.CharacterChannelUtils"
} external;

function closeReadableCharacterChannel(ReadableCharacterChannel characterChannel) returns Error? = @java:Method {
    name: "close",
    class: "org.ballerinalang.stdlib.io.nativeimpl.CharacterChannelUtils"
} external;
