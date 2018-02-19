package ballerina.security.crypto;

@Description {value:"The algorithms which can be used in crypto functions."}
@Field {value:"MD5: MD5 algorithm"}
@Field {value:"SHA1: SHA1 algorithm"}
@Field {value:"SHA256: SHA256 algorithm"}
public enum Algorithm {
    SHA1, SHA256, MD5
}

@Description {value:"Returns the hash of the given string using the specified algorithm."}
@Param {value:"baseString: The string to be hashed"}
@Param {value:"algorithm: The hashing algorithm to be used"}
@Return {value:"The hashed string"}
documentation {
Returns the hash of the given string using the specified algorithm.
- #baseString The string to be hashed
- #algorithm The hashing algorithm to be used
- #s The hashed string
}
public native function getHash (string baseString, Algorithm algorithm) (string s);

@Description {value:"Returns the HMAC value of the provided base string."}
@Param {value:"baseString: The string to be hashed"}
@Param {value:"keyString: The key string "}
@Param {value:"algorithm: The hashing algorithm to be used"}
@Return {value:"The hashed string"}
documentation {
Returns the HMAC value of the provided base string.
- #baseString The string to be hashed
- #keyString The key string
- #algorithm The hashing algorithm to be used
- #s The hashed string
}
public native function getHmac (string baseString, string keyString, Algorithm algorithm) (string s);
