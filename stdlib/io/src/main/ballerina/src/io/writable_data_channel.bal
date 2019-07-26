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

# Represents network byte order.
#
# BIG_ENDIAN - specifies the bytes to be in the order of most significant byte first
#
# LITTLE_ENDIAN - specifies the byte order to be the least significant byte first
public type ByteOrder "BE"|"LE";
public const BIG_ENDIAN = "BE";
public const LITTLE_ENDIAN = "LE";

# Represents a WritableDataChannel for writing data.
public type WritableDataChannel object {

    public function __init(WritableByteChannel byteChannel, public ByteOrder bOrder = "BE") {
        self.init(byteChannel, bOrder);
    }

    # Initializes data channel.
    #
    # + byteChannel - channel which would represent the source to read/write data
    # + bOrder - network byte order
    function init(WritableByteChannel byteChannel, ByteOrder bOrder) = external;

    # Writes 16 bit integer.
    #
    # + value - integer which will be written
    # + return - nill if the content is written successfully or `Error` if any error occurred
    public function writeInt16(int value) returns Error? = external;

    # Writes 32 bit integer.
    #
    # + value - integer which will be written
    # + return - nill if the content is written successfully or `Error` if any error occurred
    public function writeInt32(int value) returns Error? = external;

    # Writes 64 bit integer.
    #
    # + value - integer which will be written
    # + return - nill if the content is written successfully or `Error` if any error occurred
    public function writeInt64(int value) returns Error? = external;

    # Writes 32 bit float.
    #
    # + value - float which will be written
    # + return - nill if the float is written successfully or `Error` if any error occurred
    public function writeFloat32(float value) returns Error? = external;

    # Writes 64 bit float.
    #
    # + value - float which will be written
    # + return - nill if the float is written successfully or `Error` if any error occurred
    public function writeFloat64(float value) returns Error? = external;

    # Writes boolean.
    #
    # + value - boolean which will be written
    # + return - nill if the content is written successfully or `Error` if any error occurred
    public function writeBool(boolean value) returns Error? = external;

    # Writes a given string value to the respective channel.
    #
    # + value - the value which should be written
    # + encoding - the encoding which will represent the value string
    # + return - nill if the content is written successfully or `Error` if any error occurred
    public function writeString(string value, string encoding) returns Error? = external;

    # Reads a variable length integer.
    #
    # + return - value of the integer which is read or `Error` if any error occurred
    public function writeVarInt(int value) returns Error? = external;

    # Closes the data channel.
    #
    # + return - nill if the channel is closed successfully or `Error` if any error occurred
    public function close() returns Error? = external;
};
