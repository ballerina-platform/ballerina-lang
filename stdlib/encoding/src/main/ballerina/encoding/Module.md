## Module overview

This module provides the necessary utilities that are required to encode and decode content using different encoding mechanisms and algorithms.

#### Encoding byte array to hex string

The `encodeHex` function encodes provided byte array to an hex `string`.

#### Decoding hex string to byte array

The `decodeHex` function decodes a hex encoded `string` to a byte array.

#### Encoding byte array to base64 string

The `encodeBase64` function encodes provided byte array to an base64 `string`.

#### Decoding base64 string to byte array

The `decodeBase64` function decodes a base64 encoded `string` to a byte array.

## Samples

```ballerina
import ballerina/io;
import ballerina/encoding;

public function main() {
     string charEncoding = "UTF-8";

     // The string content to be hashed.
     string text = "Hello Ballerina";
     byte[] inputByteArr = input.toByteArray(charEncoding);

     string output = encode:encodeHex(inputByteArr)
     io:println("Hex encoded string : " + output);

     // Hex encoded string, decoded back into a byte array
     inputByteArr = encode:decodeHex(output)

     output = encode:encodeBase64(inputByteArr)
     io:println("Base64 encoded string : " + output);

     // Base64 encoded string, decoded back into a byte array
     inputByteArr = encode:decodeBase64(output)
}
```
