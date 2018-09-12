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


# ReadableByteChannel represents an input resource (i.e file, socket). which could be used to source bytes.
public type ReadableByteChannel object {

    # Sink bytes from a given input/output resource.
    #
    # This operation will be asynchronous, write might return without writing all the content.
    #
    # + content - Block of bytes which should be written
    # + return - Offset which should be kept when writing bytes.
    #            Number of bytes written or an error.
    public extern function read(@sensitive int nBytes) returns @tainted (byte[], int)|error;

    # Encodes a given ReadableByteChannel with Base64 encoding scheme.
    #
    # + return - Return an encoded ReadableByteChannel or an error
    public extern function base64Encode() returns ReadableByteChannel|error;

    # Decodes a given ReadableByteChannel with Base64 encoding scheme.
    #
    # + return - Return a decoded ReadableByteChannel or an error
    public extern function base64Decode() returns ReadableByteChannel|error;

    # Closes a given ReadableByteChannel.
    #
    # + return - Will return () if there's no error
    public extern function close() returns error?;
};
