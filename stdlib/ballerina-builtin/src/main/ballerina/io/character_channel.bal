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

package ballerina.io;

@Description {value:"Ballerina CharacterChannel represents a channel which will allow to read/write characters"}
public type CharacterChannel object {
    private {
        ByteChannel channel;
        string charset;
    }

    new(channel, charset) {
        init(channel, charset);
    }

    @Description {value:"Initialize the character channel"}
    @Param {value:"channel : byte channel which will be used for reading/writing"}
    @Param {value:"charset : character which will be used to encode/decode bytes"}
    native function init(ByteChannel channel, string charset);

    @Description {value:"Function to read characters"}
    @Param {value:"numberOfChars: Number of characters which should be read"}
    @Return {value:"The character sequence which was read"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public native function read(@sensitive int numberOfChars) returns @tainted string|error;

    @Description {value:"Function to write characters"}
    @Param {value:"content: Text content which should be written"}
    @Param {value:"startOffset: If the content needs to be written with an offset, the value of that offset"}
    @Return {value:"Number of characters written"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public native function write(string content, int startOffset) returns int|error;

    @Description {value:"Function to convert a character channel to a JSON"}
    @Return {value:"Returns A JSON"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public native function readJson() returns @tainted json|error;

    @Description {value:"Function to convert a character channel to a XML"}
    @Return {value:"Returns A XML"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public native function readXml() returns @tainted xml|error;

    @Description {value:"Writes json through a given character channel"}
    public native function writeJson(json content) returns error?;

    @Description {value:"Writes xml through a given character channel"}
    public native function writeXml(xml content) returns error?;

    @Description {value:"Function to close a character channel"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public native function close() returns error?;
};
