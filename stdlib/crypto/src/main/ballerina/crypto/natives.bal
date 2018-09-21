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

# The hashing algorithms supported by this package.
public type Algorithm "SHA1"|"SHA256"|"MD5";

# The `SHA1` hashing algorithm
@final public Algorithm SHA1 = "SHA1";
# The `SHA256` hashing algorithm
@final public Algorithm SHA256 = "SHA256";
# The `MD5` hashing algorithm
@final public Algorithm MD5 = "MD5";

# The encoding types supported for the HMAC key, by this package.
public type Encoding "UTF-8"|"BASE64"|"HEX";

# The `UTF-8` encoding
@final public Encoding UTF8 = "UTF-8";
# The `BASE64` encoding
@final public Encoding BASE64 = "BASE64";
# The `HEX` encoding
@final public Encoding HEX = "HEX";


# Returns the hash of the given string using the specified algorithm.
#
# + baseString - The string to be hashed
# + algorithm - The hashing algorithm to be used
# + return - The hashed string
public extern function hash(string baseString, Algorithm algorithm) returns (string);

# Returns the HMAC value of the provided base string.
#
# + baseString - The string to be hashed
# + keyString - The key string
# + keyEncoding - The encoding of the key
# + algorithm - The hashing algorithm to be used
# + return - The hashed string
public extern function hmac(string baseString, string keyString, Encoding? keyEncoding = (), Algorithm algorithm) returns (string);

# Returns the CRC32 hash for the provided element. This accepts `string`, `byte[]`, `json` and `xml` content.
#
# + content - The content to be hashed
# + return - The generated hash
public extern function crc32(any content) returns (string);
