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
@final public ByteOrder BIG_ENDIAN = "BE";
@final public ByteOrder LITTLE_ENDIAN = "LE";

# Represents a WritableDataChannel for writing data.
public type WritableDataChannel object {

    public new(WritableByteChannel byteChannel, ByteOrder bOrder = "BE") {
        init(byteChannel, bOrder);
    }

    # Initializes data channel.
    #
    # + byteChannel - channel which would represent the source to read/write data
    # + bOrder - network byte order
    extern function init(WritableByteChannel byteChannel, ByteOrder bOrder);

    # Writes 16 bit integer.
    #
    # + value - integer which will be written
    # + return - nill if the content is written successfully or an error
    public extern function writeInt16(int value) returns error?;

    # Writes 32 bit integer.
    #
    # + value - integer which will be written
    # + return - nill if the content is written successfully or an error
    public extern function writeInt32(int value) returns error?;

    # Writes 64 bit integer.
    #
    # + value - integer which will be written
    # + return - nill if the content is written successfully or an error
    public extern function writeInt64(int value) returns error?;

    # Writes 32 bit float.
    #
    # + value - float which will be written
    # + return - nill if the float is written successfully or an error
    public extern function writeFloat32(float value) returns error?;

    # Writes 64 bit float.
    #
    # + value - float which will be written
    # + return - nill if the float is written successfully or an error
    public extern function writeFloat64(float value) returns error?;

    # Writes boolean.
    #
    # + value - boolean which will be written
    # + return - nill if the content is written successfully or an error
    public extern function writeBool(boolean value) returns error?;

    # Writes a given string value to the respective channel.
    #
    # + value - the value which should be written
    # + encoding - the encoding which will represent the value string
    # + return - nill if the content is written successfully or an error
    public extern function writeString(string value, string encoding) returns error?;

    # Reads a variable length integer.
    #
    # + return - value of the integer which is read or an error
    public extern function writeVarInt(int value) returns error?;

    # Closes the data channel.
    #
    # + return - nill if the channel is closed successfully or an i/o error
    public extern function close() returns error?;
};
