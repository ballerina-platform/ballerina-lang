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

    public new(ReadableByteChannel byteChannel, ByteOrder bOrder = "BE") {
        self.init(byteChannel, bOrder);
    }

    #Initializes data channel.

    # +byteChannel - channel which would represent the source to read/write data
    # +bOrder - network byte order
    extern function init(ReadableByteChannel byteChannel, ByteOrder bOrder);

    #Reads a 16 bit integer.

    # + return - value of the integer which is read or an error
    public extern function readInt16() returns int|error;

    # Reads a 32 bit integer.

    # + return - value of the integer which is read or an error
    public extern function readInt32() returns int|error;

    # Reads a 64 bit integer.

    # + return - value of the integer which is read or an error
    public extern function readInt64() returns int|error;

    # Reads 32 bit float.

    # + return - value of the float which is read or an error
    public extern function readFloat32() returns float|error;

    # Reads 64 bit float.

    # + return - value of the float which is read or an error
    public extern function readFloat64() returns float|error;

    # Reads 1 byte and convert it's value to boolean.

    # + return - boolean value which is read or an error
    public extern function readBool() returns boolean|error;

    # Reads string value represented through the provided number of bytes.

    # + nBytes - specifies the number of bytes which represents the string
    # + encoding - specifies the char-set encoding of the string
    # + return - value of the string or an error
    public extern function readString(int nBytes, string encoding) returns string|error;

    # Reads a variable length integer.

    # + return - value of the integer which is read or an error
    public extern function readVarInt() returns int|error;

    # Closes the data channel.

    # + return - nill if the channel is closed successfully or an i/o error
    public extern function close() returns error?;
};
