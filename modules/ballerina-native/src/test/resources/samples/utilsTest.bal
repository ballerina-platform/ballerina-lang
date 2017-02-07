import ballerina.util;
function testEncodeDecode(string s)(string) {
  string encoded;
  encoded = util:base64encode(s);
  return util:base64decode(encoded);
}

function testRandomString()(string) {
  return util:getRandomString();
}

function testHmac(string base, string key, string algo)(string) {
  return util:getHmac(base, key, algo);
}