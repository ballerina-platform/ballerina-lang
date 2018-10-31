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

# ReadableProtoChannel represents capabilities to decode bytes based on protobuf specification.
public type ReadableProtoChannel object {
    private ReadableDataChannel? dc;

    public new(ReadableByteChannel byteChannel) {
        dc = new ReadableDataChannel(byteChannel, bOrder = BIG_ENDIAN);
    }

    public function readBool() returns boolean|error? {
        return dc.readBool();
    }

    public function readDouble() returns float|error? {
        return dc.readFloat64();
    }

    public function readFixed32() returns int|error? {
        return dc.readInt32();
    }

    public function readFixed64() returns int|error? {
        return dc.readInt64();
    }

    public function readFloat() returns float|error? {
        return dc.readFloat32();
    }

    public function readInt() returns int|error? {
        return dc.readVarInt();
    }

    public function readString() returns string|error? {
        match dc.readVarInt() {
            int length => {
                return dc.readString(length, PROTOBUF_STRING_ENCODING);
            }
            error e => {
                return e;
            }
            () => {
                error err =  error( "{ballerina/io}IOError", { message : "error occurred while reading string"} );
                return err;
            }
        }
    }

    public function readTag() returns int|error {
        match dc.readVarInt() {
            int value => {
                return value >> 3;
            }
            error e => {
                return e;
            }
            () => {
                error err = error( "{ballerina/io}IOError", { message : "error occurred while reading tag"} );
                return err;
            }
        }
    }

    public function close() returns error? {
        return dc.close();
    }
};
