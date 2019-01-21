import ballerina/crypto;

function testParsingPrivateKeyFromBytes(byte[] input, string? password = ()) returns crypto:PrivateKey {
    return crypto:decodePrivateKey(keyContent = input, keyPassword = password);
}

function testParsingPrivateKeyFromFile(string input, string? password = ()) returns crypto:PrivateKey {
    return crypto:decodePrivateKey(keyFile = input, keyPassword = password);
}