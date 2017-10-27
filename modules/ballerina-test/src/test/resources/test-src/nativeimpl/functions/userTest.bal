import ballerina.user;
import ballerina.util;

function testGetHome () (string) {
    return user:getHome();
}

function testGetName () (string) {
    return user:getName();
}

function testGetLanguage () (string) {
    return user:getLanguage();
}

function testGetCountry () (string) {
    return user:getCountry();
}

function testGetLocale () (string, string) {
    util:Locale locale = user:getLocale();
    return locale.language, locale.countryCode;
}
