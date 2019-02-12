## Module overview

This module provides the necessary utilities that are required to hash content using different hashing mechanisms and algorithms. 

## Samples

### Hashing

The sample given below shows how to use hashing functions such as `hashSha512` to calculate the hash value of a byte array and then encode the byte array using a common encoding algorithm.

```ballerina
import ballerina/crypto;
import ballerina/encoding;
import ballerina/io;

public function main() {
     // Input value for crypto operations
     string input = "Hello Ballerina";
     byte[] inputArr = input.toByteArray("UTF-8");
     byte[] output = [];

     // Hashing input value using different hashing algorithms, and printing hash value using Hex encoding.
     output = crypto:hashMd5(inputArr);
     io:println("Hash with MD5: " + encoding:encodeHex(output));

     output = crypto:hashSha1(inputArr);
     io:println("Hash with SHA1: " + encoding:encodeHex(output));

     output = crypto:hashSha256(inputArr);
     io:println("Hash with SHA256: " + encoding:encodeHex(output));

     output = crypto:hashSha384(inputArr);
     io:println("Hash with SHA384: " + encoding:encodeHex(output));

     output = crypto:hashSha512(inputArr);
     io:println("Hash with SHA512: " + encoding:encodeHex(output));
}
```

### HMAC Generation

The sample given below shows how to use HMAC functions such as `hmacSha512` to calculate the HMAC value of a byte array and then encode the byte array using a common encoding algorithm.


```ballerina
import ballerina/crypto;
import ballerina/encoding;
import ballerina/io;

public function main() {
     string charEncoding = "UTF-8";

     // Input value for crypto operations
     string input = "Hello Ballerina";
     byte[] inputArr = input.toByteArray(charEncoding);

     // The key used for HMAC generation.
     string key = "somesecret";
     byte[] keyArr = key.toByteArray(charEncoding);

     byte[] output = [];

     // HMAC generation for input value using different HMAC algorithms, and printing HMAC value using Hex encoding.
     output = crypto:hmacMd5(inputArr, keyArr);
     io:println("HMAC with MD5: " + encoding:encodeHex(output));

     output = crypto:hmacSha1(inputArr, keyArr);
     io:println("HMAC with SHA1: " + encoding:encodeHex(output));

     output = crypto:hmacSha256(inputArr, keyArr);
     io:println("HMAC with SHA256: " + encoding:encodeHex(output));

     output = crypto:hmacSha384(inputArr, keyArr);
     io:println("HMAC with SHA384: " + encoding:encodeHex(output));

     output = crypto:hmacSha512(inputArr, keyArr);
     io:println("HMAC with SHA512: " + encoding:encodeHex(output));
}
```

### Calculating CRC32B Checksum

The sample given below shows how to use `crc32b` function to calculate the CRC32B checksum.


```ballerina
import ballerina/crypto;
import ballerina/io;

public function main() {
     // Input value for cryto operations
     string input = "Hello Ballerina";

     // Hex encoded CRC32B checksum generation for input value.
     io:println("CRC32B for text: " + crypto:crc32b(input));

     // The XML content to be hashed.
     xml xmlContent = xml `<foo>Hello Ballerina</foo>`;
     io:println("CRC32 for xml content: " + crypto:crc32b(xmlContent));
}
```

### Signing

The sample given below shows how to use signing functions such as `signRsaSha512` to get the signature value of a byte array and then encode the byte array using a common encoding algorithm.


```ballerina
import ballerina/crypto;
import ballerina/encoding;
import ballerina/io;

public function main() returns error? {
     // Input value for cryto operations
     string input = "Hello Ballerina";
     byte[] inputArr = input.toByteArray("UTF-8");

     // PrivateKey used for signing operations.
     crypto:KeyStore keyStore = { path: "/home/ballerina/keystore.p12", password: "ballerina" };
     crypto:PrivateKey privateKey = check crypto:decodePrivateKey(keyStore = keyStore, keyAlias = "ballerina",
                                                            keyPassword = "ballerina");


     // Signing input value using different signature algorithms, and printing the signature value
     // using Hex encoding.
     output = check crypto:signRsaMd5(inputArr, privateKey);
     io:println("RSA-MD5 signature: " + encoding:encodeHex(output));

     output = check crypto:signRsaSha1(inputArr, privateKey);
     io:println("RSA-SHA1 signature: " + encoding:encodeHex(output));

     output = check crypto:signRsaSha256(inputArr, privateKey);
     io:println("RSA-SHA256 signature: " + encoding:encodeHex(output));

     output = check crypto:signRsaSha384(inputArr, privateKey);
     io:println("RSA-SHA384 signature: " + encoding:encodeHex(output));

     output = check crypto:signRsaSha512(inputArr, privateKey);
     io:println("RSA-SHA512 signature: " + encoding:encodeHex(output));
}
```

