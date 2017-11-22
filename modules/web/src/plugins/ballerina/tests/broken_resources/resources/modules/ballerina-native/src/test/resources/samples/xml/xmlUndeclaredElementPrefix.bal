function xmlUndeclaredElementPrefix() (xml) {
   
    xml x = xml `<ns1:foo>hello</ns1:foo>`;
    return x;
}
