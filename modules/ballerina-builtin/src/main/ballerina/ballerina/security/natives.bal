package ballerina.security;

@Description {value:"Returns a hash of a given string using the SHA-256 algorithm "}
@Param {value:"baseString: The string to be hashed"}
@Param {value:"algorithm: The hashing algorithm to be used"}
@Return {value:"The hashed string"}
public native function getHash (string baseString, string algorithm) (string);

@Description {value:"Returns a hash of a given string using the key provided "}
@Param {value:"baseString: The string to be hashed"}
@Param {value:"keyString: The key string "}
@Param {value:"algorithm: The hashing algorithm to be used"}
@Return {value:"The hashed string"}
public native function getHmac (string baseString, string keyString, string algorithm) (string);
