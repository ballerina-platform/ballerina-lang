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
    The hashing algorithms supported by this package.
}
public type Algorithm "SHA1"|"SHA256"|"MD5";

documentation { The `SHA1` hashing algorithm" }
@final public Algorithm SHA1 = "SHA1";
documentation { The `SHA256` hashing algorithm }
@final public Algorithm SHA256 = "SHA256";
documentation { The `MD5` hashing algorithm }
@final public Algorithm MD5 = "MD5";

documentation {
    Returns the hash of the given string using the specified algorithm.

    P{{baseString}} The string to be hashed
    P{{algorithm}} The hashing algorithm to be used
    R{{}} The hashed string
}
public extern function hash(string baseString, Algorithm algorithm) returns (string);

documentation {
    Returns the HMAC value of the provided base string.

    P{{baseString}} The string to be hashed
    P{{keyString}} The key string
    P{{keyEncoding}} The encoding of the key
    P{{algorithm}} The hashing algorithm to be used
    R{{}} The hashed string
}
public extern function hmac(string baseString, string keyString, string keyEncoding = "UTF-8", Algorithm algorithm) returns (string);

documentation {
    Returns the CRC32 hash for the provided element. This accepts `string`, `byte[]`, `json` and `xml` content.

    P{{content}} The content to be hashed
    R{{}} The generated hash
}
public extern function crc32(any content) returns (string);
