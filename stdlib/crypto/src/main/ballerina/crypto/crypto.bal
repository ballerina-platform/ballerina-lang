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

import ballerina/time;

# The key algorithms supported by crypto module.
public type KeyAlgorithm RSA;

# The `RSA` algorithm
public const RSA = "RSA";

# Padding algorithms supported with AES encryption and decryption
public type AesPadding NONE|PKCS5;

# Padding algorithms supported with RSA encryption and decryption
public type RsaPadding PKCS1|OAEPwithMD5andMGF1|OAEPWithSHA1AndMGF1|OAEPWithSHA256AndMGF1|OAEPwithSHA384andMGF1|
                       OAEPwithSHA512andMGF1;

# No padding
public const NONE = "NONE";

# The `PKCS1` padding mode
public const PKCS1 = "PKCS1";

# The `PKCS5` padding mode
public const PKCS5 = "PKCS5";

# The `OAEPwithMD5andMGF1` padding mode
public const OAEPwithMD5andMGF1 = "OAEPwithMD5andMGF1";

# The `OAEPWithSHA1AndMGF1` padding mode
public const OAEPWithSHA1AndMGF1 = "OAEPWithSHA1AndMGF1";

# The `OAEPWithSHA256AndMGF1` padding mode
public const OAEPWithSHA256AndMGF1 = "OAEPWithSHA256AndMGF1";

# The `OAEPwithSHA384andMGF1` padding mode
public const OAEPwithSHA384andMGF1 = "OAEPwithSHA384andMGF1";

# The `OAEPwithSHA512andMGF1` padding mode
public const OAEPwithSHA512andMGF1 = "OAEPwithSHA512andMGF1";

# Record for providing key store related configurations.
#
# + path - Path to the key store file
# + password - Key store password
public type KeyStore record {|
    string path = "";
    string password = "";
|};

# Record for providing trust store related configurations.
#
# + path - Path to the key store file
# + password - Key store password
public type TrustStore record {|
    string path = "";
    string password = "";
|};

# Private key used in cryptographic operations.
#
# + algorithm - Key algorithm
public type PrivateKey record {|
    KeyAlgorithm algorithm;
|};

# Public key used in cryptographic operations.
#
# + algorithm - Key algorithm
# + certificate - Public key certificate
public type PublicKey record {|
    KeyAlgorithm algorithm;
    Certificate? certificate;
|};

# X509 public key certificate information.
#
# + version0 - Version number
# + serial - Serial number
# + issuer - Issuer name
# + subject - Subject name
# + notBefore - Not before validity period of certificate
# + notAfter - Not after validity period of certificate
# + signature - Raw signature bits
# + signingAlgorithm - Signature algorithm
public type Certificate record {|
    int version0;
    int serial;
    string issuer;
    string subject;
    time:Time notBefore;
    time:Time notAfter;
    byte[] signature;
    string signingAlgorithm;
|};

# Error relevant to crypto operations.
#
# + message - Error message
public type CryptoError record {|
    string message = "";
|};

# Returns the MD5 hash of the given data.
#
# + input - Value to be hashed
# + return - Hashed output
public function hashMd5(byte[] input) returns byte[] = external;

# Returns the SHA-1 hash of the given data.
#
# + input - Value to be hashed
# + return - Hashed output
public function hashSha1(byte[] input) returns byte[] = external;

# Returns the SHA-256 hash of the given data.
#
# + input - Value to be hashed
# + return - Hashed output
public function hashSha256(byte[] input) returns byte[] = external;

# Returns the SHA-384 hash of the given data.
#
# + input - Value to be hashed
# + return - Hashed output
public function hashSha384(byte[] input) returns byte[] = external;

# Returns the SHA-512 hash of the given data.
#
# + input - Value to be hashed
# + return - Hashed output
public function hashSha512(byte[] input) returns byte[] = external;

