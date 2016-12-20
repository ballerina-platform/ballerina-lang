function getString(xml msg) (string){
    string xPath;
    xPath = "/persons/person/name/text()";
    return ballerina.lang.xml:getString( msg , xPath );
}

function getXML(xml msg) (string){
    string xPath;
    xPath = "/persons/person";
    return ballerina.lang.xml:getXml( msg , xPath );
}

function remove(xml msg) (string){
    string xPath;
    xPath = "/persons/person/address";
    ballerina.lang.xml:remove( msg , xPath );
    return msg;
}