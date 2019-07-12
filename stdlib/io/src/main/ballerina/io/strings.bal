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

# Represents a reader which will wrap string content as a channel.
public type StringReader object {
    private ReadableCharacterChannel? charChannel;

    # Constructs a channel to read string.
    #
    # + content - content which should be written
    # + encoding - encoding of the characters of the content
    public function __init(string content, string encoding = "UTF-8") {
        byte[] contentBytes = content.toByteArray(encoding);
        ReadableByteChannel byteChannel = checkpanic createReadableChannel(contentBytes);
        self.charChannel = new ReadableCharacterChannel(byteChannel, encoding);
    }

    # Reads string as json from reader.
    #
    # + return - json or `IOError` if any error occurred
    public function readJson() returns @tainted json|IOError {
        return self.charChannel.readJson();
    }

    # Reads string as XML from reader
    #
    # + return -
    public function readXml() returns @tainted xml|IOError? {
        return self.charChannel.readXml();
    }

    # Reads characters from the given string.
    #
    # + nCharacters - read specific number of characters
    # + return - string or `IOError` if any error occurred
    public function readChar(int nCharacters) returns @tainted string|IOError? {
        return self.charChannel.read(nCharacters);
    }

    # Closes reader.
    #
    # + return - An `IOError` if could not close the channel
    public function close() returns IOError? {
        return self.charChannel.close();
    }
};
