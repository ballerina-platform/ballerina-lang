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

import ballerina/java;

# ReadableByteChannel represents an input resource (i.e file). which could be used to source bytes.
public class ReadableByteChannel {

    # Adding default init function to prevent object getting initialized from the user code.
    function init() {}

# Source bytes from a given input/output resource. The number of bytes returned will be < 0 if the file reached its end.
# This operation will be asynchronous in which the total number of required bytes might not be returned at a given
# time. An `io:EofError` will return once the channel reaches the end.
# ```ballerina
# byte[]|io:Error result = readableByteChannel.read(1000);
# ```
#
# + nBytes - A positive integer. Represents the number of bytes, which should be read
# + return - Content (the number of bytes) read, an `EofError` once the channel reaches the end or else an `io:Error`
    public function read(@untainted int nBytes) returns @tainted byte[]|Error {
        return byteReadExtern(self, nBytes);
    }

# Encodes a given `ReadableByteChannel` using the Base64 encoding scheme.
# ```ballerina
# ReadableByteChannel|Error encodedChannel = readableByteChannel.base64Encode();
# ```
#
# + return - An encoded `ReadableByteChannel` or else an `io:Error`
    public function base64Encode() returns ReadableByteChannel|Error {
        return base64EncodeExtern(self);
    }

# Decodes a given `ReadableByteChannel` using the Base64 encoding scheme.
# ```ballerina
# ReadableByteChannel|Error encodedChannel = readableByteChannel.base64Decode();
# ```
#
# + return - A decoded `ReadableByteChannel` or else an `io:Error`
    public function base64Decode() returns ReadableByteChannel|Error {
        return base64DecodeExtern(self);
    }

# Closes a given `ReadableByteChannel`.
# ```ballerina
# io:Error? err = readableByteChannel.close();
# ```
#
# + return - Will return `()` if there is no error
    public function close() returns Error? {
        return closeReadableByteChannelExtern(self);
    }
}

function byteReadExtern(ReadableByteChannel byteChannel, @untainted int nBytes) returns @tainted byte[]|Error = @java:Method {
    name: "read",
    'class: "org.ballerinalang.stdlib.io.nativeimpl.ByteChannelUtils"
} external;

function base64EncodeExtern(ReadableByteChannel byteChannel) returns ReadableByteChannel|Error = @java:Method {
    name: "base64Encode",
    'class: "org.ballerinalang.stdlib.io.nativeimpl.ByteChannelUtils"
} external;

function base64DecodeExtern(ReadableByteChannel byteChannel) returns ReadableByteChannel|Error = @java:Method {
    name: "base64Decode",
    'class: "org.ballerinalang.stdlib.io.nativeimpl.ByteChannelUtils"
} external;

function closeReadableByteChannelExtern(ReadableByteChannel byteChannel) returns Error? = @java:Method {
    name: "closeByteChannel",
    'class: "org.ballerinalang.stdlib.io.nativeimpl.ByteChannelUtils"
} external;
