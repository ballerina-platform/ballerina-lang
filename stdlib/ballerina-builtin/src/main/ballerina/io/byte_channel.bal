// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


@Description {value:"Ballerina ByteChannel represents a channel which will allow I/O operations to be done"}
public type ByteChannel object {

    @Description {value:"Function to read bytes"}
    @Param {value:"nBytes: Number of bytes which should be read"}
    @Return {value:"The bytes which were read"}
    @Return {value:"Number of bytes read"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public native function read(@sensitive int nBytes) returns @tainted (blob, int)|error;

    @Description {value:"Function to write bytes"}
    @Param {value:"content: Bytes which should be written"}
    @Param {value:"offset: If the bytes need to be written with an offset, the value of that offset"}
    @Return {value:"Number of bytes written"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public native function write(blob content, int offset) returns int|error;

    @Description {value:"Function to close a byte channel"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public native function close() returns error?;

    @Description {value:"Encode a given ByteChannel with Base64 encoding scheme."}
    @Param {value:"valueToBeEncoded: Content that needs to be encoded"}
    @Return {value:"Return an encoded ByteChannel"}
    @Return {value:"error will get return, in case of errors"}
    public native function base64Encode() returns ByteChannel|error;

    @Description {value:"Decode a given ByteChannel with Base64 encoding scheme."}
    @Param {value:"valueToBeDecoded: Content that needs to be decoded"}
    @Return {value:"Return a decoded ByteChannel"}
    @Return {value:"error will get return, in case of errors"}
    public native function base64Decode() returns ByteChannel|error;
};
