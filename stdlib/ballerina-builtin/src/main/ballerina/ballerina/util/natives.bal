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
documentation {
Returns a random UUID string.
- #s The random string
}
public native function uuid () (string s);

@Description {value:"Encodes a base10 encoded string to base64."}
@Param {value:"s: string to be encoded"}
@Return {value:"the encoded string."}
documentation {
Encodes a base10 encoded string to base64.
- #s string to be encoded
- #encodedS the encoded string
}
public native function base64Encode (string s) (string encodedS);

@Description {value:"Decodes a base64 encoded string to base10."}
@Param {value:"s: string to be decoded"}
@Return {value:"the decoded string."}
documentation {
Decodes a base64 encoded string to base10.
- #s string to be decoded
- #decodedS the decoded string
}
public native function base64Decode (string s) (string decodedS);

@Description {value:"Encodes a base16 encoded string to base64 encoding."}
@Param {value:"s: string to be encoded"}
@Return {value:"the encoded string."}
documentation {
Encodes a base16 encoded string to base64 encoding.
- #baseString string to be encoded
- #encodedS the encoded string
}
public native function base16ToBase64Encode (string baseString) (string encodedS);

@Description {value:"Encodes a base64 encoded string to base16 encoding."}
@Param {value:"s: string to be encoded"}
@Return {value:"the encoded string."}
documentation {
Encodes a base64 encoded string to base16 encoding.
- #baseString string to be encoded
- #encodedS the encoded string
}
public native function base64ToBase16Encode (string baseString) (string encodedS);
