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

# Represents a channel which could be used to write characters through a given WritableCharacterChannel.
public type WritableCharacterChannel object {

    private WritableByteChannel bChannel;
    private string charset;

    # Constructs a WritableByteChannel from a given WritableByteChannel and Charset.

    # + bChannel - WritableByteChannel which would be used to write characters
    # + charset - Character-Set which would be used to encode given bytes to characters
    public function __init(WritableByteChannel bChannel, string charset) {
        self.init(bChannel, charset);
    }

    # Initializes a character channel.
    #
    # + byteChannel - The WritableByteChannel which should be used to initialize the character channel
    # + cs - Character-set (i.e UTF-8) which should be used to encode
    function init(WritableByteChannel byteChannel, string cs) = external;

    # Writes a given sequence of characters (string).
    #
    # + content - Content which should be written
    # + startOffset - Number of characters which should be offset when writing content
    # + return - Content length that written or an error.
    public function write(string content, int startOffset) returns int|error = external;

    # Writes a given json to the given channel.
    #
    # + content - The json which should be written
    # + return - If an error occurred while writing
    public function writeJson(json content) returns error? = external;

    # Writes a given xml to the channel.
    #
    # + content - The XML which should be written
    # + return - If an error occurred while writing
    public function writeXml(xml content) returns error? = external;

    # Closes a given WritableCharacterChannel channel.
    #
    # + return - If an error occurred while writing
    public function close() returns error? = external;
};
