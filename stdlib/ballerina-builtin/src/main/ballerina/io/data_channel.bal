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

documentation{
    Represents network byte order.

    BIG_ENDIAN - specifies the bytes to be in the order of most significant byte first

    LITTLE_ENDIAN - specifies the byte order to be the least significant byte first

    NATIVE - specifies the byte order to be in the native order defined by the platform of execusion
}
public type ByteOrder "BI"|"LI"|"NATIVE";
@final public ByteOrder BIG_ENDIAN = "BI";
@final public ByteOrder LITTLE_ENDIAN = "LI";
@final public ByteOrder NATIVE = "NATIVE";

documentation{
    Represents supported bit lengths.

    BIT_16 - for 16 bit(2 byte) representation

    BIT_32 - for 32 bit (4 byte) representation

    BIT_64 - for 64 bit (8 byte) representation
}
public type BitLength "16b"|"32b"|"64b";
@final public BitLength BIT_16 = "16b";
@final public BitLength BIT_32 = "32b";
@final public BitLength BIT_64 = "64b";

documentation {
    Represents a data channel for reading/writing data.
}
public type DataChannel object {

    public new(ByteChannel byteChannel, ByteOrder bOrder = "NATIVE") {
        init(byteChannel, bOrder);
    }

    documentation{
        Initializes data channel.

        P{{byteChannel}} channel which would represent the source to read/write data
        P{{bOrder}} network byte order
    }
    native function init(ByteChannel byteChannel, ByteOrder bOrder);

    documentation {
        Reads an integer for the specified bit length.

        P{{len}}        Length of the integer in bits
        P{{signed}}     True if the integer is signed else would be unsigned
        R{{}} Value of the integer which is read or an error
    }
    public native function readInt(BitLength len = "64b", boolean signed = true) returns int|error;

    documentation {
        Writes integer for the specified bit length.

        P{{value}}   Integer which will be written
        P{{len}}     length which should be allocated for int
        R{{}} nill if the content is written successfully or an error
    }
    public native function writeInt(int value, BitLength len = "64b") returns error?;

    documentation {
        Reads float value based on the specified bit length.

        P{{len}}        Length of the float in bits
        R{{}} Value of the float which is read or an error
    }
    public native function readFloat(BitLength len = "64b") returns float|error;

    documentation {
        Writes float for the specified bit length.

        P{{value}}   float which will be written
        P{{len}}     length which should be allocated to float
        R{{}} Value of the float which is read or an error
    }
    public native function writeFloat(float value, BitLength len = "64b") returns error?;

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
