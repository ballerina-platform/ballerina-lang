package ballerina.lang.xmls;

import ballerina.doc;

@doc:Description { value: "Options struct for XML to JSON conversion "}
public struct Options {
    string attributePrefix = "@";
    boolean preserveNamespaces = true;
}

@doc:Description { value:"Check whether the XML sequence contains only a single element."}
@doc:Param { value:"x: An XML object" }
@doc:Return { value:"xml: Boolean flag indicating whether the XML sequence contains only a single element" }
public native function isSingleton(xml x) (boolean);

@doc:Description { value:"Check whether the XML sequence is empty."}
@doc:Param { value:"x: An XML object" }
@doc:Return { value:"xml: Boolean flag indicating whether the XML sequence is empty" }
public native function isEmpty(xml x) (boolean);

@doc:Description { value:"Get all the items that are of element type in an XML sequence."}
@doc:Param { value:"x: An XML object" }
@doc:Return { value:"xml: All the elements-type items in the given XML sequence" }
public native function elements(xml x) (xml);

@doc:Description { value:"Get all the items that are of element type, and matches the given qualified name, in an XML sequence."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"qname: Qualified name of the element" }
@doc:Return { value:"xml: All the elements-type items in the given XML sequence, that matches the qualified name" }
public native function select(xml x, string qname) (xml);

@doc:Description { value:"Get the type of a XML as a string. If the XML is singleton, type can be one of 'element', 'text', 'comment' or 'pi'. Returns an empty string if the XML is not a singleton."}
@doc:Param { value:"x: An XML object" }
@doc:Return { value:"xml: Type of the XML as a string" }
public native function getItemType(xml x) (string);

@doc:Description { value:"Get the fully qualified name of the element as a string. Returns an empty string if the XML is not a singleton."}
@doc:Param { value:"x: An XML object" }
@doc:Return { value:"xml: Qualified name of the XML as a string" }
public native function getElementName(xml x) (string);

@doc:Description { value:"Get the text value of a XML. If the XML is a sequence, concatenation of the text values of the members of the sequence is returned. If the XML is an element, then the text value of the sequence of children is returned.  If the XML is a text item, then the text is returned. Otherwise, an empty string is returned."}
@doc:Param { value:"x: An XML object" }
@doc:Return { value:"xml: Text value of the xml" }
public native function getTextValue(xml x) (string);

@doc:Description { value:"Selects all the children of the elements in an XML, and return as a sequence."}
@doc:Param { value:"x: An XML object" }
@doc:Return { value:"xml: All the children of the elements in the xml" }
public native function children(xml x) (xml);

@doc:Description { value:"Selects all the children of the elements in this sequence that matches the given qualified name."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"qname: Qualified name of the element" }
@doc:Return { value:"xml: All the children of the elements in this sequence that matches the given qualified name" }
public native function selectChildren(xml x, string qname) (xml);

@doc:Description { value:"Set the children of an XML if its a singleton. An Error otherwise. Any existing children will be removed."}
@doc:Param { value:"x: An XML object" }
public native function setChildren(xml x, xml children);

@doc:Description { value:"Make a deep copy of an XML."}
@doc:Param { value:"x: An XML object" }
@doc:Return { value:"xml: A Copy of the XML" }
public native function copy(xml x) (xml);

@doc:Description { value:"Strips any text items from an XML sequence that are all whitespace."}
@doc:Param { value:"x: An XML object" }
@doc:Return { value:"xml: Striped sequence" }
public native function strip(xml x) (xml);

@doc:Description { value:"Slice and return a subsequence of the an XML sequence."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"startIndex: Start index, inclusive" }
@doc:Param { value:"endIndex: End index, exclusive" }
@doc:Return { value:"xml: Sliced sequence" }
public native function slice(xml x, int startIndex, int endIndex) (xml);

@doc:Description { value:"Sets the attributes to the provided attributes map."}
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"attributes: Attributes map" }
public native function setAttributes(xml x, map attributes);

@doc:Description { value:"Converts a XML object to a JSON representation"}
@doc:Param { value:"x: A XML object" }
@doc:Return { value:"json: JSON representation of the given XML" }
public native function toJSON (xml x, Options options) (json);

@doc:Description { value:"Searches in children recursively for elements matching the qualified name and returns a sequence containing them all. Does not search within a matched result." }
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"qname: Qualified name of the element" }
@doc:Return { value:"xml: All the descendants that matches the given qualified name, as a sequence" }
public native function selectDescendants(xml x, string qname) (xml);

@doc:Description { value:"Remove an attribute from an XML." }
@doc:Param { value:"x: An XML object" }
@doc:Param { value:"qname: Qualified name of the attribute" }
public native function removeAttribute(xml x, string qname);
