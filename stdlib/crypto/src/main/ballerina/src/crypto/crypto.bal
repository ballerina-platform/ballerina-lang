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
import ballerina/java;

# The key algorithms supported by the Crypto module.
public type KeyAlgorithm RSA;

# The `RSA` algorithm.
public const RSA = "RSA";

# Padding algorithms supported by AES encryption and decryption.
public type AesPadding NONE|PKCS5;

# Padding algorithms supported with RSA encryption and decryption.
public type RsaPadding PKCS1|OAEPwithMD5andMGF1|OAEPWithSHA1AndMGF1|OAEPWithSHA256AndMGF1|OAEPwithSHA384andMGF1|
                       OAEPwithSHA512andMGF1;

# No padding.
public const NONE = "NONE";

# The `PKCS1` padding mode.
public const PKCS1 = "PKCS1";

# The `PKCS5` padding mode.
public const PKCS5 = "PKCS5";

# The `OAEPwithMD5andMGF1` padding mode.
public const OAEPwithMD5andMGF1 = "OAEPwithMD5andMGF1";

# The `OAEPWithSHA1AndMGF1` padding mode.
public const OAEPWithSHA1AndMGF1 = "OAEPWithSHA1AndMGF1";

# The `OAEPWithSHA256AndMGF1` padding mode.
public const OAEPWithSHA256AndMGF1 = "OAEPWithSHA256AndMGF1";

# The `OAEPwithSHA384andMGF1` padding mode.
public const OAEPwithSHA384andMGF1 = "OAEPwithSHA384andMGF1";

# The `OAEPwithSHA512andMGF1` padding mode.
public const OAEPwithSHA512andMGF1 = "OAEPwithSHA512andMGF1";

# Key store related configurations.
#
# + path - Path to the key store file
# + password - Key store password
public type KeyStore record {|
    string path;
    string password;
|};

