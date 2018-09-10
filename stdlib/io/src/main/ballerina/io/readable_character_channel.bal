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


#Represents a channel which could be used to read/write characters through a given ByteChannel.
public type ReadableCharacterChannel object {

    private ReadableByteChannel channel;
    private string charset;

    # Constructs a CharacterChannel from a given ByteChannel and Charset.

    # + channel - ByteChannel which would be used to read/write characters
    # + charset - Character-Set which would be used to encode/decode given bytes to characters
    public new(channel, charset) {
        init(channel, charset);
    }

    # Initializes a character channel.
    #
    # + byteChannel - ByteChannel which should be used to initalize the character channel
    # + cs - Character-set (i.e UTF-8) which should be used to encode/decode
    extern function init(ReadableByteChannel byteChannel, string cs);

    # Reads a given number of characters.
    #
    # + numberOfChars - Number of characters which should be read
    # + return - Content which is read or an error
    public extern function read(@sensitive int numberOfChars) returns @tainted string|error;

    # Reads a json from the given channel.
    #
    # + return - Read json string or an error
    public extern function readJson() returns @tainted json|error;

    # Reads a XML from the given channel.
    #
    # + return - Read xml or an error
    public extern function readXml() returns @tainted xml|error;

    # Closes a given character channel.
    #
    # + return - If an error occurred while writing
    public extern function close() returns error?;
};
