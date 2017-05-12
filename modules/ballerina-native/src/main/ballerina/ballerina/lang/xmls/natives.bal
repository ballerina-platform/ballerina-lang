package ballerina.lang.xmls;

import ballerina.doc;

@doc:Description { value:"Sets the string value of an element that matches the given XPath. If the XPath matches an existing element, that element's value will be updated. If the XPath does not match an existing element, this operation will have no effect. Namespaces are supported."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"xPath: An XPath" }
@doc:Param { value:"value: A string value" }
@doc:Param { value:"namespaces: A map object consisting of namespaces" }
native function set (xml x, string xPath, string value, map namespaces);

@doc:Description { value:"Evaluates the XPath on an XML object and returns the matching XML object."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"xPath: An XPath" }
@doc:Return { value:"xml: Matching XML object" }
native function getXml (xml x, string xPath) (xml);

@doc:Description { value:"Sets the XML value of an element that matches the given XPath. If the XPath matches an existing element, that element's value will be updated. If the XPath does not match an existing element, this operation will have no effect. Namespaces are supported."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"xPath: An XPath" }
@doc:Param { value:"value: An XML value" }
@doc:Param { value:"namespaces: A map object consisting of namespaces" }
native function set (xml x, string xPath, xml value, map namespaces);

@doc:Description { value:"Removes the element(s) that match the given XPath. Namespaces are supported."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"xPath: An XPath" }
@doc:Param { value:"namespaces: A map object consisting of namespaces" }
native function remove (xml x, string xPath, map namespaces);

@doc:Description { value:"Adds an XML element to the location that matches the given XPath. If the XPath matches the text value of an existing element, or if the XPath does not match an existing element, this operation will have no effect."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"xPath: An XPath" }
@doc:Param { value:"value: An XML value" }
native function addElement (xml x, string xPath, xml value);

@doc:Description { value:"Sets the XML value of an element that matches the given XPath. If the XPath matches an existing element, that element's value will be updated. If the XPath does not match an existing element, this operation will have no effect."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"xPath: An XPath" }
@doc:Param { value:"value: An XML value" }
native function set (xml x, string xPath, xml value);

@doc:Description { value:"Removes the element(s) that match the given XPath."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"xPath: An XPath" }
native function remove (xml x, string xPath);

@doc:Description { value:"Sets the string value of an element that matches the given XPath. If the XPath matches an existing element, that element's value will be updated. If the XPath does not match an existing element, this operation will have no effect."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"xPath: An XPath" }
@doc:Param { value:"nameSpaces: A string value" }
native function set (xml x, string xPath, string value);

@doc:Description { value:"Adds an attribute to the XML element that matches the given XPath. If the XPath matches the text value of an existing element, or if the XPath does not match an existing element, this operation will have no effect. Namespaces are supported."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"xPath: An XPath" }
@doc:Param { value:"name: Name of the attribute to be added" }
@doc:Param { value:"value: Attribute value" }
@doc:Param { value:"namespaces: A map object consisting of namespaces" }
native function addAttribute (xml x, string xPath, string name, string value, map namespaces);

@doc:Description { value:"Evaluates the XPath on an XML object and returns the matching string value."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"xPath: An XPath" }
@doc:Return { value:"string: Matching string value" }
native function getString (xml x, string xPath) (string);

@doc:Description { value:"Evaluates the XPath on an XML object and returns the matching XML object. Namespaces are supported"}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"xPath: An XPath" }
@doc:Param { value:"namespaces: A map object consisting of namespaces" }
@doc:Return { value:"xml: Matching XML object" }
native function getXml (xml x, string xPath, map namespaces) (xml);

@doc:Description { value:"Adds an XML element to the location that matches the given XPath. If the XPath matches the text value of an existing element, or if the XPath does not match an existing element, this operation will have no effect. Namespaces are supported."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"xPath: An XPath" }
@doc:Param { value:"value: An XML value" }
@doc:Param { value:"namespaces: A map object consisting of namespaces" }
native function addElement (xml x, string xPath, xml value, map namespaces);

@doc:Description { value:"Evaluates the XPath on an XML object and returns the matching string value. Namespaces are supported."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"xPath: An XPath" }
@doc:Param { value:"namespaces: A map object consisting of namespaces" }
@doc:Return { value:"string: Matching string value" }
native function getString (xml x, string xPath, map namespaces) (string);

@doc:Description { value:"Converts an XML object to a string representation."}
@doc:Param { value:"x: An XML object" }
@doc:Return { value:"string: String value of the converted XML" }
native function toString (xml x) (string);

@doc:Description { value:"Adds an attribute to the XML element that matches the given XPath. If the XPath matches the text value of an existing element, or if the XPath does not match an existing element, this operation will have no effect."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"xPath: An XPath" }
@doc:Param { value:"name: Name of the attribute to be added" }
@doc:Param { value:"value: Attribute value" }
native function addAttribute (xml x, string xPath, string name, string value);

