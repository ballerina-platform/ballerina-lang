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

@Description {value:"Locale type represents specific geographical, political, or cultural region."}
@Field {value:"language: The language field for Locale"}
@Field {value:"countryCode: The countryCode field for Locale"}
public type Locale {
    string language,
    string countryCode,
};

@Description {value:"Represent errors related to base64 encoder"}
@Field {value:"message: The error message"}
@Field {value:"cause: The cause of the error"}
public type Base64EncodeError {
    string message,
    error? cause,
};

@Description {value:"Represent errors related to base64 decoder"}
@Field {value:"message: The error message"}
@Field {value:"cause: The cause of the error"}
public type Base64DecodeError {
    string message,
    error? cause,
};

@Description {value:"Returns a random UUID string"}
@Return {value:"The random string"}
public native function uuid () returns (string);

@Description {value:"Encode a given input with Base64 encoding scheme."}
@Param {value:"contentToBeEncoded: Content that needs to be encoded can be of type string, blob or io:ByteChannel"}
@Param {value:"charset: Charset to be used. This is used only with the string input"}
@Return {value:"If the given input is of type string return value will be an encoded string"}
@Return {value:"If the given input is of type blob return value will be an encoded blob"}
@Return {value:"If the given input is of type io:ByteChannel return value will be an encoded io:ByteChannel"}
@Return {value:"Base64EncodeError will get return, in case of errors"}
native function base64Encode ((string | blob | io:ByteChannel) contentToBeEncoded, string charset="utf-8") returns (string  | blob  | io:ByteChannel | Base64EncodeError);

@Description {value:"Decode a given input with Base64 encoding scheme."}
@Param {value:"contentToBeDecoded: Content that needs to be decoded can be of type string, blob or io:ByteChannel"}
@Param {value:"charset: Charset to be used. This is used only with the string input"}
@Return {value:"If the given input is of type string return value will be a decoded string"}
@Return {value:"If the given input is of type blob return value will be a decoded blob"}
@Return {value:"If the given input is of type io:ByteChannel return value will be a decoded io:ByteChannel"}
@Return {value:"Base64DecodeError will get return, in case of errors"}
native function base64Decode ((string | blob | io:ByteChannel) contentToBeDecoded, string charset="utf-8") returns (string  | blob  | io:ByteChannel | Base64DecodeError);

@Description {value:"Encode a given blob with Base64 encoding scheme."}
@Param {value:"valueToBeEncoded: Content that needs to be encoded"}
@Return {value:"Return an encoded blob"}
@Return {value:"Base64EncodeError will get return, in case of errors"}
public function base64EncodeBlob(blob valueToBeEncoded) returns blob | Base64EncodeError {
    Base64EncodeError customErr = {message : "Error occurred while encoding blob"};
    match base64Encode(valueToBeEncoded) {
        string returnString => return customErr;
        blob returnBlob => return returnBlob;
        io:ByteChannel returnChannel => return customErr;
        Base64EncodeError encodeErr => return encodeErr;
    }
}

@Description {value:"Encode a given string with Base64 encoding scheme."}
@Param {value:"valueToBeEncoded: Content that needs to be encoded"}
@Param {value:"charset: Charset to be used"}
@Return {value:"Return an encoded string"}
@Return {value:"Base64EncodeError will get return, in case of errors"}
public function base64EncodeString(string valueToBeEncoded, string charset="utf-8") returns string | Base64EncodeError {
    Base64EncodeError customErr = {message : "Error occurred while encoding string"};
    match base64Encode(valueToBeEncoded) {
        string returnString => return returnString;
        blob returnBlob => return customErr;
        io:ByteChannel returnChannel => return customErr;
        Base64EncodeError encodeErr => return encodeErr;
    }
}

@Description {value:"Encode a given ByteChannel with Base64 encoding scheme."}
@Param {value:"valueToBeEncoded: Content that needs to be encoded"}
@Return {value:"Return an encoded ByteChannel"}
@Return {value:"Base64EncodeError will get return, in case of errors"}
public function base64EncodeByteChannel(io:ByteChannel valueToBeEncoded) returns io:ByteChannel | Base64EncodeError {
    Base64EncodeError customErr = {message : "Error occurred while encoding ByteChannel content"};
    match base64Encode(valueToBeEncoded) {
        string returnString => return customErr;
        blob returnBlob => return customErr;
        io:ByteChannel returnChannel => return returnChannel;
        Base64EncodeError encodeErr => return encodeErr;
    }
}

@Description {value:"Decode a given blob with Base64 encoding scheme."}
@Param {value:"valueToBeDecoded: Content that needs to be decoded"}
@Return {value:"Return a decoded blob"}
@Return {value:"Base64DecodeError will get return, in case of errors"}
public function base64DecodeBlob(blob valueToBeDecoded) returns blob | Base64DecodeError {
    Base64DecodeError customErr = {message : "Error occurred while decoding blob"};
    match base64Decode(valueToBeDecoded) {
        string returnString => return customErr;
        blob returnBlob => return returnBlob;
        io:ByteChannel returnChannel => return customErr;
        Base64DecodeError decodeErr => return decodeErr;
    }
}

@Description {value:"Decode a given string with Base64 encoding scheme."}
@Param {value:"valueToBeDecoded: Content that needs to be decoded"}
@Param {value:"charset: Charset to be used"}
@Return {value:"Return a decoded string"}
@Return {value:"Base64DecodeError will get return, in case of errors"}
public function base64DecodeString(string valueToBeDecoded, string charset="utf-8") returns string | Base64DecodeError {
    Base64DecodeError customErr = {message : "Error occurred while decoding string"};
    match base64Decode(valueToBeDecoded) {
        string returnString => return returnString;
        blob returnBlob => return customErr;
        io:ByteChannel returnChannel => return customErr;
        Base64DecodeError decodeErr => return decodeErr;
    }
}

@Description {value:"Decode a given ByteChannel with Base64 encoding scheme."}
@Param {value:"valueToBeDecoded: Content that needs to be decoded"}
@Return {value:"Return a decoded ByteChannel"}
@Return {value:"Base64DecodeError will get return, in case of errors"}
public function base64DecodeByteChannel(io:ByteChannel valueToBeDecoded) returns io:ByteChannel | Base64DecodeError {
    Base64DecodeError customErr = {message : "Error occurred while decoding ByteChannel content"};
    match base64Decode(valueToBeDecoded) {
        string returnString => return customErr;
        blob returnBlob => return customErr;
        io:ByteChannel returnChannel => return returnChannel;
        Base64DecodeError decodeErr => return decodeErr;
    }
}

@Description {value:"Encodes a base16 encoded string to base64 encoding."}
@Param {value:"s: string to be encoded"}
@Return {value:"the encoded string."}
public native function base16ToBase64Encode (string baseString) returns (string);

@Description {value:"Encodes a base64 encoded string to base16 encoding."}
@Param {value:"s: string to be encoded"}
@Return {value:"the encoded string."}
public native function base64ToBase16Encode (string baseString) returns (string);
