
//----------------------------XML Stamp -------------------------------------------------------------

function stampXMLToXML() returns xml {

    xml xmlValue = xml `<book>The Lost World</book>`;

    xml returnValue = xml.stamp(xmlValue);
    return returnValue;
}

function stampXMLToAnydata() returns anydata {

    xml xmlValue = xml `<book>The Lost World</book>`;

    anydata anydataValue = anydata.stamp(xmlValue);
    return anydataValue;
}
