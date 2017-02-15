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

function testHmacFromBase64(string base, string key, string algo)(string) {
  return util:getHmacFromBase64(base, util:base64encode(key), algo);
}

function testGetHash(string base, string algo)(string) {
  return util:getHash(base, algo);
}

function testBase64ToBase16Encode(string base)(string) {
  return util:base64ToBase16Encode(util:base64encode(base));
}