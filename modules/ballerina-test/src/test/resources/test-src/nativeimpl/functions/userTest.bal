import ballerina.user;
import ballerina.utils;

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
    utils:Locale locale = user:getLocale();
    return locale.language, locale.countryCode;
}
