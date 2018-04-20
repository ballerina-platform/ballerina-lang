
import ballerina/doc;

@doc:Description { value: "Options struct for XML to JSON conversion "}
struct Options {
    string attributePrefix = "@";
    boolean preserveNamespaces = true;
}

@doc:Description { value:"Sets the string value of an element that matches the given XPath. If the XPath matches an existing element, that element's value will be updated. If the XPath does not match an existing element, this operation will have no effect. Namespaces are supported."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"xPath: An XPath" }
@doc:Param { value:"value: A string value" }
@doc:Param { value:"namespaces: A map object consisting of namespaces" }
native function setStringWithNamespace (xml x, string xPath, string value, map namespaces);

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
native function setXmlWithNamespace (xml x, string xPath, xml value, map namespaces);

@doc:Description { value:"Removes the element(s) that match the given XPath. Namespaces are supported."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"xPath: An XPath" }
@doc:Param { value:"namespaces: A map object consisting of namespaces" }
native function removeWithNamespace (xml x, string xPath, map namespaces);

@doc:Description { value:"Adds an XML element to the location that matches the given XPath. If the XPath matches the text value of an existing element, or if the XPath does not match an existing element, this operation will have no effect."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"xPath: An XPath" }
@doc:Param { value:"value: An XML value" }
native function addElement (xml x, string xPath, xml value);

@doc:Description { value:"Sets the XML value of an element that matches the given XPath. If the XPath matches an existing element, that element's value will be updated. If the XPath does not match an existing element, this operation will have no effect."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"xPath: An XPath" }
@doc:Param { value:"value: An XML value" }
native function setXml (xml x, string xPath, xml value);

@doc:Description { value:"Removes the element(s) that match the given XPath."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"xPath: An XPath" }
native function remove (xml x, string xPath);

@doc:Description { value:"Sets the string value of an element that matches the given XPath. If the XPath matches an existing element, that element's value will be updated. If the XPath does not match an existing element, this operation will have no effect."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"xPath: An XPath" }
@doc:Param { value:"nameSpaces: A string value" }
native function setString (xml x, string xPath, string value);

@doc:Description { value:"Adds an attribute to the XML element that matches the given XPath. If the XPath matches the text value of an existing element, or if the XPath does not match an existing element, this operation will have no effect. Namespaces are supported."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"xPath: An XPath" }
@doc:Param { value:"name: Name of the attribute to be added" }
@doc:Param { value:"value: Attribute value" }
@doc:Param { value:"namespaces: A map object consisting of namespaces" }
native function addAttributeWithNamespace (xml x, string xPath, string name, string value, map namespaces);

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
native function getXmlWithNamespace (xml x, string xPath, map namespaces) (xml);

@doc:Description { value:"Adds an XML element to the location that matches the given XPath. If the XPath matches the text value of an existing element, or if the XPath does not match an existing element, this operation will have no effect. Namespaces are supported."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"xPath: An XPath" }
@doc:Param { value:"value: An XML value" }
@doc:Param { value:"namespaces: A map object consisting of namespaces" }
native function addElementWithNamespace (xml x, string xPath, xml value, map namespaces);

@doc:Description { value:"Evaluates the XPath on an XML object and returns the matching string value. Namespaces are supported."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"xPath: An XPath" }
@doc:Param { value:"namespaces: A map object consisting of namespaces" }
@doc:Return { value:"string: Matching string value" }
native function getStringWithNamespace (xml x, string xPath, map namespaces) (string);

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

@doc:Description { value:"Check whether the XML sequence contains only a single element."}
@doc:Param { value:"x: An XML object" }
native function isSingleton(xml x) (boolean);

@doc:Description { value:"Check whether the XML sequence is empty."}
@doc:Param { value:"x: An XML object" }
native function isEmpty(xml x) (boolean);

@doc:Description { value:"Get all the items that are of element type in an XML sequence."}
@doc:Param { value:"x: An XML object" }
native function elements(xml x) (xml);

@doc:Description { value:"Get all the items that are of element type, and matches the given qualified name, in an XML sequence."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"qname: Qualified name of the element" }
native function select(xml x, string qname) (xml);

@doc:Description { value:"Get the type of a XML as a string. If the XML is singleton, type can be one of 'element', 'text', 'comment' or 'pi'. Returns an empty string if the XML is not a singleton."}
@doc:Param { value:"x: An XML object" }
native function getItemType(xml x) (string);

@doc:Description { value:"Get the fully qualified name of the element as a string. Returns an empty string if the XML is not a singleton."}
@doc:Param { value:"x: An XML object" }
native function getElementName(xml x) (string);

@doc:Description { value:"Get the text value of a XML. If the XML is a sequence, concatenation of the text values of the members of the sequence is returned. If the XML is an element, then the text value of the sequence of children is returned.  If the XML is a text item, then the text is returned. Otherwise, an empty string is returned."}
@doc:Param { value:"x: An XML object" }
native function getTextValue(xml x) (string);

@doc:Description { value:"Selects all the children of the elements in an XML, and return as a sequence."}
@doc:Param { value:"x: An XML object" }
native function children(xml x) (xml);

@doc:Description { value:"Selects all the children of the elements in this sequence that matches the given qualified name."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"qname: Qualified name of the element" }
native function selectChildren(xml x, string qname) (xml);

@doc:Description { value:"Set the children of an XML if its a singleton. An Error otherwise. Any existing children will be removed."}
@doc:Param { value:"x: An XML object" }
native function setChildren(xml x, xml children);

@doc:Description { value:"Make a deep copy of an XML."}
@doc:Param { value:"x: An XML object" }
native function copy(xml x) (xml);

@doc:Description { value:"Parse and get an XML from a string ."}
@doc:Param { value:"xmlStr: String representation of XML" }
native function parse(string xmlStr) (xml);

@doc:Description { value:"Strips any text items from an XML sequence that are all whitespace."}
@doc:Param { value:"x: An XML object" }
native function strip(xml x) (xml);

@doc:Description { value:"Slice and return a subsequence of the an XML sequence."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"startIndex: Start index, inclusive" }
@doc:Param { value:"endIndex: End index, exclusive" }
native function slice(xml x, int startIndex, int endIndex) (xml);

@doc:Description { value:"Sets the attributes to the provided attributes map."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"attributes: Attributes map" }
native function setAttributes(xml x, map attributes);

@doc:Description { value:"Converts a XML object to a JSON representation"}
@doc:Param { value:"x: A XML object" }
@doc:Return { value:"json: JSON representation of the given XML" }
native function toJSON (xml x, Options options) (json);