### RSA Encryption

The sample given below shows how to use encryption function `encryptRsaEcb` and decryption function `decryptRsaEcb` to perform RSA encryption and decryption.


```ballerina
import ballerina/crypto;
import ballerina/encoding;
import ballerina/io;

public function main() returns error? {
     // Input value for cryto operations.
     string input = "Hello Ballerina";
     byte[] inputArr = input.toByteArray("UTF-8");

     crypto:KeyStore keyStore = { path: "/home/ballerina/keystore.p12", password: "ballerina" };

     // Public key used for encryption.
     crypto:PublicKey publicKey = check crypto:decodePublicKey(keyStore = keyStore, keyAlias = "ballerina");

     // Private key used for decryption.
     crypto:PrivateKey privateKey = check crypto:decodePrivateKey(keyStore = keyStore, keyAlias = "ballerina",
                                                            keyPassword = "ballerina");

     // Encrypt and decrypt input value using RSA ECB PKCS1 padding.
     byte[] output = check crypto:encryptRsaEcb(inputArr, publicKey);
     output = check crypto:decryptRsaEcb(output, privateKey);
     io:println("RSA ECB PKCS1 decrypted value: " + encoding:byteArrayToString(output));

     // Encrypt and decrypt input value using RSA ECB OAEPwithSHA512andMGF1 padding.
     output = check crypto:encryptRsaEcb(inputArr, publicKey, padding = crypto:OAEPwithSHA512andMGF1);
     output = check crypto:decryptRsaEcb(output, privateKey, padding = crypto:OAEPwithSHA512andMGF1);
     io:println("RSA ECB OAEPwithSHA512andMGF1 decrypted value: " + encoding:byteArrayToString(output));
}
```

### AES Encryption

The sample given below shows how to use encryption function `encryptAesCbc` and decryption function `decryptAesCbc` to perform AES encryption and decryption.


```ballerina
import ballerina/crypto;
import ballerina/encoding;
import ballerina/io;
import ballerina/math;

public function main() returns error? {
     // Input value for cryto operations.
     string input = "Hello Ballerina!";
     byte[] inputArr = input.toByteArray("UTF-8");

     // Randomly generate a 128 bit key.
     byte[16] keyArr = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
     foreach var i in 0...15 {
        keyArr[i] = check byte.convert(math:randomInRange(0, 255));
     }

     // Randomly generate a 128 bit IV.
     byte[16] ivArr = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
     foreach var i in 0...15 {
        ivArr[i] = check byte.convert(math:randomInRange(0, 255));
     }

     // Encrypt and decrypt input value using AES CBC PKCS5 padding.
     byte[] output = check crypto:encryptAesCbc(inputArr, keyArr, ivArr);
     output = check crypto:decryptAesCbc(output, keyArr, ivArr);
     io:println("AES CBC PKCS5 decrypted value: " + encoding:byteArrayToString(output));

     // Encrypt and decrypt input value using AES CBC no padding.
     output = check crypto:encryptAesCbc(inputArr, keyArr, ivArr, padding = crypto:NONE);
     output = check crypto:decryptAesCbc(output, keyArr, ivArr, padding = crypto:NONE);
     io:println("AES CBC no padding decrypted value: " + encoding:byteArrayToString(output));

     // Encrypt and decrypt input value using AES GCM PKCS5 padding.
     output = check crypto:encryptAesGcm(inputArr, keyArr, ivArr);
     output = check crypto:decryptAesGcm(output, keyArr, ivArr);
     io:println("AES GCM PKCS5 decrypted value: " + encoding:byteArrayToString(output));

     // Encrypt and decrypt input value using AES GCM no padding.
     output = check crypto:encryptAesGcm(inputArr, keyArr, ivArr, padding = crypto:NONE);
     output = check crypto:decryptAesGcm(output, keyArr, ivArr, padding = crypto:NONE);
     io:println("AES GCM no padding decrypted value: " + encoding:byteArrayToString(output));

     // Encrypt and decrypt input value using AES ECB PKCS5 padding.
     output = check crypto:encryptAesEcb(inputArr, keyArr);
     output = check crypto:decryptAesEcb(output, keyArr);
     io:println("AES ECB PKCS5 decrypted value: " + encoding:byteArrayToString(output));

     // Encrypt and decrypt input value using AES ECB no padding.
     output = check crypto:encryptAesEcb(inputArr, keyArr, padding = crypto:NONE);
     output = check crypto:decryptAesEcb(output, keyArr, padding = crypto:NONE);
     io:println("AES ECB no padding decrypted value: " + encoding:byteArrayToString(output));
}
```