# Trust store related configurations.
#
# + path - Path to the key store file
# + password - Key store password
public type TrustStore record {|
    string path;
    string password;
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
    Certificate certificate?;
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

# Returns the MD5 hash of the given data.
# ```ballerina
#  string dataString = "Hello Ballerina";
#  byte[] data = dataString.toBytes();
#  byte[] hash = crypto:hashMd5(data);
# ```
#
# + input - Value to be hashed
# + return - Hashed output
public function hashMd5(byte[] input) returns byte[] = @java:Method {
    name: "hashMd5",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Hash"
} external;

# Returns the SHA-1 hash of the given data.
# ```ballerina
#  string dataString = "Hello Ballerina";
#  byte[] data = dataString.toBytes();
#  byte[] hash = crypto:hashSha1(data);
# ```
#
# + input - Value to be hashed
# + return - Hashed output
public function hashSha1(byte[] input) returns byte[] = @java:Method {
    name: "hashSha1",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Hash"
} external;

# Returns the SHA-256 hash of the given data.
# ```ballerina
#  string dataString = "Hello Ballerina";
#  byte[] data = dataString.toBytes();
#  byte[] hash = crypto:hashSha256(data);
# ```
#
# + input - Value to be hashed
# + return - Hashed output
public function hashSha256(byte[] input) returns byte[] = @java:Method {
    name: "hashSha256",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Hash"
} external;

# Returns the SHA-384 hash of the given data.
# ```ballerina
#  string dataString = "Hello Ballerina";
#  byte[] data = dataString.toBytes();
#  byte[] hash = crypto:hashSha384(data);
# ```
#
# + input - Value to be hashed
# + return - Hashed output
public function hashSha384(byte[] input) returns byte[] = @java:Method {
    name: "hashSha384",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Hash"
} external;

# Returns the SHA-512 hash of the given data.
# ```ballerina
#  string dataString = "Hello Ballerina";
#  byte[] data = dataString.toBytes();
#  byte[] hash = crypto:hashSha512(data);
# ```
#
# + input - Value to be hashed
# + return - Hashed output
public function hashSha512(byte[] input) returns byte[] = @java:Method {
    name: "hashSha512",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Hash"
} external;

# Returns the HMAC using the MD-5 hash function of the given data.
# ```ballerina
#  string stringData = "Hello Ballerina";
#  byte[] data = stringData.toBytes();
#  string keyString = "some-secret";
#  byte[] key = keyString.toBytes();
#  byte[] hmac = crypto:hmacMd5(data, key);
# ```
#
# + input - Value to be hashed
# + key - Key used for HMAC generation
# + return - HMAC output
public function hmacMd5(byte[] input, byte[] key) returns byte[] = @java:Method {
    name: "hmacMd5",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Hmac"
} external;

# Returns the HMAC using the SHA-1 hash function of the given data.
# ```ballerina
#  string stringData = "Hello Ballerina";
#  byte[] data = stringData.toBytes();
#  string keyString = "some-secret";
#  byte[] key = keyString.toBytes();
#  byte[] hmac = crypto:hmacSha1(data, key);
# ```
#
# + input - Value to be hashed
# + key - Key used for HMAC generation
# + return - HMAC output
public function hmacSha1(byte[] input, byte[] key) returns byte[] = @java:Method {
    name: "hmacSha1",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Hmac"
} external;

# Returns the HMAC using the SHA-256 hash function of the given data.
# ```ballerina
#  string stringData = "Hello Ballerina";
#  byte[] data = stringData.toBytes();
#  string keyString = "some-secret";
#  byte[] key = keyString.toBytes();
#  byte[] hmac = crypto:hmacSha256(data, key);
# ```
#
# + input - Value to be hashed
# + key - Key used for HMAC generation
# + return - HMAC output
public function hmacSha256(byte[] input, byte[] key) returns byte[] = @java:Method {
    name: "hmacSha256",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Hmac"
} external;

# Returns the HMAC using the SHA-384 hash function of the given data.
# ```ballerina
#  string stringData = "Hello Ballerina";
#  byte[] data = stringData.toBytes();
#  string keyString = "some-secret";
#  byte[] key = keyString.toBytes();
#  byte[] hmac = crypto:hmacSha384(data, key);
# ```
#
# + input - Value to be hashed
# + key - Key used for HMAC generation
# + return - HMAC output
public function hmacSha384(byte[] input, byte[] key) returns byte[] = @java:Method {
    name: "hmacSha384",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Hmac"
} external;

# Returns the HMAC using the SHA-512 hash function of the given data.
# ```ballerina
#  string stringData = "Hello Ballerina";
#  byte[] data = stringData.toBytes();
#  string keyString = "some-secret";
#  byte[] key = keyString.toBytes();
#  byte[] hmac = crypto:hmacSha512(data, key);
# ```
#
# + input - Value to be hashed
# + key - Key used for HMAC generation
# + return - HMAC output
public function hmacSha512(byte[] input, byte[] key) returns byte[] = @java:Method {
    name: "hmacSha512",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Hmac"
} external;

# Returns the Hex-encoded CRC32B value for the provided element.
# ```ballerina
#  string stringData = "Hello Ballerina";
#  byte[] data = stringData.toBytes();
#  string checksum = crypto:crc32b(data);
# ```
#
# + input - Value for checksum generation
# + return - The generated checksum
public function crc32b(byte[] input) returns string = @java:Method {
    name: "crc32b",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Hash"
} external;

# Returns the RSA-MD5-based signature value for the given data.
# ```ballerina
#  string stringData = "Hello Ballerina";
#  byte[] data = stringData.toBytes();
#  crypto:KeyStore keyStore = {
#      path: "/home/ballerina/keystore.p12",
#      password: "keystorePassword"
#  };
#  crypto:PrivateKey privateKey =
#      checkpanic crypto:decodePrivateKey(keyStore, "keyAlias", "keyPassword");
#  byte[]|crypto:Error signature = crypto:signRsaMd5(data, privateKey);
# ```
#
# + input - The content to be signed
# + privateKey - Private key used for signing
# + return - The generated signature or else a `crypto:Error` if the private key is invalid
public function signRsaMd5(byte[] input, PrivateKey privateKey) returns byte[]|Error = @java:Method {
    name: "signRsaMd5",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Sign"
} external;

# Returns the RSA-SHA1-based signature value for the given data.
# ```ballerina
#  string stringData = "Hello Ballerina";
#  byte[] data = stringData.toBytes();
#  crypto:KeyStore keyStore = {
#      path: "/home/ballerina/keystore.p12",
#      password: "keystorePassword"
#  };
#  crypto:PrivateKey privateKey =
#      checkpanic crypto:decodePrivateKey(keyStore, "keyAlias", "keyPassword");
#  byte[]|crypto:Error signature = crypto:signRsaSha1(data, privateKey);
# ```
#
# + input - The content to be signed
# + privateKey - Private key used for signing
# + return - The generated signature or else a `crypto:Error` if the private key is invalid
public function signRsaSha1(byte[] input, PrivateKey privateKey) returns byte[]|Error = @java:Method {
    name: "signRsaSha1",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Sign"
} external;

# Returns the RSA-SHA256-based signature value for the given data.
# ```ballerina
#  string stringData = "Hello Ballerina";
#  byte[] data = stringData.toBytes();
#  crypto:KeyStore keyStore = {
#      path: "/home/ballerina/keystore.p12",
#      password: "keystorePassword"
#  };
#  crypto:PrivateKey privateKey =
#      checkpanic crypto:decodePrivateKey(keyStore, "keyAlias", "keyPassword");
#  byte[]|crypto:Error signature = crypto:signRsaSha256(data, privateKey);
# ```
#
# + input - The content to be signed
# + privateKey - Private key used for signing
# + return - The generated signature or else a `crypto:Error` if the private key is invalid
public function signRsaSha256(byte[] input, PrivateKey privateKey) returns byte[]|Error = @java:Method {
    name: "signRsaSha256",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Sign"
} external;

# Returns the RSA-SHA384-based signature value for the given data.
# ```ballerina
#  string stringData = "Hello Ballerina";
#  byte[] data = stringData.toBytes();
#  crypto:KeyStore keyStore = {
#      path: "/home/ballerina/keystore.p12",
#      password: "keystorePassword"
#  };
#  crypto:PrivateKey privateKey =
#      checkpanic crypto:decodePrivateKey(keyStore, "keyAlias", "keyPassword");
#  byte[]|crypto:Error signature = crypto:signRsaSha384(data, privateKey);
# ```
#
# + input - The content to be signed
# + privateKey - Private key used for signing
# + return - The generated signature or else a `crypto:Error` if the private key is invalid
public function signRsaSha384(byte[] input, PrivateKey privateKey) returns byte[]|Error = @java:Method {
    name: "signRsaSha384",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Sign"
} external;

# Returns the RSA-SHA512-based signature value for the given data.
# ```ballerina
#  string stringData = "Hello Ballerina";
#  byte[] data = stringData.toBytes();
#  crypto:KeyStore keyStore = {
#      path: "/home/ballerina/keystore.p12",
#      password: "keystorePassword"
#  };
#  crypto:PrivateKey privateKey =
#      checkpanic crypto:decodePrivateKey(keyStore, "keyAlias", "keyPassword");
#  byte[]|crypto:Error signature = crypto:signRsaSha512(data, privateKey);
# ```
#
# + input - The content to be signed
# + privateKey - Private key used for signing
# + return - The generated signature or else a `crypto:Error` if the private key is invalid
public function signRsaSha512(byte[] input, PrivateKey privateKey) returns byte[]|Error = @java:Method {
    name: "signRsaSha512",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Sign"
} external;

# Verifies the RSA-MD5-based signature.
# ```ballerina
#  string stringData = "Hello Ballerina";
#  byte[] data = stringData.toBytes();
#  crypto:KeyStore keyStore = {
#      path: "/home/ballerina/keystore.p12",
#      password: "keystorePassword"
#  };
#  crypto:PrivateKey privateKey = checkpanic crypto:decodePrivateKey(keyStore, "keyAlias", "keyPassword");
#  byte[] signature = checkpanic crypto:signRsaMd5(data, privateKey);
#  crypto:PublicKey publicKey =  checkpanic crypto:decodePublicKey(keyStore, "keyAlias");
#  boolean|crypto:Error validity = crypto:verifyRsaMd5Signature(data, signature, publicKey);
# ```
#
# + data - The content to be verified
# + signature - Signature value
# + publicKey - Public key used for verification
# + return - Validity of the signature or else a `crypto:Error` if the public key is invalid
public function verifyRsaMd5Signature(byte[] data, byte[] signature, PublicKey publicKey)
                                      returns boolean|Error = @java:Method {
    name: "verifyRsaMd5Signature",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Sign"
} external;

# Verifies the RSA-SHA1-based signature.
# ```ballerina
#  string stringData = "Hello Ballerina";
#  byte[] data = stringData.toBytes();
#  crypto:KeyStore keyStore = {
#      path: "/home/ballerina/keystore.p12",
#      password: "keystorePassword"
#  };
#  crypto:PrivateKey privateKey = checkpanic crypto:decodePrivateKey(keyStore, "keyAlias", "keyPassword");
#  byte[] signature = checkpanic crypto:signRsaMd5(data, privateKey);
#  crypto:PublicKey publicKey =  checkpanic crypto:decodePublicKey(keyStore, "keyAlias");
#  boolean|crypto:Error validity = crypto:verifyRsaSha1Signature(data, signature, publicKey);
# ```
#
# + data - The content to be verified
# + signature - Signature value
# + publicKey - Public key used for verification
# + return - Validity of the signature or else a `crypto:Error` if the public key is invalid
public function verifyRsaSha1Signature(byte[] data, byte[] signature, PublicKey publicKey)
                                       returns boolean|Error = @java:Method {
    name: "verifyRsaSha1Signature",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Sign"
} external;

# Verifies the RSA-SHA256-based signature.
# ```ballerina
#  string stringData = "Hello Ballerina";
#  byte[] data = stringData.toBytes();
#  crypto:KeyStore keyStore = {
#      path: "/home/ballerina/keystore.p12",
#      password: "keystorePassword"
#  };
#  crypto:PrivateKey privateKey = checkpanic crypto:decodePrivateKey(keyStore, "keyAlias", "keyPassword");
#  byte[] signature = checkpanic crypto:signRsaMd5(data, privateKey);
#  crypto:PublicKey publicKey =  checkpanic crypto:decodePublicKey(keyStore, "keyAlias");
#  boolean|crypto:Error validity = crypto:verifyRsaSha256Signature(data, signature, publicKey);
# ```
#
# + data - The content to be verified
# + signature - Signature value
# + publicKey - Public key used for verification
# + return - Validity of the signature or else a `crypto:Error` if the public key is invalid
public function verifyRsaSha256Signature(byte[] data, byte[] signature, PublicKey publicKey)
                                         returns boolean|Error = @java:Method {
    name: "verifyRsaSha256Signature",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Sign"
} external;

# Verifies the RSA-SHA384-based signature.
# ```ballerina
#  string stringData = "Hello Ballerina";
#  byte[] data = stringData.toBytes();
#  crypto:KeyStore keyStore = {
#      path: "/home/ballerina/keystore.p12",
#      password: "keystorePassword"
#  };
#  crypto:PrivateKey privateKey = checkpanic crypto:decodePrivateKey(keyStore, "keyAlias", "keyPassword");
#  byte[] signature = checkpanic crypto:signRsaMd5(data, privateKey);
#  crypto:PublicKey publicKey =  checkpanic crypto:decodePublicKey(keyStore, "keyAlias");
#  boolean|crypto:Error validity = crypto:verifyRsaSha384Signature(data, signature, publicKey);
# ```
#
# + data - The content to be verified
# + signature - Signature value
# + publicKey - Public key used for verification
# + return - Validity of the signature or else a `crypto:Error` if the public key is invalid
public function verifyRsaSha384Signature(byte[] data, byte[] signature, PublicKey publicKey)
                                         returns boolean|Error = @java:Method {
    name: "verifyRsaSha384Signature",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Sign"
} external;

# Verifies the RSA-SHA512-based signature.
# ```ballerina
#  string stringData = "Hello Ballerina";
#  byte[] data = stringData.toBytes();
#  crypto:KeyStore keyStore = {
#      path: "/home/ballerina/keystore.p12",
#      password: "keystorePassword"
#  };
#  crypto:PrivateKey privateKey = checkpanic crypto:decodePrivateKey(keyStore, "keyAlias", "keyPassword");
#  byte[] signature = checkpanic crypto:signRsaMd5(data, privateKey);
#  crypto:PublicKey publicKey =  checkpanic crypto:decodePublicKey(keyStore, "keyAlias");
#  boolean|crypto:Error validity = crypto:verifyRsaSha512Signature(data, signature, publicKey);
# ```
#
# + data - The content to be verified
# + signature - Signature value
# + publicKey - Public key used for verification
# + return - Validity of the signature or else a `crypto:Error` if the public key is invalid
public function verifyRsaSha512Signature(byte[] data, byte[] signature, PublicKey publicKey)
                                         returns boolean|Error = @java:Method {
    name: "verifyRsaSha512Signature",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Sign"
} external;

# Reads a private key from the provided PKCS#12 archive file.
# ```ballerina
#  crypto:KeyStore keyStore = {
#      path: "/home/ballerina/keystore.p12",
#      password: "keystorePassword"
#  };
#  crypto:PrivateKey|crypto:Error privateKey = crypto:decodePrivateKey(keyStore, "keyAlias", "keyPassword");
# ```
#
# + keyStore - Key store or Trust store configurations
# + keyAlias - Key alias
# + keyPassword - Key password
# + return - Reference to the private key or else a `crypto:Error` if the private key was unreadable
public function decodePrivateKey(KeyStore|TrustStore keyStore, string keyAlias, string keyPassword)
                                 returns PrivateKey|Error = @java:Method {
    name: "decodePrivateKey",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Decode"
} external;

# Reads a public key from the provided PKCS#12 archive file.
# ```ballerina
#  crypto:KeyStore keyStore = {
#      path: "/home/ballerina/keystore.p12",
#      password: "keystorePassword"
#  };
#  crypto:PublicKey|crypto:Error publicKey = crypto:decodePublicKey(keyStore, "keyAlias");
# ```
#
# + keyStore - Key store or Trust store configurations
# + keyAlias - Key alias
# + return - Reference to the public key or else a `crypto:Error` if the private key was unreadable
public function decodePublicKey(KeyStore|TrustStore keyStore, string keyAlias) returns PublicKey|Error = @java:Method {
    name: "decodePublicKey",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Decode"
} external;

# Returns the `crypto:PublicKey` created with the modulus and exponent retrieved from the JWK's endpoint.
# ```ballerina
# string modulus = "luZFdW1ynitztkWLC6xKegbRWxky...";
# string exponent = "AQAB";
# crypto:PublicKey|crypto:Error publicKey = crypto:buildRsaPublicKey(modulus, exponent);
# ```
#
# + modulus - JWK modulus value ('n' parameter) for the RSA public key
# + exponent - JWK exponent value ('e' paramenter) for the RSA public key
# + return - Reference to the public key or else a `crypto:Error` if the modulus or exponent is invalid
public function buildRsaPublicKey(string modulus, string exponent) returns PublicKey|Error = @java:Method {
    name: "buildRsaPublicKey",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Decode"
} external;

# Returns the RSA-encrypted value for the given data.
# ```ballerina
#  string stringData = "Hello Ballerina";
#  byte[] data = stringData.toBytes();
#  crypto:KeyStore keyStore = {
#      path: "/home/ballerina/keystore.p12",
#      password: "keystorePassword"
#  };
#  crypto:PublicKey publicKey = checkpanic crypto:decodePublicKey(keyStore, "keyAlias");
#  byte[]|crypto:Error cipherText = crypto:encryptRsaEcb(data, publicKey);
# ```
#
# + input - The content to be encrypted
# + key - Private or public key used for encryption
# + padding - The padding
# + return - Encrypted data or else a `crypto:Error` if the key is invalid
public function encryptRsaEcb(byte[] input, PrivateKey|PublicKey key, RsaPadding padding = "PKCS1")
                              returns byte[]|Error = @java:Method {
    name: "encryptRsaEcb",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Encrypt"
} external;

# Returns the AES-CBC-encrypted value for the given data.
# ```ballerina
#  string dataString = "Hello Ballerina!";
#  byte[] data = dataString.toBytes();
#  byte[16] key = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
#  foreach var i in 0...15 {
#      key[i] = <byte>math:randomInRange(0, 255);
#  }
#  byte[16] initialVector = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
#  foreach var i in 0...15 {
#      initialVector[i] = <byte>math:randomInRange(0, 255);
#  }
#  byte[]|crypto:Error cipherText = crypto:encryptAesCbc(data, key, initialVector);
# ```
#
# + input - The content to be encrypted
# + key - Encryption key
# + iv - Initialization vector
# + padding - The padding
# + return - Encrypted data or else a `crypto:Error` if the key is invalid
public function encryptAesCbc(byte[] input, byte[] key, byte[] iv, AesPadding padding = "PKCS5")
                              returns byte[]|Error = @java:Method {
    name: "encryptAesCbc",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Encrypt"
} external;

# Returns the AES-ECB-encrypted value for the given data.
# ```ballerina
#  string dataString = "Hello Ballerina!";
#  byte[] data = dataString.toBytes();
#  byte[16] key = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
#  foreach var i in 0...15 {
#      key[i] = <byte>math:randomInRange(0, 255);
#  }
#  byte[]|crypto:Error cipherText = crypto:encryptAesEcb(data, key);
# ```
#
# + input - The content to be encrypted
# + key - Encryption key
# + padding - The padding
# + return - Encrypted data or else a `crypto:Error` if the key is invalid
public function encryptAesEcb(byte[] input, byte[] key, AesPadding padding = "PKCS5")
                              returns byte[]|Error = @java:Method {
    name: "encryptAesEcb",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Encrypt"
} external;

# Returns the AES-GCM-encrypted value for the given data.
# ```ballerina
#  string dataString = "Hello Ballerina!";
#  byte[] data = dataString.toBytes();
#  byte[16] key = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
#  foreach var i in 0...15 {
#      key[i] = <byte>math:randomInRange(0, 255);
#  }
#  byte[16] initialVector = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
#  foreach var i in 0...15 {
#      initialVector[i] = <byte>math:randomInRange(0, 255);
#  }
#  byte[]|crypto:Error cipherText = crypto:encryptAesGcm(data, key, initialVector);
# ```
#
# + input - The content to be encrypted
# + key - Encryption key
# + iv - Initialization vector
# + padding - The padding
# + tagSize - Tag size
# + return - Encrypted data or else a `crypto:Error` if the key is invalid
public function encryptAesGcm(byte[] input, byte[] key, byte[] iv, AesPadding padding = "PKCS5",
                              int tagSize = 128) returns byte[]|Error = @java:Method {
    name: "encryptAesGcm",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Encrypt"
} external;

# Returns the RSA-decrypted value for the given RSA-encrypted data.
# ```ballerina
#  string stringData = "Hello Ballerina";
#  byte[] data = stringData.toBytes();
#  crypto:KeyStore keyStore = {
#      path: "/home/ballerina/keystore.p12",
#      password: "keystorePassword"
#  };
#  crypto:PublicKey publicKey = checkpanic crypto:decodePublicKey(keyStore, "keyAlias");
#  crypto:PrivateKey privateKey = checkpanic crypto:decodePrivateKey(keyStore, "keyAlias", "keyPassword");
#  byte[] cipherText = checkpanic crypto:encryptRsaEcb(data, publicKey);
#  byte[]|crypto:Error plainText = checkpanic crypto:decryptRsaEcb(cipherText, privateKey);
# ```
#
# + input - The content to be decrypted
# + key - Private or public key used for encryption
# + padding - The padding
# + return - Decrypted data or else a `crypto:Error` if the key is invalid
public function decryptRsaEcb(byte[] input, PrivateKey|PublicKey key, RsaPadding padding = "PKCS1")
                              returns byte[]|Error = @java:Method {
    name: "decryptRsaEcb",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Decrypt"
} external;

# Returns the AES-CBC-decrypted value for the given AES-CBC-encrypted data.
# ```ballerina
#  string dataString = "Hello Ballerina!";
#  byte[] data = dataString.toBytes();
#  byte[16] key = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
#  foreach var i in 0...15 {
#      key[i] = <byte>math:randomInRange(0, 255);
#  }
#  byte[16] initialVector = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
#  foreach var i in 0...15 {
#      initialVector[i] = <byte>math:randomInRange(0, 255);
#  }
#  byte[] cipherText = checkpanic crypto:encryptAesCbc(data, key, initialVector);
#  byte[]|crypto:Error plainText = crypto:decryptAesCbc(cipherText, key, initialVector);
# ```
#
# + input - The content to be decrypted
# + key - Encryption key
# + iv - Initialization vector
# + padding - The padding
# + return - Decrypted data or else a `crypto:Error` if the key is invalid
public function decryptAesCbc(byte[] input, byte[] key, byte[] iv, AesPadding padding = "PKCS5")
                              returns byte[]|Error = @java:Method {
    name: "decryptAesCbc",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Decrypt"
} external;

# Returns the AES-ECB-decrypted value for the given AES-ECB-encrypted data.
# ```ballerina
#  string dataString = "Hello Ballerina!";
#  byte[] data = dataString.toBytes();
#  byte[16] key = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
#  foreach var i in 0...15 {
#      key[i] = <byte>math:randomInRange(0, 255);
#  }
#  byte[] cipherText = checkpanic crypto:encryptAesEcb(data, key);
#  byte[]|crypto:Error plainText = crypto:decryptAesEcb(cipherText, key);
# ```
#
# + input - The content to be decrypted
# + key - Encryption key
# + padding - The padding
# + return - Decrypted data or else a `crypto:Error` if the key is invalid
public function decryptAesEcb(byte[] input, byte[] key, AesPadding padding = "PKCS5")
                              returns byte[]|Error = @java:Method {
    name: "decryptAesEcb",
   'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Decrypt"
} external;

# Returns the AES-GCM-decrypted value for the given AES-GCM-encrypted data.
# ```ballerina
#  string dataString = "Hello Ballerina!";
#  byte[] data = dataString.toBytes();
#  byte[16] key = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
#  foreach var i in 0...15 {
#      key[i] = <byte>math:randomInRange(0, 255);
#  }
#  byte[16] initialVector = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
#  foreach var i in 0...15 {
#      initialVector[i] = <byte>math:randomInRange(0, 255);
#  }
#  byte[] cipherText = checkpanic crypto:encryptAesGcm(data, key, initialVector);
#  byte[]|crypto:Error plainText = crypto:decryptAesGcm(cipherText, key, initialVector);
# ```
#
# + input - The content to be decrypted
# + key - Encryption key
# + iv - Initialization vector
# + padding - The padding
# + tagSize - Tag size
# + return - Decrypted data or else a `crypto:Error` if the key is invalid
public function decryptAesGcm(byte[] input, byte[] key, byte[] iv, AesPadding padding = "PKCS5", int tagSize = 128)
                              returns byte[]|Error = @java:Method {
    name: "decryptAesGcm",
    'class: "org.ballerinalang.stdlib.crypto.nativeimpl.Decrypt"
} external;
