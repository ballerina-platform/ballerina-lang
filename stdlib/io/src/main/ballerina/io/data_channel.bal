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

documentation {
    Represents network byte order.

    BIG_ENDIAN - specifies the bytes to be in the order of most significant byte first

    LITTLE_ENDIAN - specifies the byte order to be the least significant byte first
}
public type ByteOrder "BE";
@final public ByteOrder BIG_ENDIAN = "BE";

documentation {
    Represents a data channel for reading/writing data.
}
public type DataChannel object {

    public new(ByteChannel byteChannel, ByteOrder bOrder = "BE") {
        init(byteChannel, bOrder);
    }

    documentation {
        Initializes data channel.

        P{{byteChannel}} channel which would represent the source to read/write data
        P{{bOrder}} network byte order
    }
    extern function init(ByteChannel byteChannel, ByteOrder bOrder);

    documentation {
        Reads a 16 bit integer.

        R{{}} value of the integer which is read or an error
    }
    public extern function readInt16() returns int|error;

    documentation {
        Reads a 32 bit integer.

        R{{}} value of the integer which is read or an error
    }
    public extern function readInt32() returns int|error;

    documentation {
        Reads a 64 bit integer.

        R{{}} value of the integer which is read or an error
    }
    public extern function readInt64() returns int|error;

    documentation {
        Writes 16 bit integer.

        P{{value}}   integer which will be written
        R{{}} nill if the content is written successfully or an error
    }
    public extern function writeInt16(int value) returns error?;

    documentation {
        Writes 32 bit integer.

        P{{value}}   integer which will be written
        R{{}} nill if the content is written successfully or an error
    }
    public extern function writeInt32(int value) returns error?;

    documentation {
        Writes 64 bit integer.

        P{{value}}   integer which will be written
        R{{}} nill if the content is written successfully or an error
    }
    public extern function writeInt64(int value) returns error?;

    documentation {
        Reads 32 bit float.

        R{{}} value of the float which is read or an error
    }
    public extern function readFloat32() returns float|error;

    documentation {
        Reads 64 bit float.

        R{{}} value of the float which is read or an error
    }
    public extern function readFloat64() returns float|error;

    documentation {
        Writes 32 bit float.

        P{{value}}   float which will be written
        R{{}} nill if the float is written successfully or an error
    }
    public extern function writeFloat32(float value) returns error?;

    documentation {
        Writes 64 bit float.

        P{{value}}   float which will be written
        R{{}} nill if the float is written successfully or an error
    }
    public extern function writeFloat64(float value) returns error?;

    documentation {
        Reads 1 byte and convert it's value to boolean.

        R{{}} boolean value which is read or an error
    }
    public extern function readBool() returns boolean|error;

    documentation {
        Writes boolean.

        P{{value}}   boolean which will be written
        R{{}} nill if the content is written successfully or an error
    }
    public extern function writeBool(boolean value) returns error?;

    documentation {
        Reads string value represented through the provided number of bytes.

        P{{nBytes}} specifies the number of bytes which represents the string
        P{{encoding}} specifies the char-set encoding of the string
        R{{}} value of the string or an error
    }
    public extern function readString(int nBytes, string encoding) returns string|error;

    documentation {
        Writes a given string value to the respective channel.

        P{{value}} the value which should be written
        P{{encoding}} the encoding which will represent the value string
        R{{}} nill if the content is written successfully or an error
    }
    public extern function writeString(string value, string encoding) returns error?;

    documentation {
        Reads a variable length integer.

        R{{}} value of the integer which is read or an error
    }
    public extern function readVarInt() returns int|error;

    documentation {
        Writes a given integer identifying the variable length.

        P{{value}} the value which should be written
        R{{}} nill if the content is written successfully or an error
    }
    public extern function writeVarInt(int value) returns error?;

    documentation {
        Closes the data channel.

        R{{}} nill if the channel is closed successfully or an i/o error
    }
    public extern function close() returns error?;
};
