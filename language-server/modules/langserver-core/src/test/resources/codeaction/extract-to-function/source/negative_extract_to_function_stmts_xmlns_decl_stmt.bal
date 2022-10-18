function testFunction() {
    xmlns "http://example.com" as eg;
    xml x = xml `<eg:doc>Hello</eg:doc>`;

    doSomething(x);
}

function doSomething(xml a) {

}
