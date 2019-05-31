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

public type BirChannelReader object {
    ChannelReader reader;
    ConstPool cp;

    public function __init(ChannelReader reader, ConstPool cp) {
        self.reader = reader;
        self.cp = cp;
    }

    public function readBType() returns BType {
        string sginatureAlias = self.readStringCpRef();
        if (sginatureAlias == "I"){
            return "int";
        } else if (sginatureAlias == "B"){
            return "boolean";
        } 
        error err = error("type signature " + sginatureAlias + " not supported.");
        panic err;
    }

    public function readStringCpRef() returns string {
        return self.cp.strings[self.reader.readInt32()];
    }

    public function readIntCpRef() returns int {
        return self.cp.ints[self.reader.readInt32()];
    }

    public function readFloatCpRef() returns float {
        return self.cp.floats[self.reader.readInt32()];
    }

    public function readByteCpRef() returns byte {
        return self.cp.bytes[self.reader.readInt32()];
    }

    public function readModuleIDCpRef() returns ModuleID {
        return self.cp.packages[self.reader.readInt32()];
    }


    // following methods "proxied" since ballerina doesn't support obj inheritance yet

    public function readBoolean() returns boolean {
        return self.reader.readBoolean();
    }

    public function readInt8() returns int {
        return self.reader.readInt8();
    }

    public function readInt32() returns int {
        return self.reader.readInt32();
    }

    public function readInt64() returns int {
        return self.reader.readInt64();
    }


    public function readString() returns string {
        return self.reader.readString();
    }

    public function readByteArray(int len) returns byte[] {
        return self.reader.readByteArray(len);
    }
};
