import ballerina.user;

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
