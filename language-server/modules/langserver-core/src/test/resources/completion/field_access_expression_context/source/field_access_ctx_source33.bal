import ballerina/module1;

xmlns "http://ballerina.com/aa" as ns0;

function testFunction() {
    xmlns "http://ballerina.com/aa" as ns1;
    xml x2 = xml `<foo>hello</foo>`;
    var result = (x2.<ns0>).t
}
