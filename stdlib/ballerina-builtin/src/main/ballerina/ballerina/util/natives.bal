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

@Description {value:"Locale struct represents specific geographical, political, or cultural region."}
@Field {value:"language: The language field for Locale"}
@Field {value:"countryCode: The countryCode field for Locale"}
public struct Locale {
    string language;
    string countryCode;
}

@Description {value:"Returns a random UUID string"}
@Return {value:"The random string"}
public native function uuid () (string);

@Description {value:"Encodes a base10 encoded string to base64."}
@Param {value:"s: string to be encoded"}
@Return {value:"the encoded string."}
public native function base64Encode (string s) (string);

@Description {value:"Decodes a base64 encoded string to base10."}
@Param {value:"s: string to be decoded"}
@Return {value:"the decoded string."}
public native function base64Decode (string s) (string);

@Description {value:"Encodes a base16 encoded string to base64 encoding."}
@Param {value:"s: string to be encoded"}
@Return {value:"the encoded string."}
public native function base16ToBase64Encode (string baseString) (string);

@Description {value:"Encodes a base64 encoded string to base16 encoding."}
@Param {value:"s: string to be encoded"}
@Return {value:"the encoded string."}
public native function base64ToBase16Encode (string baseString) (string);
