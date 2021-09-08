import ballerina/module1;

xmlns "http://ballerina.com/ns0" as ns0;

function testFunction() {
    xmlns "http://ballerina.com/ns1" as ns1;
    xml x2 = xml `<foo n>hello</foo>`;
    
    x2.n
}


