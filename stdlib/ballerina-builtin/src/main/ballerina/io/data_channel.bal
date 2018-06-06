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

    NATIVE - specifies the byte order to be in the native order defined by the platform of execution
}
public type ByteOrder "BI"|"LI"|"NATIVE";
@final public ByteOrder BIG_ENDIAN = "BI";
@final public ByteOrder LITTLE_ENDIAN = "LI";

documentation {
    Represents a data channel for reading/writing data.
}
public type DataChannel object {

    public new(ByteChannel byteChannel, ByteOrder bOrder = "BI") {
        init(byteChannel, bOrder);
    }

    documentation {
        Initializes data channel.

        P{{byteChannel}} channel which would represent the source to read/write data
        P{{bOrder}} network byte order
    }
    native function init(ByteChannel byteChannel, ByteOrder bOrder);

    documentation {
        Reads a 16 bit integer.

        R{{}} Value of the integer which is read or an error
    }
    public native function readInt16() returns int|error;

    documentation {
        Reads a 32 bit integer.

        R{{}} Value of the integer which is read or an error
    }
    public native function readInt32() returns int|error;

    documentation {
        Reads a 64 bit integer.

        R{{}} Value of the integer which is read or an error
    }
    public native function readInt64() returns int|error;

    documentation {
        Writes 16 bit integer.

        P{{value}}   Integer which will be written
        R{{}} nill if the content is written successfully or an error
    }
    public native function writeInt16(int value) returns error?;

    documentation {
        Writes 32 bit integer.

        P{{value}}   Integer which will be written
        R{{}} nill if the content is written successfully or an error
    }
    public native function writeInt32(int value) returns error?;

    documentation {
        Writes 64 bit integer.

        P{{value}}   Integer which will be written
        R{{}} nill if the content is written successfully or an error
    }
    public native function writeInt64(int value) returns error?;

    documentation {
        Reads 32 bit float.

        R{{}} Value of the float which is read or an error
    }
    public native function readFloat32() returns float|error;

    documentation {
        Reads 64 bit float.

        R{{}} Value of the float which is read or an error
    }
    public native function readFloat64() returns float|error;

    documentation {
        Writes 32 bit float.

        P{{value}}   Float which will be written
        R{{}} Value of the float which is read or an error
    }
    public native function writeFloat32(float value) returns error?;

    documentation {
        Writes 64 bit float.

        P{{value}}   Float which will be written
        R{{}} Value of the float which is read or an error
    }
    public native function writeFloat64(float value) returns error?;

    documentation {
        Reads 1 byte and convert it's value to boolean.

        R{{}} boolean value which is read or an error
    }
    public native function readBool() returns boolean|error;

    documentation {
        Writes boolean.

        P{{value}}   boolean which will be written
        R{{}} nill if the content is written successfully or an error
    }
    public native function writeBool(boolean value) returns error?;
};
