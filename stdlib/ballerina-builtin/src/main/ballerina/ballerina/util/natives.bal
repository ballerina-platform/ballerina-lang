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
@Param {value:"s: String to be encoded"}
@Return {value:"The encoded string."}
public native function base64Encode (string s) (string);

@Description {value:"Decodes a base64 encoded string to base10."}
@Param {value:"s: String to be decoded"}
@Return {value:"The decoded string."}
public native function base64Decode (string s) (string);

@Description {value:"Encodes a base16 encoded string to base64 encoding."}
@Param {value:"s: String to be encoded"}
@Return {value:"The encoded string."}
public native function base16ToBase64Encode (string baseString) (string);

@Description {value:"Encodes a base64 encoded string to base16 encoding."}
@Param {value:"s: String to be encoded"}
@Return {value:"The encoded string."}
public native function base64ToBase16Encode (string baseString) (string);

@Description {value:"Convert hex numbers into decimal numbers"}
@Param {value:"hexValue: String of the hex number to be converted into decimal"}
@Return {value:"Integer in decimal"}
public native function hexToDecimal (string hexValue) (int);

@Description {value:"Convert decimal numbers into hex numbers"}
@Param {value:"decimalValue: Integer of the decimal number to be converted into hex"}
@Return {value:"String of hex number"}
public native function decimalToHex (int decimalValue) (string);
