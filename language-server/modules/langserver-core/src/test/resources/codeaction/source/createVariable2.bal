import ballerina/crypto;

function getCacheId(string str) {
    crypto:hashMd5(str.toBytes());
}
