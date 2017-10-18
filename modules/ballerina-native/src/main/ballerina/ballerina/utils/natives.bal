package ballerina.utils;

import ballerina.doc;

@doc:Description {value:"Represents the user's locale."}
@doc:Field {value:"language: language code"}
@doc:Field {value:"countryCode: country code"}
struct Locale {
    string language;
    string countryCode;
}
