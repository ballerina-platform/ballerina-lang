import ballerina.lang.system;
import ballerina.util;

function main (string[] args) {

    string encoded;
    string s;

    s = "https://example.com/test/index.html#title1";

    // parameter is the string to be encoded.
    //returns base64 encoded string.
    encoded = util:base64encode(s);
    system:println(encoded);

    //parameter is base64 encoded string.
    //returns decoded string.
    system:println(util:base64decode(encoded));

    //returns a random string
    system:println(util:getRandomString());

    //returns base 64 encoded string.
    //first parameter is the base string to be hashed.
    //second parameter is the key string to hash the bse string with.
    //third parameter is the algorithm.
    //supported algorithms are, SHA1, SHA256, MD5.

    system:println(util:getHmac("Ballerina HMAC test", "abcdefghijk", "SHA256"));

}