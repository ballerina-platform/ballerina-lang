package ballerina.security.crypto;

@Description {value:"Returns a hash of a given string using the specified algorithm (supported algorithms: SHA1, SHA256, MD5)."}
@Param {value:"baseString: The string to be hashed"}
@Param {value:"algorithm: The hashing algorithm to be used"}
@Return {value:"The hashed string"}
public native function getHash (string baseString, string algorithm) (string);

@Description {value:"Returns an HMAC value for the provided base string (supported algorithms: SHA1, SHA256, MD5)."}
@Param {value:"baseString: The string to be hashed"}
@Param {value:"keyString: The key string "}
@Param {value:"algorithm: The hashing algorithm to be used"}
@Return {value:"The hashed string"}
public native function getHmac (string baseString, string keyString, string algorithm) (string);
