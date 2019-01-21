import ballerina/crypto;

function testHashWithCRC32b(byte[] input) returns string {
    return crypto:crc32b(input);
}

function testHashWithMD5(byte[] input) returns byte[] {
    return crypto:hashMd5(input);
}

function testHashWithSHA1(byte[] input) returns byte[] {
    return crypto:hashSha1(input);
}

function testHashWithSHA256(byte[] input) returns byte[] {
    return crypto:hashSha256(input);
}

function testHashWithSHA384(byte[] input) returns byte[] {
    return crypto:hashSha384(input);
}

function testHashWithSHA512(byte[] input) returns byte[] {
    return crypto:hashSha512(input);
}

function testHmacWithMD5(byte[] input, byte[] key) returns byte[] {
    return crypto:hmacMd5(input, key);
}

function testHmacWithSHA1(byte[] input, byte[] key) returns byte[] {
    return crypto:hmacSha1(input, key);
}

function testHmacWithSHA256(byte[] input, byte[] key) returns byte[] {
    return crypto:hmacSha256(input, key);
}

function testHmacWithSHA384(byte[] input, byte[] key) returns byte[] {
    return crypto:hmacSha384(input, key);
}

function testHmacWithSHA512(byte[] input, byte[] key) returns byte[] {
    return crypto:hmacSha512(input, key);
}

function testSignRsaSha1(byte[] input, byte[] key) returns byte[] {
    crypto:PrivateKey pk = crypto:decodePrivateKey(keyContent = key);
    return crypto:signRsaSha1(input, pk);
}

function testSignRsaSha256(byte[] input, byte[] key) returns byte[] {
    crypto:PrivateKey pk = crypto:decodePrivateKey(keyContent = key);
    return crypto:signRsaSha256(input, pk);
}

function testSignRsaSha384(byte[] input, byte[] key) returns byte[] {
    crypto:PrivateKey pk = crypto:decodePrivateKey(keyContent = key);
    return crypto:signRsaSha384(input, pk);
}

function testSignRsaSha512(byte[] input, byte[] key) returns byte[] {
    crypto:PrivateKey pk = crypto:decodePrivateKey(keyContent = key);
    return crypto:signRsaSha512(input, pk);
}

function testSignRsaMd5(byte[] input, byte[] key) returns byte[] {
    crypto:PrivateKey pk = crypto:decodePrivateKey(keyContent = key);
    return crypto:signRsaMd5(input, pk);
}
