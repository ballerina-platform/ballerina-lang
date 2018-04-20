## Package overview

This packages provides the necessary utilities that are required to hash content using different hashing mechanisms and algorithms. 

#### Hashing string content
The `getHash` function uses an algorithm to hash content of type `string`. 

#### Calculating HMAC 
The `getHmac` function uses an algorithm to calculate HMAC of a given string, using a given key. 

### Calculating CRC

The `getCRC32` function can be used to calculate CRC on input types such as string, xml , json, and blobs.

## Samples

### Sample hash function

```ballerina
import ballerina/io;
import ballerina/security.crypto;

function main(string[] args) {
     // The string content to be hashed.
     string input = "Hello Ballerina";
     // The key used for hashing when required.
     string key = "somesecret";
     // The XML content to be hashed.
     xml xmlContent = xml `<foo>Hello Ballerina</foo>`;

     io:println("HMAC with MD5: " + crypto:getHmac(input, key, crypto:MD5));
     io:println("HMAC with SHA1: " + crypto:getHmac(input, key, crypto:SHA1));
     io:println("HMAC with SHA256: " + crypto:getHmac(input, key, crypto:SHA256));

     io:println("Hash with MD5: " + crypto:getHash(input, crypto:MD5));
     io:println("Hash with SHA1: " + crypto:getHash(input, crypto:SHA1));
     io:println("Hash with SHA256: " + crypto:getHash(input, crypto:SHA256));

     io:println("Hash with CRC32 for text: " + crypto:getCRC32(input));
     io:println("Hash with CRC32 for xml content: " + crypto:getCRC32(xmlContent));
}

```

## Package contents