# Returns the HMAC using MD-5 hash function of the given data.
#
# + input - Value to be hashed
# + key - Key used for HMAC generation
# + return - HMAC output
public function hmacMd5(byte[] input, byte[] key) returns byte[] = external;

# Returns the HMAC using SHA-1 hash function of the given data.
#
# + input - Value to be hashed
# + key - Key used for HMAC generation
# + return - HMAC output
public function hmacSha1(byte[] input, byte[] key) returns byte[] = external;

# Returns the HMAC using SHA-256 hash function of the given data.
#
# + input - Value to be hashed
# + key - Key used for HMAC generation
# + return - HMAC output
public function hmacSha256(byte[] input, byte[] key) returns byte[] = external;

# Returns the HMAC using SHA-384 hash function of the given data.
#
# + input - Value to be hashed
# + key - Key used for HMAC generation
# + return - HMAC output
public function hmacSha384(byte[] input, byte[] key) returns byte[] = external;

# Returns the HMAC using SHA-512 hash function of the given data.
#
# + input - Value to be hashed
# + key - Key used for HMAC generation
# + return - HMAC output
public function hmacSha512(byte[] input, byte[] key) returns byte[] = external;

# Returns Hex encoded CRC32B value for the provided element. This accepts `string`, `byte[]`, `json` and `xml` content.
#
# + input - Value for checksum generation
# + return - The generated checksum
public function crc32b(any input) returns string = external;

# Returns RSA-MD5 based signature value for the given data.
#
# + input - The content to be signed
# + privateKey - Private key used for signing
# + return - The generated signature or error if private key is invalid
public function signRsaMd5(byte[] input, PrivateKey privateKey) returns byte[]|error = external;

# Returns RSA-SHA1 based signature value for the given data.
#
# + input - The content to be signed
# + privateKey - Private key used for signing
# + return - The generated signature or error if private key is invalid
public function signRsaSha1(byte[] input, PrivateKey privateKey) returns byte[]|error = external;

# Returns RSA-SHA256 based signature value for the given data.
#
# + input - The content to be signed
# + privateKey - Private key used for signing
# + return - The generated signature or error if private key is invalid
public function signRsaSha256(byte[] input, PrivateKey privateKey) returns byte[]|error = external;

# Returns RSA-SHA384 based signature value for the given data.
#
# + input - The content to be signed
# + privateKey - Private key used for signing.
# + return - The generated signature or error if private key is invalid
public function signRsaSha384(byte[] input, PrivateKey privateKey) returns byte[]|error = external;

# Returns RSA-SHA512 based signature value for the given data.
#
# + input - The content to be signed
# + privateKey - Private key used for signing
# + return - The generated signature or error if private key is invalid
public function signRsaSha512(byte[] input, PrivateKey privateKey) returns byte[]|error = external;

# Verify RSA-MD5 based signature.
#
# + data - The content to be verified
# + signature - Signature value
# + publicKey - Public key used for verification
# + return - Validity of the signature or error if public key is invalid
public function verifyRsaMd5Signature(byte[] data, byte[] signature, PublicKey publicKey) 
returns boolean|error = external;

# Verify RSA-SHA1 based signature.
#
# + data - The content to be verified
# + signature - Signature value
# + publicKey - Public key used for verification
# + return - Validity of the signature or error if public key is invalid
public function verifyRsaSha1Signature(byte[] data, byte[] signature, PublicKey publicKey)
returns boolean|error = external;

# Verify RSA-SHA256 based signature.
#
# + data - The content to be verified
# + signature - Signature value
# + publicKey - Public key used for verification
# + return - Validity of the signature or error if public key is invalid
public function verifyRsaSha256Signature(byte[] data, byte[] signature, PublicKey publicKey)
returns boolean|error = external;

# Verify RSA-SHA384 based signature.
#
# + data - The content to be verified
# + signature - Signature value
# + publicKey - Public key used for verification
# + return - Validity of the signature or error if public key is invalid
public function verifyRsaSha384Signature(byte[] data, byte[] signature, PublicKey publicKey)
returns boolean|error = external;

