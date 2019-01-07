import ballerina/io;

function testBase64EncodeByteChannel(io:ReadableByteChannel contentToBeEncoded) returns io:ReadableByteChannel|error {
    return contentToBeEncoded.base64Encode();
}

function testBase64DecodeByteChannel(io:ReadableByteChannel contentToBeDecoded) returns io:ReadableByteChannel|error {
    return contentToBeDecoded.base64Decode();
}
