## Package Overview
This package provides utility functions, which perform common functions and are often reused. Functions such as encoding, decoding, generating random numbers, and parsing the JSON are included in this package. 
### Encode and decode operations
This package provides functions to encode `string`, `blob`, and `io:ByteChannel` values to the base64 format and functions to decode them back to their original format. 

Data encoding is the process of converting data from the original format to a different format using a predefined process. The encoded data improves the efficiency of transmission and storage. The rfc4648 data encoding specification defines a set of encoding standards and base64 is one of them. `string` and `blob` are primitive types in Ballerina and `io:ByteChannel` is a stream of bytes that can be used to perform read-write operations. 
### Conversion between encoding formats
The `base64ToBase16Encode()` function converts the base64 encoded `string` to a base16 format while the `base16ToBase64Encode()` function converts the base16 encoded string to base64. These functions are useful when communicating between two systems that use two different encoding formats. 
### Parse JSON strings
JavaScript Object Notation (JSON) is a widely recognized format that is used to exchange and store data in web applications. This package provides the function to parse a JSON `string` and create a `json` object. `json` is a built-in type, which can represent any JSON object, in Ballerina. 
 ## Generate Universally Unique Identifier (UUID)
UUIDs are 128-bit numbers that are widely used to uniquely identify the data or processes in computer systems. The `uuid()` function generates a random UUID string.
## Samples
### Encode a `string` and convert the encoding format
The sample given below encodes a `string` to the base64 format and then converts it to a base16 string.

```ballerina
function main(string... args) {
   string contentToBeEncoded = "This is a ballerina sample";

   // Encode the ‘contentToBeEncoded’ string.
   var result = contentToBeEncoded.base64Encode();
   // The base64EncodeString() function can return an error or encoded string.
   match result {
       error err => io:println(err);
       // If a base64 encoded string is returned, convert it to a base16 encoded string.
       string encodedString => {
           string base16encoded = encodedString.base64ToBase16Encode();
           io:println(base16encoded);
       }
   }
}

```
### Decode the encoded content in a file
The sample given below encodes a file and then decodes it back again. In Ballerina, the `openFile()` function creates a streaming channel to a local file. Channels provide read or write access to different resources.

```ballerina
function main(string... args) {
   string path = "/home/user/encode.txt";

   // Open the file that is denoted by the path in read access mode.
   io:ByteChannel channel = io:openFile(path, io:MODE_R);
   // Base64 encode the channel.
   var result = channel.base64Encode();
   var channelCloseResult = channel.close();

   match result {
       // If the result is an error, print the error
       error err => io:println(err);
       // If the result is an encoded channel, decode the channel.
       io:ByteChannel encodedChannel => {
           var decodeResult = encodedChannel.base64Decode();
           var closeResult = encodedChannel.close();

           match decodeResult {
               // If results is an error, print the error.
               eroor err => io:println(err);
               // If the decoded channel is returned, read the content.
               io:ByteChannel decodedChannel => {
                   var readResult = decodedChannel.read(36);
                   closeResult = decodedChannel.close();
                   match readResult {
                       // If the ‘readResult’ is not an error, read the byteContent and convert it to the content format.
                       (blob, int) byteContent => {
                           var (blobContent, numberOfBytes) = byteContent;

                           // Convert the ‘blobContent’ to a string
                           string encodedString = blobContent.toString("UTF-8");
                           io:println(encodedString);

                           // Decode the encoded string.
                           var decodeStringResult = encodedString.base64Decode();
                           match decodeStringResult {
                               string decodedString => io:println(decodeStringResult);
                               error err => io:println(err);
                           }
                       }
                       io:IOError err => {
                           io:println(err);
                       }
                   }
               }
           }

       }
   }
}
```
### Parse a JSON `string`
The sample given below parses a JSON `string` and gives the equivalent JSON object. 
```ballerina
function main(string... args) {
   string jsonString = "{'language' : 'ballerina'}";
   match util:parseJson(jsonString) {
       // If the parsing succeeds, a JSON Object is returned.
       json jsonObject => {
           // Retrieve a value from JSON object.
           io:println(jsonObject["language"]);
       }
       // If an error is returned, print the error.
       error err => io:println(err);
   }
}
```
