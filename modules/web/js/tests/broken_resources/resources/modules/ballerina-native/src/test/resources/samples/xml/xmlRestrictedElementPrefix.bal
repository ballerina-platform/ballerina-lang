function testRestrictedElementPrefix() (xml) {
   
    xml x = xml `<xmlns:foo>hello</xmlns:foo>`;
    return x;
}
