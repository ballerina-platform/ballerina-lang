import ballerina/io;
import ballerina/crypto;
import ballerina/encoding;

public function main() returns error? {

    // Input value for crypto operations.
    string input = "Hello Ballerina";
    byte[] inputArr = input.toByteArray("UTF-8");

    // Hashing input value using MD5 hashing algorithm, and printing hash value using Hex encoding.
    byte[] output = crypto:hashMd5(inputArr);
    io:println("Hex encoded hash with MD5: " + encoding:encodeHex(output));

    // Hashing input value using SHA1 hashing algorithm, and printing hash value using Base64 encoding.
    output = crypto:hashSha1(inputArr);
    io:println("Base64 encoded hash with SHA1: " + encoding:encodeBase64(output));

    // Hashing input value using SHA256 hashing algorithm, and printing hash value using Hex encoding.
    output = crypto:hashSha256(inputArr);
    io:println("Hex encoded hash with SHA256: " + encoding:encodeHex(output));

    // Hashing input value using SHA384 hashing algorithm, and printing hash value using Base64 encoding.
    output = crypto:hashSha384(inputArr);
    io:println("Base64 encoded hash with SHA384: " + encoding:encodeBase64(output));

    // Hashing input value using SHA512 hashing algorithm, and printing hash value using Hex encoding.
    output = crypto:hashSha512(inputArr);
    io:println("Hex encoded hash with SHA512: " + encoding:encodeHex(output));


    // The key used for HMAC generation.
    string key = "somesecret";
    byte[] keyArr = key.toByteArray("UTF-8");

    // HMAC generation for input value using MD5 hasing algorithm, and printing HMAC value using Hex encoding.
    output = crypto:hmacMd5(inputArr, keyArr);
    io:println("Hex encoded HMAC with MD5: " + encoding:encodeHex(output));

    // HMAC generation for input value using SHA1 hasing algorithm, and printing HMAC value using Base64 encoding.
    output = crypto:hmacSha1(inputArr, keyArr);
    io:println("Base64 encoded HMAC with SHA1: " + encoding:encodeBase64(output));

    // HMAC generation for input value using SHA256 hasing algorithm, and printing HMAC value using Hex encoding.
    output = crypto:hmacSha256(inputArr, keyArr);
    io:println("Hex encoded HMAC with SHA256: " + encoding:encodeHex(output));

    // HMAC generation for input value using SHA384 hasing algorithm, and printing HMAC value using Base64 encoding.
    output = crypto:hmacSha384(inputArr, keyArr);
    io:println("Base64 encoded HMAC with SHA384: " + encoding:encodeBase64(output));

    // HMAC generation for input value using SHA512 hasing algorithm, and printing HMAC value using Hex encoding.
    output = crypto:hmacSha512(inputArr, keyArr);
    io:println("Hex encoded HMAC with SHA512: " + encoding:encodeHex(output));


    // Hex encoded CRC32B checksum generation for input value.
    io:println("CRC32B for text: " + crypto:crc32b(input));

    // Hex encoded CRC32B checksum generation for XML data.
    xml xmlContent = xml `<foo>Hello Ballerina</foo>`;
    io:println("CRC32 for xml content: " + crypto:crc32b(xmlContent));


    // Obtaining reference to a RSA private key stored within a PKCS#12 or PFX format archive file.
    crypto:KeyStore keyStore = { path: "crypto/sampleKeystore.p12", password: "ballerina" };
    var privateKey = crypto:decodePrivateKey(keyStore = keyStore, keyAlias = "ballerina",
        keyPassword = "ballerina");

    if (privateKey is crypto:PrivateKey) {
        // Signing input value using RSA-MD5 signature algorithms, and printing the signature value using Hex encoding.
        output = check crypto:signRsaMd5(inputArr, privateKey);
        io:println("Hex encoded RSA-MD5 signature: " + encoding:encodeHex(output));

        // Signing input value using RSA-MD5 signature algorithms, and printing the signature value using Base64 encoding.
        output = check crypto:signRsaSha1(inputArr, privateKey);
        io:println("Base64 encoded RSA-SHA1 signature: " + encoding:encodeBase64(output));

        // Signing input value using RSA-MD5 signature algorithms, and printing the signature value using Hex encoding.
        output = check crypto:signRsaSha256(inputArr, privateKey);
        io:println("Hex encoded RSA-SHA256 signature: " + encoding:encodeHex(output));

        // Signing input value using RSA-MD5 signature algorithms, and printing the signature value using Base64 encoding.
        output = check crypto:signRsaSha384(inputArr, privateKey);
        io:println("Base64 encoded RSA-SHA384 signature: " + encoding:encodeBase64(output));

        // Signing input value using RSA-MD5 signature algorithms, and printing the signature value using Hex encoding.
        output = check crypto:signRsaSha512(inputArr, privateKey);
        io:println("Hex encoded RSA-SHA512 signature: " + encoding:encodeHex(output));
    } else {
        io:println("invalid private key");
    }
}
