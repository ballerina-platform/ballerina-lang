package ballerina.util;

import ballerina.doc;

@doc:Description { value: "Locale struct represents specific geographical, political, or cultural region."}
@doc:Field {value:"language: The language field for Locale"}
@doc:Field {value:"countryCode: The countryCode field for Locale"}
public struct Locale {
    string language;
    string countryCode;
}

@doc:Description { value:"Returns a hash of a given string using the SHA-256 algorithm "}
@doc:Param { value:"baseString: The string to be hashed" }
@doc:Param { value:"algorithm: The hashing algorithm to be used" }
@doc:Return { value:"string: The hashed string" }
public native function getHash (string baseString, string algorithm) (string);

@doc:Description { value:"Decodes a Base64 encoded string to a new string"}
@doc:Param { value:"s: The input string to be decoded" }
@doc:Return { value:"string: The decoded string" }
public native function base64decode (string s) (string);

@doc:Description { value:"Returns a random UUID string"}
@doc:Return { value:"string: The random string" }
public native function uuid () (string);

@doc:Description { value:"Encodes a Base64 encoded string into a Base16 encoded string."}
@doc:Param { value:"baseString: The input string to be encoded" }
@doc:Return { value:"string: The Base16 encoded string" }
public native function base64ToBase16Encode (string baseString) (string);

@doc:Description { value:"Encodes the specified string into a string using the Base64 scheme"}
@doc:Param { value:"s: The input string to be encoded" }
@doc:Return { value:"string: The encoded string" }
public native function base64encode (string s) (string);

@doc:Description { value:"Returns a hash of a given string in Base64 format using the key provided "}
@doc:Param { value:"baseString: The string to be hashed" }
@doc:Param { value:"keyString: The key string " }
@doc:Param { value:"algorithm: The hashing algorithm to be used" }
@doc:Return { value:"string: The hashed string" }
public native function getHmacFromBase64 (string baseString, string keyString, string algorithm) (string);

@doc:Description { value:"Returns a hash of a given string using the key provided "}
@doc:Param { value:"baseString: The string to be hashed" }
@doc:Param { value:"keyString: The key string " }
@doc:Param { value:"algorithm: The hashing algorithm to be used" }
@doc:Return { value:"string: The hashed string" }
public native function getHmac (string baseString, string keyString, string algorithm) (string);
