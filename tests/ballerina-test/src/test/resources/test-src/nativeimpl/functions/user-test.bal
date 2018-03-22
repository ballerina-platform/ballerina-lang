import ballerina/user;
import ballerina/util;

function testGetHome () returns (string) {
    return user:getHome();
}

function testGetName () returns (string) {
    return user:getName();
}

function testGetLanguage () returns (string) {
    return user:getLanguage();
}

function testGetCountry () returns (string) {
    return user:getCountry();
}

function testGetLocale () returns (string, string) {
    util:Locale locale = user:getLocale();
    return (locale.language, locale.countryCode);
}
