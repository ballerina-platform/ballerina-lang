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

documentation {
    The algorithms which can be used in crypto functions.
}
public type Algorithm "SHA1"|"SHA256"|"MD5";

@final public Algorithm SHA1 = "SHA1";
@final public Algorithm SHA256 = "SHA256";
@final public Algorithm MD5 = "MD5";

documentation {
    Returns the hash of the given string using the specified algorithm.

    P{{baseString}} The string to be hashed
    P{{algorithm}} The hashing algorithm to be used
    R{{}} The hashed string
}
public native function hash(string baseString, Algorithm algorithm) returns (string);

documentation {
    Returns the HMAC value of the provided base string.

    P{{baseString}} The string to be hashed
    P{{keyString}} The key string
    P{{algorithm}} The hashing algorithm to be used
    R{{}} The hashed string
}
public native function hmac(string baseString, string keyString, Algorithm algorithm) returns (string);

documentation {
    Returns the CRC32 hash for the provided element. Currently supports strings and blobs.

    P{{content}} The content to be hashed
    R{{}} The generated hash
}
public native function crc32(any content) returns (string);
