
function testEncodeDecode(string s)(string) {
  string encoded;
  encoded = ballerina.util:base64encode(s);
  return ballerina.util:base64decode(encoded);
}

function testRandomString()(string) {
  return ballerina.util:getRandomString();
}

function testHmac(string base, string key, string algo)(string) {
  return ballerina.util:getHmac(base, key, algo);
}