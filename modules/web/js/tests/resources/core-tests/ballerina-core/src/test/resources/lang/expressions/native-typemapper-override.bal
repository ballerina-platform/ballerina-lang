function nativeMapperOverrideTest()(xml) {
    json j = {"name":"chanaka"};
    xml x = (xml)j;
    return x;
}

typemapper jsonToXml(json j)(xml) {
    xml x = `<test>hello</test>`;
    return x;
}
