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

# Represents a channel which could be used to write characters through a given WritableCharacterChannel.
public class WritableCharacterChannel {

    private WritableByteChannel bChannel;
    private string charset;

    # Constructs a `WritableByteChannel` from a given `WritableByteChannel` and `Charset`.
    #
    # + bChannel - The `WritableByteChannel`, which would be used to write the characters
    # + charset - The character set, which would be used to encode the given bytes to characters
    public function init(WritableByteChannel bChannel, string charset) {
        self.bChannel = bChannel;
        self.charset = charset;
        initWritableCharacterChannel(self, bChannel, charset);
    }

    # Writes a given sequence of characters (string).
    # ```ballerina
    # int|io:Error result = writableCharChannel.write("Content", 0);
    # ```
    #
    # + content - Content, which should be written
    # + startOffset - Number of characters, which should be offset when writing the content
    # + return - Content length that written or else `io:Error`
    public function write(string content, int startOffset) returns int|Error {
        return writeExtern(self, content, startOffset);
    }

    # Writes a given JSON to the given channel.
    # ```ballerina
    # io:Error? err = writableCharChannel.writeJson(inputJson, 0);
    # ```
    #
    # + content - The JSON, which should be written
    # + return - If an `io:Error` occurred while writing
    public function writeJson(json content) returns Error? {
        return writeJsonExtern(self, content);
    }

    # Writes a given XML to the channel.
    # ```ballerina
    # io:Error? err = writableCharChannel.writeXml(inputXml, 0);
    # ```
    #
    # + content - The XML, which should be written
    # + return - `()` or else `io:Error` if any error occurred
    public function writeXml(xml content) returns Error? {
        return writeXmlExtern(self, content);
    }

    # Writes a given key-valued pair `map<string>` to a property file.
    # ```ballerina
    # io:Error? err = writableCharChannel.writeProperties(properties);
    # ```
    # + properties - The map<string> that contains keys and values.
    # + comment - Comment describing the property list
    # + return - `()` or else `io:Error` if any error occurred
    public function writeProperties(map<string> properties, string comment) returns Error? {
        return writePropertiesExtern(self, properties, comment);
    }

    # Closes a given `WritableCharacterChannel` channel.
    # ```ballerina
    # io:Error err = writableCharChannel.close();
    # ```
    #
    # + return - `()` or else an `io:Error` if any error occurred
    public function close() returns Error? {
        return closeWritableCharacterChannel(self);
    }
}

function initWritableCharacterChannel(WritableCharacterChannel characterChannel, WritableByteChannel byteChannel,
                                      string charset) = @java:Method {
    name: "initCharacterChannel",
    'class: "org.ballerinalang.stdlib.io.nativeimpl.CharacterChannelUtils"
} external;

function writeExtern(WritableCharacterChannel characterChannel, string content, int startOffset)
                     returns int|Error = @java:Method {
    name: "write",
    'class: "org.ballerinalang.stdlib.io.nativeimpl.CharacterChannelUtils"
} external;

function writeJsonExtern(WritableCharacterChannel characterChannel, json content) returns Error? = @java:Method {
    name: "writeJson",
    'class: "org.ballerinalang.stdlib.io.nativeimpl.CharacterChannelUtils"
} external;

function writeXmlExtern(WritableCharacterChannel characterChannel, xml content) returns Error? = @java:Method {
    name: "writeXml",
    'class: "org.ballerinalang.stdlib.io.nativeimpl.CharacterChannelUtils"
} external;

function writePropertiesExtern(WritableCharacterChannel characterChannel, map<string> properties,
                                string comment) returns Error? = @java:Method {
    name: "writeProperties",
    'class: "org.ballerinalang.stdlib.io.nativeimpl.CharacterChannelUtils"
} external;

function closeWritableCharacterChannel(WritableCharacterChannel characterChannel) returns Error? = @java:Method {
    name: "close",
    'class: "org.ballerinalang.stdlib.io.nativeimpl.CharacterChannelUtils"
} external;
