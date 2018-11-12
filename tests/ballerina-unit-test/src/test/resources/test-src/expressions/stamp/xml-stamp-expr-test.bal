
//----------------------------XML Stamp -------------------------------------------------------------
function stampXMLToAny() returns any {

    xml xmlValue = xml `<book>The Lost World</book>`;

    any anyValue = xmlValue.stamp(any);
    return anyValue;
}

function stampXMLToXML() returns xml {

    xml xmlValue = xml `<book>The Lost World</book>`;

    xml returnValue = xmlValue.stamp(xml);
    return returnValue;
}

function stampXMLToAnydata() returns anydata {

    xml xmlValue = xml `<book>The Lost World</book>`;

    anydata anydataValue = xmlValue.stamp(anydata);
    return anydataValue;
}
