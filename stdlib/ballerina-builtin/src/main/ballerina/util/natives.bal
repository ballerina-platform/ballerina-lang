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

package ballerina.util;

import ballerina/io;

// Todo - Remove in a future version.
@Description {value:"Encode a given input with Base64 encoding scheme."}
@Param {value:"contentToBeEncoded: Content that needs to be encoded can be of type string, blob or io:ByteChannel"}
@Param {value:"charset: Charset to be used. This is used only with the string input"}
@Return {value:"If the given input is of type string return value will be an encoded string"}
@Return {value:"If the given input is of type blob return value will be an encoded blob"}
@Return {value:"If the given input is of type io:ByteChannel return value will be an encoded io:ByteChannel"}
@Return {value:"Base64EncodeError will get return, in case of errors"}
native function base64Encode((string|blob|io:ByteChannel) contentToBeEncoded, string charset = "utf-8") returns (string|
        blob|io:ByteChannel|error);

// Todo - Remove in a future version.
@Description {value:"Decode a given input with Base64 encoding scheme."}
@Param {value:"contentToBeDecoded: Content that needs to be decoded can be of type string, blob or io:ByteChannel"}
@Param {value:"charset: Charset to be used. This is used only with the string input"}
@Return {value:"If the given input is of type string return value will be a decoded string"}
@Return {value:"If the given input is of type blob return value will be a decoded blob"}
@Return {value:"If the given input is of type io:ByteChannel return value will be a decoded io:ByteChannel"}
@Return {value:"Base64DecodeError will get return, in case of errors"}
native function base64Decode((string|blob|io:ByteChannel) contentToBeDecoded, string charset = "utf-8") returns (string|
        blob|io:ByteChannel|error);

// Todo - Remove in a future version.
@Description {value:"Encode a given blob with Base64 encoding scheme."}
@Param {value:"valueToBeEncoded: Content that needs to be encoded"}
@Return {value:"Return an encoded blob"}
@Return {value:"Base64EncodeError will get return, in case of errors"}
public function base64EncodeBlob(blob valueToBeEncoded) returns blob|error {
    return valueToBeEncoded.base64Encode();
}

// Todo - Remove in a future version.
@Description {value:"Encode a given string with Base64 encoding scheme."}
@Param {value:"valueToBeEncoded: Content that needs to be encoded"}
@Param {value:"charset: Charset to be used"}
@Return {value:"Return an encoded string"}
@Return {value:"Base64EncodeError will get return, in case of errors"}
public function base64EncodeString(string valueToBeEncoded, string charset = "utf-8") returns string|error {
    return valueToBeEncoded.base64Encode();
}

// Todo - Remove in a future version.
@Description {value:"Encode a given ByteChannel with Base64 encoding scheme."}
@Param {value:"valueToBeEncoded: Content that needs to be encoded"}
@Return {value:"Return an encoded ByteChannel"}
@Return {value:"Base64EncodeError will get return, in case of errors"}
public function base64EncodeByteChannel(io:ByteChannel valueToBeEncoded) returns io:ByteChannel|error {
    return valueToBeEncoded.base64Encode();
}

// Todo - Remove in a future version.
@Description {value:"Decode a given blob with Base64 encoding scheme."}
@Param {value:"valueToBeDecoded: Content that needs to be decoded"}
@Return {value:"Return a decoded blob"}
@Return {value:"Base64DecodeError will get return, in case of errors"}
public function base64DecodeBlob(blob valueToBeDecoded) returns blob|error {
    return valueToBeDecoded.base64Decode();
}

// Todo - Remove in a future version.
@Description {value:"Decode a given string with Base64 encoding scheme."}
@Param {value:"valueToBeDecoded: Content that needs to be decoded"}
@Param {value:"charset: Charset to be used"}
@Return {value:"Return a decoded string"}
@Return {value:"Base64DecodeError will get return, in case of errors"}
public function base64DecodeString(string valueToBeDecoded, string charset = "utf-8") returns string|error {
    return valueToBeDecoded.base64Decode(charset);
}

// Todo - Remove in a future version.
@Description {value:"Decode a given ByteChannel with Base64 encoding scheme."}
@Param {value:"valueToBeDecoded: Content that needs to be decoded"}
@Return {value:"Return a decoded ByteChannel"}
@Return {value:"Base64DecodeError will get return, in case of errors"}
public function base64DecodeByteChannel(io:ByteChannel valueToBeDecoded) returns io:ByteChannel|error {
    return valueToBeDecoded.base64Decode();
}

// Todo - Remove in a future version.
@Description {value:"Encodes a base16 encoded string to base64 encoding."}
@Param {value:"s: string to be encoded"}
@Return {value:"the encoded string."}
public function base16ToBase64Encode(string baseString) returns string {
    return baseString.base16ToBase64Encode();
}

// Todo - Remove in a future version.
@Description {value:"Encodes a base64 encoded string to base16 encoding."}
@Param {value:"s: string to be encoded"}
@Return {value:"the encoded string."}
public function base64ToBase16Encode(string baseString) returns string {
    return baseString.base64ToBase16Encode();
}

@Description {value:"Parse JSON string to generate JSON object."}
@Param {value:"s: JSON string"}
@Return {value:"JSON object."}
public native function parseJson(string s) returns (json|error);
