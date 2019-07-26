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

# Represents a data channel for reading data.
public type ReadableDataChannel object {

    public function __init(ReadableByteChannel byteChannel, public ByteOrder bOrder = "BE") {
        self.init(byteChannel, bOrder);
    }

    #Initializes data channel.

    # +byteChannel - channel which would represent the source to read/write data
    # +bOrder - network byte order
    function init(ReadableByteChannel byteChannel, ByteOrder bOrder) = external;

    #Reads a 16 bit integer.

    # + return - value of the integer which is read or `Error` if any error occurred
    public function readInt16() returns int|Error = external;

    # Reads a 32 bit integer.

    # + return - value of the integer which is read or `Error` if any error occurred
    public function readInt32() returns int|Error = external;

    # Reads a 64 bit integer.

    # + return - value of the integer which is read or `Error` if any error occurred
    public function readInt64() returns int|Error = external;

    # Reads 32 bit float.

    # + return - value of the float which is read or `Error` if any error occurred
    public function readFloat32() returns float|Error = external;

    # Reads 64 bit float.

    # + return - value of the float which is read or `Error` if any error occurred
    public function readFloat64() returns float|Error = external;

    # Reads 1 byte and convert it's value to boolean.

    # + return - boolean value which is read or `Error` if any error occurred
    public function readBool() returns boolean|Error = external;

    # Reads string value represented through the provided number of bytes.

    # + nBytes - specifies the number of bytes which represents the string
    # + encoding - specifies the char-set encoding of the string
    # + return - value of the string or an error
    public function readString(int nBytes, string encoding) returns string|Error = external;

    # Reads a variable length integer.

    # + return - value of the integer which is read or `Error` if any error occurred
    public function readVarInt() returns int|Error = external;

    # Closes the data channel.

    # + return - nill if the channel is closed successfully or `Error` if any error occurred
    public function close() returns Error? = external;
};
