import ballerina.io;

function testExternalEntityInjectionNegative () {
    var xmlDoc, x = <xml>("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                          "<!DOCTYPE foo [" +
                          "<!ELEMENT foo ANY >" +
                          "<!ENTITY xxe SYSTEM \"file:///\" >]>" +
                          "<foo>&xxe;</foo>");
    io:println(xmlDoc);
}
