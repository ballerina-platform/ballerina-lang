## Module overview

This module provides the necessary utilities that are required to encode and decode content using different encoding mechanisms and algorithms.

### Samples

#### Encoding a URL to Base64 encoded string

The `encodeBase64Url` function encodes provided URL to an base64 `string`.

```ballerina
import ballerina/encoding;
import ballerina/io;

public function main() {
    string data = "abc123!?$*&()'-=@~";
    string urlEncodedValue = encoding:encodeBase64Url(data.toBytes());
    io:println("Base64 URL encode value: " + urlEncodedValue);
}
```

#### Decoding a Base64 URL encoded string into byte array.

The `decodeBase64Url` function decodes a base64 encoded `string` to a byte array.

```ballerina
import ballerina/encoding;
import ballerina/io;
import ballerina/lang.'string as str;

public function main() returns error? {
    string data = "YWJjMTIzIT8kKiYoKSctPUB-";
    byte[] urlDecodedValue = check encoding:decodeBase64Url(data);
    io:println("Base64 URL decode value: " + check str:fromBytes(urlDecodedValue));
}
```

#### Encoding a URI component into a string

The `encodeUriComponent` function can be used to encode a URI into a string using a provided charset.

```ballerina
import ballerina/encoding;
import ballerina/io;

public function main() returns error? {
    string data = "data=value";
    string encodedUriComponent = check encoding:encodeUriComponent(data, "UTF-8");
    io:println("URI encoded value: " + encodedUriComponent);
}
```

#### Decoding a encoded URI component into string

The `decodeUriComponent` function can be used to decode a URI into a string using a provided charset.

```ballerina
import ballerina/encoding;
import ballerina/io;

public function main() returns error? {
    string data = "data%3Dvalue";
    string decodedUriComponent = check encoding:decodeUriComponent(data, "UTF-8");
    io:println("URI decoded value: " + decodedUriComponent);
}
```
