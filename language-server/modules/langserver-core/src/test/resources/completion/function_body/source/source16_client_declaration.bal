client "https://postman-echo1.com/get?name=projectapiclientplugin" as testPrefixMod;

function testFunction() {
    client "https://postman-echo2.com/get?name=projectapiclientplugin" as testPrefix;
    
}
