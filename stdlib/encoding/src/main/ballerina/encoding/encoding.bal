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

# Error relevant to encoding operations.
#
# + message - Error message
public type EncodingError record {
    string message = "";
    !...;
};

# Returns the Base64 encoded `string` value of the given byte array.
#
# + input - Value to be encoded
# + return - Encoded output
public extern function encodeBase64(byte[] input) returns string;

# Decode Base64 encoded `string` into byte array.
#
# + input - Value to be decoded
# + return - Decoded output or error if input is not a valid Base64 value
public extern function decodeBase64(string input) returns byte[]|error;

# Returns the Hex encoded `string` value of the given byte array.
#
# + input - Value to be encoded
# + return - Encoded output
public extern function encodeHex(byte[] input) returns string;

# Decode Hex encoded `string` into byte array.
#
# + input - Value to be decoded
# + return - Decoded output or error if input is not a valid Hex value
public extern function decodeHex(string input) returns byte[]|error;

# Converts given byte array to a string.
#
# + content - Byte array content to be converted
# + encoding - Encoding to used in byte array conversion to string
# + return - String representation of the given byte array
public extern function byteArrayToString(byte[] content, string encoding = "utf-8") returns string;
