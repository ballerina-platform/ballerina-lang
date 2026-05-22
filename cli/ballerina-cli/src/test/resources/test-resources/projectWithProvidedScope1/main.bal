public function main() returns error? {
    // commons-codec DigestUtils: hash strings
    string msg = "hello ballerina";
    systemOutPrintln("MD5    : " + md5Hex(msg));
    systemOutPrintln("SHA-256: " + sha256Hex(msg));
    systemOutPrintln("SHA-512: " + sha512Hex(msg));
}
