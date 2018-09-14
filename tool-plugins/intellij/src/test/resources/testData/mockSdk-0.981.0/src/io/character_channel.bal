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

documentation {
    Represents a channel which could be used to read/write characters through a given ByteChannel.
}
public type CharacterChannel object {
    private ByteChannel channel;
    private string charset;

    documentation {
        Constructs a CharacterChannel from a given ByteChannel and Charset.

        P{{byteChannel}} ByteChannel which would be used to read/write characters
        P{{charset}} Character-Set which would be used to encode/decode given bytes to characters
    }
    public new(byteChannel, charset) {
        init(byteChannel, charset);
    }

    documentation {
        Initializes a character channel.

        P{{byteChannel}} ByteChannel which should be used to initalize the character channel
        P{{cs}} Character-set (i.e UTF-8) which should be used to encode/decode
    }
    extern function init(ByteChannel byteChannel, string cs);

    documentation {
        Reads a given number of characters.

        P{{numberOfChars}} Number of characters which should be read
        R{{}} Content which is read or an error
    }
    public extern function read(@sensitive int numberOfChars) returns @tainted string|error;

    documentation {
        Writes a given sequence of characters (string).

        P{{content}} Content which should be written
        P{{startOffset}} Number of characters which should be offset when writing content
    }
    public extern function write(string content, int startOffset) returns int|error;

    documentation {
        Reads a json from the given channel.

        R{{}} Read json string or an error
    }
    public extern function readJson() returns @tainted json|error;

    documentation {
        Reads a XML from the given channel.

        R{{}} Read xml or an error
    }
    public extern function readXml() returns @tainted xml|error;

    documentation {
        Writes a given json to the given channel.

        P{{content}} The json which should be written
        R{{}} If an error occurred while writing
    }
    public extern function writeJson(json content) returns error?;

    documentation {
        Writes a given xml to the channel.

        P{{content}} The XML which should be written
        R{{}} If an error occurred while writing
    }
    public extern function writeXml(xml content) returns error?;

    documentation {
        Closes a given character channel.

        R{{}} If an error occurred while writing
    }
    public extern function close() returns error?;
};
