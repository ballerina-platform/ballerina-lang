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

package ballerina.crypto;

@Description {value:"The algorithms which can be used in crypto functions."}
@Field {value:"MD5: MD5 algorithm"}
@Field {value:"SHA1: SHA1 algorithm"}
@Field {value:"SHA256: SHA256 algorithm"}
public type Algorithm "SHA1" | "SHA256" | "MD5";

@final public Algorithm SHA1 = "SHA1";
@final public Algorithm SHA256 = "SHA256";
@final public Algorithm MD5 = "MD5";

@Description {value:"Returns the hash of the given string using the specified algorithm."}
@Param {value:"baseString: The string to be hashed"}
@Param {value:"algorithm: The hashing algorithm to be used"}
@Return {value:"The hashed string"}
public native function getHash (string baseString, Algorithm algorithm) returns (string);

@Description {value:"Returns the HMAC value of the provided base string."}
@Param {value:"baseString: The string to be hashed"}
@Param {value:"keyString: The key string "}
@Param {value:"algorithm: The hashing algorithm to be used"}
@Return {value:"The hashed string"}
public native function getHmac (string baseString, string keyString, Algorithm algorithm) returns (string);

@Description {value:"Returns the CRC32 hash for the provided element. Currently supports strings and blobs."}
@Param {value:"content: The content to be hashed"}
@Return {value:"The generated hash"}
public native function getCRC32 (any content) returns (string);
