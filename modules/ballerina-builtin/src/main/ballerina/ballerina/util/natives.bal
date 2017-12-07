package ballerina.util;

@Description {value:"Locale struct represents specific geographical, political, or cultural region."}
@Field {value:"language: The language field for Locale"}
@Field {value:"countryCode: The countryCode field for Locale"}
public struct Locale {
    string language;
    string countryCode;
}

@Description {value:"Returns a random UUID string"}
@Return {value:"The random string"}
public native function uuid () (string);

@Description {value:"Encodes a base10 encoded string to base64."}
@Param {value:"s: string to be encoded"}
@Return {value:"the encoded string."}
public native function base64Encode (string s) (string);

@Description {value:"Decodes a base64 encoded string to base10."}
@Param {value:"s: string to be decoded"}
@Return {value:"the decoded string."}
public native function base64Decode (string s) (string);

@Description {value:"Encodes a base16 encoded string to base64 encoding."}
@Param {value:"s: string to be encoded"}
@Return {value:"the encoded string."}
public native function base16ToBase64Encode (string baseString) (string);

@Description {value:"Encodes a base64 encoded string to base16 encoding."}
@Param {value:"s: string to be encoded"}
@Return {value:"the encoded string."}
public native function base64ToBase16Encode (string baseString) (string);

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

@doc:Description { value:"Returns validity of the jwt "}
@doc:Param { value:"jwToken: JW Token" }
@doc:Param { value:"publicKey: public key " }
@doc:Return { value:"boolean: validity" }
native function getShaWithRsa (string jwToken, string publicKey) (boolean);
