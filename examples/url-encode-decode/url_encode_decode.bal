import ballerina/encoding;
import ballerina/io;
import ballerina/lang.'string as str;

public function main() returns error? {
    string encodingValue = "abc123!?$*&()'-=@~";
    // Encoding a URL into a Base64-encoded string.
    string urlEncodedValue = encoding:encodeBase64Url(encodingValue.toBytes());
    io:println("Base64 URL encoded value: " + urlEncodedValue);

    string decodingValue = "YWJjMTIzIT8kKiYoKSctPUB-";
    // Decoding a Base64 URL encoded string into a byte array.
    byte[] urlDecodedValue = check encoding:decodeBase64Url(decodingValue);
    io:println("Base64 URL decoded value: " + check str:fromBytes(urlDecodedValue));

    string encodingUriComp = "data=value";
    // Encoding a URI component into a string.
    string encodedUriComponent = check encoding:encodeUriComponent(encodingUriComp, "UTF-8");
    io:println("URI encoded value: " + encodedUriComponent);

    string data = "data%3Dvalue";
    // Decoding an encoded URI component into a string.
    string decodedUriComponent = check encoding:decodeUriComponent(data, "UTF-8");
    io:println("URI decoded value: " + decodedUriComponent);
}
