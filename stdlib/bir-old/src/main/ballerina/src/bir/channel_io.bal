// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/io;
import ballerina/lang.'string as strings;

// TODO: move to DataChannel native impl
public type ChannelReader object {
    io:ReadableByteChannel byteChannel;
    io:ReadableDataChannel dataChannel;

    public function init(io:ReadableByteChannel byteChannel) {
        self.byteChannel = byteChannel;
        self.dataChannel = new (byteChannel, "BE");
    }

    public function readBoolean() returns boolean {
        byte[] boolByte = checkpanic self.byteChannel.read(1);
        byte one = 1;
        return <@untainted> (boolByte[0] == one);
    }

    public function readInt8() returns int {
        byte[] byteValue = checkpanic self.byteChannel.read(1);
        return <@untainted int> byteValue[0];
    }

    public function readInt32() returns int {
        byte[] intBytes = checkpanic self.byteChannel.read(4);
        return <@untainted> bytesToInt(intBytes);
    }

    public function readInt64() returns int {
        return self.readInt32() << 32 | self.readInt32();
    }

    //TODO remove these and directly use data channel reader
    public function readFloat64() returns float {
        float | error ret = self.dataChannel.readFloat64();
        if (ret is float) {
            return ret;
        } else {
            panic ret;
        }
    }


    public function readString() returns string {
        var stringLen = <@untainted> self.readInt32();
        if (stringLen > 0){
            byte[] strBytes = checkpanic self.byteChannel.read(<@untainted> stringLen);
            return checkpanic <@untainted> strings:fromBytes(strBytes);
        } else {
            return "";
        }
    }

    public function readByteArray(int len) returns byte[] {
        byte[] arr = checkpanic self.byteChannel.read(len);
        if(len != arr.length()) {
            error err = error("Unable to read " + len.toString() + " bytes");
            panic err;
        }
        return <@untainted> arr;
    }

    public function readByte() returns byte {
        int value = self.readInt32();
        return <byte> value;
    }
};

function bytesToInt(byte[] b) returns int {
    int ff = 255;
    int octave1 = 8;
    int octave2 = 16;
    int octave3 = 24;
    int b0 = <int> b[0];
    int b1 = <int> b[1];
    int b2 = <int> b[2];
    int b3 = <int> b[3];
    return b0 <<octave3|(b1 & ff) <<octave2|(b2 & ff) <<octave1|(b3 & ff);
}
