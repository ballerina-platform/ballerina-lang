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

//documentation {
//    Represents network byte order.
//
//    BIG_ENDIAN - specifies the bytes to be in the order of most significant byte first
//
//    LITTLE_ENDIAN - specifies the byte order to be the least significant byte first
//}
//public type ByteOrder "BE";
//@final public ByteOrder BIG_ENDIAN = "BE";

documentation {
    Represents a data channel for reading/writing data.
}
public type ReadableDataChannel object {

    public new(ReadableByteChannel byteChannel, ByteOrder bOrder = "BE") {
        init(byteChannel, bOrder);
    }

    documentation {
        Initializes data channel.

        P{{byteChannel}} channel which would represent the source to read/write data
        P{{bOrder}} network byte order
    }
    extern function init(ReadableByteChannel byteChannel, ByteOrder bOrder);

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
        Reads 1 byte and convert it's value to boolean.

        R{{}} boolean value which is read or an error
    }
    public extern function readBool() returns boolean|error;

    documentation {
        Reads string value represented through the provided number of bytes.

        P{{nBytes}} specifies the number of bytes which represents the string
        P{{encoding}} specifies the char-set encoding of the string
        R{{}} value of the string or an error
    }
    public extern function readString(int nBytes, string encoding) returns string|error;

    documentation {
        Reads a variable length integer.

        R{{}} value of the integer which is read or an error
    }
    public extern function readVarInt() returns int|error;

    documentation {
        Closes the data channel.

        R{{}} nill if the channel is closed successfully or an i/o error
    }
    public extern function close() returns error?;
};
