package ballerina.util;

@Description { value: "Locale struct represents specific geographical, political, or cultural region."}
@Field {value:"language: The language field for Locale"}
@Field {value:"countryCode: The countryCode field for Locale"}
public struct Locale {
    string language;
    string countryCode;
}

@Description { value:"Returns a hash of a given string using the SHA-256 algorithm "}
@Param { value:"baseString: The string to be hashed" }
@Param { value:"algorithm: The hashing algorithm to be used" }
@Return { value:"string: The hashed string" }
public native function getHash (string baseString, string algorithm) (string);

@Description { value:"Decodes a Base64 encoded string to a new string"}
@Param { value:"s: The input string to be decoded" }
@Return { value:"string: The decoded string" }
public native function base64decode (string s) (string);

@Description { value:"Returns a random UUID string"}
@Return { value:"string: The random string" }
public native function uuid () (string);

@Description { value:"Encodes a Base64 encoded string into a Base16 encoded string."}
@Param { value:"baseString: The input string to be encoded" }
@Return { value:"string: The Base16 encoded string" }
public native function base64ToBase16Encode (string baseString) (string);

@Description { value:"Encodes the specified string into a string using the Base64 scheme"}
@Param { value:"s: The input string to be encoded" }
@Return { value:"string: The encoded string" }
public native function base64encode (string s) (string);

@Description { value:"Returns a hash of a given string in Base64 format using the key provided "}
@Param { value:"baseString: The string to be hashed" }
@Param { value:"keyString: The key string " }
@Param { value:"algorithm: The hashing algorithm to be used" }
@Return { value:"string: The hashed string" }
public native function getHmacFromBase64 (string baseString, string keyString, string algorithm) (string);

@Description { value:"Returns a hash of a given string using the key provided "}
@Param { value:"baseString: The string to be hashed" }
@Param { value:"keyString: The key string " }
@Param { value:"algorithm: The hashing algorithm to be used" }
@Return { value:"string: The hashed string" }
public native function getHmac (string baseString, string keyString, string algorithm) (string);
