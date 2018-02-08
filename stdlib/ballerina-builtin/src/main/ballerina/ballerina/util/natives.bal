package ballerina.util;

import ballerina.math;

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

@Description {value:"Convert hex Numbers into decimal Numbers"}
@Param {value:"hexValue: string of the hex number to be converted into decimal"}
@Return {value:"integer in decimal"}
public function hexToDecimal (string hexValue) (int, TypeConversionError) {
    map hexDecimalMap = {"0":0, "1":1, "2":2, "3":3, "4":4, "5":5, "6":6, "7":7, "8":8, "9":9, "a":10, "b":11, "c":12,
                            "d":13, "e":14, "f":15};
    int decimalValue = 0;
    // Hex numbers must start with "0x".
    if (!hexValue.subString(0, 2).equalsIgnoreCase("0x") || lengthof hexValue == 2) {
        TypeConversionError err = {msg:"Invalid hex number: Hex numbers must start with 0x"};
        return -1, err;
    }
    // Convert hex value part to decimal
    foreach i in 2..(lengthof hexValue - 1) {
        var decimalEquivalent, castError = (int)hexDecimalMap[hexValue.subString(i, i + 1)];
        if (castError != null) {
            TypeConversionError err = {msg:"Invalid hex number: Invalid character"};
            return -1, err;
        }
        decimalValue = decimalValue + decimalEquivalent * (<int>math:pow(16.0, <float>(lengthof hexValue - i - 1)));
    }
    return decimalValue, null;
}

@Description {value:"Convert decimal Numbers into hex Numbers"}
@Param {value:"decimalValue: integer of the decimal number to beconverted into hex"}
@Return {value:"string of hex number"}
public function decimalToHex (int decimalValue) (string) {
    string hexValue = "";
    if (decimalValue == 0) {
        return "0x0";
    }
    while (decimalValue != 0) {
        int reminder = decimalValue - (decimalValue / 16) * 16;
        decimalValue = decimalValue / 16;
        if (reminder < 10) {
            hexValue = <string>reminder + hexValue;
        } else if (reminder == 10) {
            hexValue = "a" + hexValue;
        } else if (reminder == 11) {
            hexValue = "b" + hexValue;
        } else if (reminder == 12) {
            hexValue = "c" + hexValue;
        } else if (reminder == 13) {
            hexValue = "d" + hexValue;
        } else if (reminder == 14) {
            hexValue = "e" + hexValue;
        } else if (reminder == 15) {
            hexValue = "f" + hexValue;
        }
    }
    return "0x" + hexValue;
}
