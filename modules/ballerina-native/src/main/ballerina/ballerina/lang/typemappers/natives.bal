package ballerina.lang.typemappers;

import ballerina.doc;

@doc:Description { value:"Converts JSON to a string"}
@doc:Param { value:"j: JSON value to be converted" }
@doc:Return { value:"string: String representation of the given JSON" }
native typemapper jsonToString (json j) (string);

@doc:Description { value:"Converts XML to a string"}
@doc:Param { value:"value: XML value to be converted" }
@doc:Return { value:"string: String representation of the given XML" }
native typemapper xmlToString (xml x) (string);

@doc:Description { value:"Converts XML to JSON"}
@doc:Param { value:"x: XML value to be converted" }
@doc:Return { value:"json: JSON representation of the given XML" }
native typemapper xmlTojson (xml x) (json);

@doc:Description { value:"Converts a string to JSON"}
@doc:Param { value:"value: String value to be converted" }
@doc:Return { value:"json: JSON representation of the given string" }
native typemapper stringToJSON (string value) (json);

@doc:Description { value:"Converts a string to XML"}
@doc:Param { value:"value: String value to be converted" }
@doc:Return { value:"xml: XML representation of the given string" }
native typemapper stringToXML (string value) (xml);

@doc:Description { value:"Converts JSON to XML"}
@doc:Param { value:"j: JSON value to be converted" }
@doc:Return { value:"xml: XML representation of the given JSON" }
native typemapper jsonToxml (json j) (xml);

