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

# Returns the MD5 hash of the given data.
#
# + input - Value to be hashed
# + return - Hashed output
public extern function hashMd5(byte[] input) returns byte[];

# Returns the SHA-1 hash of the given data.
#
# + input - Value to be hashed
# + return - Hashed output
public extern function hashSha1(byte[] input) returns byte[];

# Returns the SHA-256 hash of the given data.
#
# + input - Value to be hashed
# + return - Hashed output
public extern function hashSha256(byte[] input) returns byte[];

# Returns the SHA-384 hash of the given data.
#
# + input - Value to be hashed
# + return - Hashed output
public extern function hashSha384(byte[] input) returns byte[];

# Returns the SHA-512 hash of the given data.
#
# + input - Value to be hashed
# + return - Hashed output
public extern function hashSha512(byte[] input) returns byte[];

# Returns the HMAC using MD-5 hash function of the given data.
#
# + input - Value to be hashed
# + return - HMAC output
public extern function hmacMd5(byte[] input, byte[] key) returns byte[];

# Returns the HMAC using SHA-1 hash function of the given data.
#
# + input - Value to be hashed
# + return - HMAC output
public extern function hmacSha1(byte[] input, byte[] key) returns byte[];

# Returns the HMAC using SHA-256 hash function of the given data.
#
# + input - Value to be hashed
# + return - HMAC output
public extern function hmacSha256(byte[] input, byte[] key) returns byte[];

# Returns the HMAC using SHA-384 hash function of the given data.
#
# + input - Value to be hashed
# + return - HMAC output
public extern function hmacSha384(byte[] input, byte[] key) returns byte[];

# Returns the HMAC using SHA-512 hash function of the given data.
#
# + input - Value to be hashed
# + return - HMAC output
public extern function hmacSha512(byte[] input, byte[] key) returns byte[];

# Returns Hex encoded CRC32B value for the provided element. This accepts `string`, `byte[]`, `json` and `xml` content.
#
# + content - The content to be hashed
# + return - The generated hash
public extern function crc32b(any content) returns (string);

//public extern function signDsaSha1(byte[] input, PrivateKey privateKey) returns byte[];
public extern function signRsaMd5(byte[] input, PrivateKey privateKey) returns byte[];
public extern function signRsaSha1(byte[] input, PrivateKey privateKey) returns byte[];
public extern function signRsaSha256(byte[] input, PrivateKey privateKey) returns byte[];
public extern function signRsaSha384(byte[] input, PrivateKey privateKey) returns byte[];
public extern function signRsaSha512(byte[] input, PrivateKey privateKey) returns byte[];

public extern function decodePrivateKey(byte[]? keyContent = (),
                                           string? keyFile = (),
                                           string? keyStore = (),
                                           string? keyStorePassword = (),
                                           string? keyAlias = (),
                                           string? keyPassword = ()) returns PrivateKey;

//public extern function decodePublicKey(byte[]? keyContent = (),
//                                          string? keyFile = (),
//                                          string? keyStore = (),
//                                          string? keyStorePassword = (),
//                                          string? keyAlias = ()) returns PublicKey;