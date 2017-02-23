import ballerina.utils;
function testEncodeDecode(string s)(string) {
  string encoded;
  encoded = utils:base64encode(s);
  return utils:base64decode(encoded);
}

function testRandomString()(string) {
  return utils:getRandomString();
}

function testHmac(string base, string key, string algo)(string) {
  return utils:getHmac(base, key, algo);
}

function testHmacFromBase64(string base, string key, string algo)(string) {
  return utils:getHmacFromBase64(base, utils:base64encode(key), algo);
}

function testMessageDigest(string base, string algo)(string) {
  return utils:getHash(base, algo);
}

function testBase64ToBase16Encode(string base)(string) {
  return utils:base64ToBase16Encode(utils:base64encode(base));
}
