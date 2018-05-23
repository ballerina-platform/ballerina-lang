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


public type ByteOrder BIG_ENDIAN|LITTLE_ENDIAN|DEFAULT;

public type BitLength 16_BIT|32_BIT|64_BIT;

public type DataChannel {
    public {
      ByteChannel byteChannel;
      ByteOrder byteOrder;
    }

    new(ByteChannel ch,ByteOrder byteOrder="default"){

    }

    documentation {
        Reads an integer based on the specified bit length.

        P{{len}}        Length of the integer in bits
        P{{signed}} True if the integer is signed else would be unsigned

        R{{}} Value of the integer which is read or an error
    }
    function readInt(BitLength len = 16_BIT, boolean signed = true) returns int | error;

    documentation {
        Writes the value of the integer.

        The bit length and the signed status will be identified from the input value

        P{{value}}   Integer which will be written
    }
    function writeInt(int value) returns error?;

    documentation {
        Reads float value based on the specified bit length.

        P{{len}}        Length of the float in bits
        P{{signed}} True if the integer is signed else would be unsigned

        R{{}} Value of the float which is read or an error.
    }
    function readFloat(BitLength len = 32_BIT) returns float | error;

    documentation {
        Writes the value of the float.

        The bit length and the signed status will be identified from the input value

        P{{value}}   float which will be written
    }
    function writeFloat(float value) returns error?;

    documentation {
        Reads 8bits (1 byte) and convert it's value to bool.

        R{{}} True if the value of the byte is 1 or an error
    }
    function readBool() returns boolean | error;

    documentation {
        Writes the provided boolean value.
    }
    function writeBool(bool value) returns error?;

    documentation {
        Skips the specified number of bytes.

        P{{nBytes}}   Number of bytes which should be skipped
    }
    function skip(int nBytes) returns error?;

    documentation {
        Reads the specified number of bytes.

        P{{nBytes}}   Number of bytes which should be read
    }
    function readBytes(int nBytes) returns blob | error;
};