# Verify RSA-SHA512 based signature.
#
# + data - The content to be verified
# + signature - Signature value
# + publicKey - Public key used for verification
# + return - Validity of the signature or error if public key is invalid
public function verifyRsaSha512Signature(byte[] data, byte[] signature, PublicKey publicKey)
returns boolean|error = external;

# Read a private key from the provided PKCS#12 archive file.
#
# + keyStore - Key store configuration
# + keyAlias - Key alias
# + keyPassword - Key password
# + return - Reference to the private key or error if private key was unreadable
public function decodePrivateKey(KeyStore? keyStore = (), string? keyAlias = (), string? keyPassword = ())
returns PrivateKey|error = external;

# Read a public key from the provided PKCS#12 archive file.
#
# + keyStore - Key store configuration
# + keyAlias - Key alias
# + return - Reference to the public key or error if private key was unreadable
public function decodePublicKey(KeyStore? keyStore = (), string? keyAlias = ()) returns PublicKey|error = external;

# Returns RSA encrypted value for the given data.
#
# + input - The content to be encrypted
# + key - Private or public key used for encryption
# + padding - The padding
# + return - Encrypted data or error if key is invalid
public function encryptRsaEcb(byte[] input, PrivateKey|PublicKey key, RsaPadding padding = "PKCS1")
                              returns byte[]|error = external;

# Returns AES CBC encrypted value for the given data.
#
# + input - The content to be encrypted
# + key - Encryption key
# + iv - Initialization vector
# + padding - The padding
# + return - Encrypted data or error if key is invalid
public function encryptAesCbc(byte[] input, byte[] key, byte[] iv, AesPadding padding = "PKCS5")
                              returns byte[]|error = external;

# Returns AES ECB encrypted value for the given data.
#
# + input - The content to be encrypted
# + key - Encryption key
# + padding - The padding
# + return - Encrypted data or error if key is invalid
public function encryptAesEcb(byte[] input, byte[] key, AesPadding padding = "PKCS5") returns byte[]|error = external;

# Returns AES GCM encrypted value for the given data.
#
# + input - The content to be encrypted
# + key - Encryption key
# + iv - Initialization vector
# + padding - The padding
# + tagSize - Tag size
# + return - Encrypted data or error if key is invalid
public function encryptAesGcm(byte[] input, byte[] key, byte[] iv, AesPadding padding = "PKCS5",
                              int tagSize = 128) returns byte[]|error = external;

# Returns RSA decrypted value for the given RSA encrypted data.
#
# + input - The content to be decrypted
# + key - Private or public key used for encryption
# + padding - The padding
# + return - Decrypted data or error if key is invalid
public function decryptRsaEcb(byte[] input, PrivateKey|PublicKey key, RsaPadding padding = "PKCS1")
                              returns byte[]|error = external;

# Returns AES CBC decrypted value for the given AES CBC encrypted data.
#
# + input - The content to be decrypted
# + key - Encryption key
# + iv - Initialization vector
# + padding - The padding
# + return - Decrypted data or error if key is invalid
public function decryptAesCbc(byte[] input, byte[] key, byte[] iv, AesPadding padding = "PKCS5")
                              returns byte[]|error = external;

# Returns AES ECB decrypted value for the given AES ECB encrypted data.
#
# + input - The content to be decrypted
# + key - Encryption key
# + padding - The padding
# + return - Decrypted data or error if key is invalid
public function decryptAesEcb(byte[] input, byte[] key, AesPadding padding = "PKCS5") returns byte[]|error = external;

# Returns AES GCM decrypted value for the given AES GCM encrypted data.
#
# + input - The content to be decrypted
# + key - Encryption key
# + iv - Initialization vector
# + padding - The padding
# + tagSize - Tag size
# + return - Decrypted data or error if key is invalid
public function decryptAesGcm(byte[] input, byte[] key, byte[] iv, AesPadding padding = "PKCS5",
                              int tagSize = 128) returns byte[]|error = external;
