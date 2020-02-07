import ballerina/crypto;
import ballerina/io;
import ballerina/lang.'string as strings;
import ballerina/math;

public function main() returns error? {

    // Input value for crypto operations.
    string input = "Hello Ballerina!";
    byte[] inputArr = input.toBytes();

    // Hashing input value using MD5 hashing algorithm, and printing hash value using Hex encoding.
    byte[] output = crypto:hashMd5(inputArr);
    io:println("Hex encoded hash with MD5: " + output.toBase16());

    // Hashing input value using SHA1 hashing algorithm, and printing hash value using Base64 encoding.
    output = crypto:hashSha1(inputArr);
    io:println("Base64 encoded hash with SHA1: " + output.toBase64());

    // Hashing input value using SHA256 hashing algorithm, and printing hash value using Hex encoding.
    output = crypto:hashSha256(inputArr);
    io:println("Hex encoded hash with SHA256: " + output.toBase16());

    // Hashing input value using SHA384 hashing algorithm, and printing hash value using Base64 encoding.
    output = crypto:hashSha384(inputArr);
    io:println("Base64 encoded hash with SHA384: " + output.toBase64());

    // Hashing input value using SHA512 hashing algorithm, and printing hash value using Hex encoding.
    output = crypto:hashSha512(inputArr);
    io:println("Hex encoded hash with SHA512: " + output.toBase16());


    // The key used for HMAC generation.
    string key = "somesecret";
    byte[] keyArr = key.toBytes();

    // HMAC generation for input value using MD5 hashing algorithm, and printing HMAC value using Hex encoding.
    output = crypto:hmacMd5(inputArr, keyArr);
    io:println("Hex encoded HMAC with MD5: " + output.toBase16());

    // HMAC generation for input value using SHA1 hashing algorithm, and printing HMAC value using Base64 encoding.
    output = crypto:hmacSha1(inputArr, keyArr);
    io:println("Base64 encoded HMAC with SHA1: " + output.toBase64());

    // HMAC generation for input value using SHA256 hashing algorithm, and printing HMAC value using Hex encoding.
    output = crypto:hmacSha256(inputArr, keyArr);
    io:println("Hex encoded HMAC with SHA256: " + output.toBase16());

    // HMAC generation for input value using SHA384 hashing algorithm, and printing HMAC value using Base64 encoding.
    output = crypto:hmacSha384(inputArr, keyArr);
    io:println("Base64 encoded HMAC with SHA384: " + output.toBase64());

    // HMAC generation for input value using SHA512 hashing algorithm, and printing HMAC value using Hex encoding.
    output = crypto:hmacSha512(inputArr, keyArr);
    io:println("Hex encoded HMAC with SHA512: " + output.toBase16());


    // Hex encoded CRC32B checksum generation for input value.
    io:println("CRC32B for text: " + crypto:crc32b(inputArr));

    // Hex encoded CRC32B checksum generation for XML data.
    xml xmlContent = xml `<foo>Hello Ballerina</foo>`;
    io:println("CRC32 for xml content: " + crypto:crc32b(xmlContent.toString().toBytes()));


    // Obtaining reference to a RSA private key stored within a PKCS#12 or PFX format archive file.
    crypto:KeyStore keyStore = {path: "./sampleKeystore.p12", password: "ballerina"};
    var privateKey = crypto:decodePrivateKey(keyStore, "ballerina", "ballerina");

    if (privateKey is crypto:PrivateKey) {
        // Signing input value using RSA-MD5 signature algorithms, and printing the signature value using Hex encoding.
        output = check crypto:signRsaMd5(inputArr, privateKey);
        io:println("Hex encoded RSA-MD5 signature: " + output.toBase16());

        // Signing input value using RSA-MD5 signature algorithms, and printing the signature value using Base64 encoding.
        output = check crypto:signRsaSha1(inputArr, privateKey);
        io:println("Base64 encoded RSA-SHA1 signature: " + output.toBase64());

        // Signing input value using RSA-MD5 signature algorithms, and printing the signature value using Hex encoding.
        output = check crypto:signRsaSha256(inputArr, privateKey);
        io:println("Hex encoded RSA-SHA256 signature: " + output.toBase16());

        // Signing input value using RSA-MD5 signature algorithms, and printing the signature value using Base64 encoding.
        output = check crypto:signRsaSha384(inputArr, privateKey);
        io:println("Base64 encoded RSA-SHA384 signature: " + output.toBase64());

        // Signing input value using RSA-MD5 signature algorithms, and printing the signature value using Hex encoding.
        output = check crypto:signRsaSha512(inputArr, privateKey);
        io:println("Hex encoded RSA-SHA512 signature: " + output.toBase16());
    } else {
        io:println("Invalid private key");
    }

     // Randomly generate a 128 bit key for AES encryption.
     byte[16] rsaKeyArr = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
     foreach var i in 0 ... 15 {
        rsaKeyArr[i] = <byte>(math:randomInRange(0, 255));
     }

     // Randomly generate a 128 bit IV for AES encryption.
     byte[16] ivArr = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
     foreach var i in 0 ... 15 {
        ivArr[i] = <byte>(math:randomInRange(0, 255));
     }

     // Encrypt and decrypt input value using AES CBC PKCS5 padding.
     output = check crypto:encryptAesCbc(inputArr, rsaKeyArr, ivArr);
     output = check crypto:decryptAesCbc(output, rsaKeyArr, ivArr);
     io:println("AES CBC PKCS5 decrypted value: " + check strings:fromBytes(output));

     // Encrypt and decrypt input value using AES CBC no padding.
     output = check crypto:encryptAesCbc(inputArr, rsaKeyArr, ivArr, crypto:NONE);
     output = check crypto:decryptAesCbc(output, rsaKeyArr, ivArr, crypto:NONE);
     io:println("AES CBC no padding decrypted value: " + check strings:fromBytes(output));

     // Encrypt and decrypt input value using AES GCM PKCS5 padding.
     output = check crypto:encryptAesGcm(inputArr, rsaKeyArr, ivArr);
     output = check crypto:decryptAesGcm(output, rsaKeyArr, ivArr);
     io:println("AES GCM PKCS5 decrypted value: " + check strings:fromBytes(output));

     // Encrypt and decrypt input value using AES GCM no padding.
     output = check crypto:encryptAesGcm(inputArr, rsaKeyArr, ivArr, crypto:NONE);
     output = check crypto:decryptAesGcm(output, rsaKeyArr, ivArr, crypto:NONE);
     io:println("AES GCM no padding decrypted value: " + check strings:fromBytes(output));

     // Encrypt and decrypt input value using AES ECB PKCS5 padding.
     output = check crypto:encryptAesEcb(inputArr, rsaKeyArr);
     output = check crypto:decryptAesEcb(output, rsaKeyArr);
     io:println("AES ECB PKCS5 decrypted value: " + check strings:fromBytes(output));

     // Encrypt and decrypt input value using AES ECB no padding.
     output = check crypto:encryptAesEcb(inputArr, rsaKeyArr, crypto:NONE);
     output = check crypto:decryptAesEcb(output, rsaKeyArr, crypto:NONE);
     io:println("AES ECB no padding decrypted value: " + check strings:fromBytes(output));

     // Public key used for RSA encryption.
     crypto:PublicKey rsaPublicKey = check crypto:decodePublicKey(keyStore, "ballerina");

     // Private key used for RSA decryption.
     crypto:PrivateKey rsaPrivateKey = check crypto:decodePrivateKey(keyStore, "ballerina", "ballerina");

     // Encrypt and decrypt input value using RSA ECB PKCS1 padding.
     output = check crypto:encryptRsaEcb(inputArr, rsaPublicKey);
     output = check crypto:decryptRsaEcb(output, rsaPrivateKey);
     io:println("RSA ECB PKCS1 decrypted value: " + check strings:fromBytes(output));

     // Encrypt and decrypt input value using RSA ECB OAEPwithSHA512andMGF1 padding.
     output = check crypto:encryptRsaEcb(inputArr, rsaPublicKey, crypto:OAEPwithSHA512andMGF1);
     output = check crypto:decryptRsaEcb(output, rsaPrivateKey, crypto:OAEPwithSHA512andMGF1);
     io:println("RSA ECB OAEPwithSHA512andMGF1 decrypted value: " + check strings:fromBytes(output));
}
