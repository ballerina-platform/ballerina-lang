package ballerina.builtin;

@Description { value: "Options struct for XML to JSON conversion "}
@Field {value:"attributePrefix:  prefix to add to the key to identify a XML attribute and namespaces, default value is '@'."}
@Field {value:"preserveNamespaces: Indicate whether to preserve namespace prefixes when converting or not."}
public struct xmlOptions {
    string attributePrefix = "@";
    boolean preserveNamespaces = true;
}

@Description { value:"Check whether the XML sequence contains only a single element."}
@Param { value:"x: An XML object" }
@Return { value:"Boolean flag indicating whether the XML sequence contains only a single element" }
documentation {
Check whether the XML sequence contains only a single element.
- #x An XML object
- #single Boolean flag indicating whether the XML sequence contains only a single element
}
public native function <xml x> isSingleton() (boolean single);

@Description { value:"Check whether the XML sequence is empty."}
@Param { value:"x: An XML object" }
@Return { value:"Boolean flag indicating whether the XML sequence is empty" }
documentation {
Check whether the XML sequence is empty.
- #x An XML object
- #empty Boolean flag indicating whether the XML sequence is empty
}
public native function <xml x> isEmpty() (boolean empty);

@Description { value:"Get all the items that are of element type in an XML sequence."}
@Param { value:"x: An XML object" }
@Return { value:"All the elements-type items in the given XML sequence" }
documentation {
Get all the items that are of element type in an XML sequence.
- #x An XML object
- #items All the elements-type items in the given XML sequence
}
public native function <xml x> elements() (xml items);

@Description { value:"Get all the items that are of element type, and matches the given qualified name, in an XML sequence."}
@Param { value:"x: An XML object" }
@Param { value:"qname: Qualified name of the element" }
@Return { value:"All the elements-type items in the given XML sequence, that matches the qualified name" }
documentation {
Get all the items that are of element type, and matches the given qualified name, in an XML sequence.
- #x An XML object
- #qname Qualified name of the element
- #items All the elements-type items in the given XML sequence, that matches the qualified name
}
public native function <xml x> select(string qname) (xml items);

@Description { value:"Get the type of a XML as a string. If the XML is singleton, type can be one of 'element', 'text', 'comment' or 'pi'. Returns an empty string if the XML is not a singleton."}
@Param { value:"x: An XML object" }
@Return { value:"Type of the XML as a string" }
documentation {
Get the type of a XML as a string. If the XML is singleton, type can be one of 'element', 'text', 'comment' or 'pi'. Returns an empty string if the XML is not a singleton.
- #x An XML object
- #typeString Type of the XML as a string
}
public native function <xml x> getItemType() (string typeString);

@Description { value:"Get the fully qualified name of the element as a string. Returns an empty string if the XML is not a singleton."}
@Param { value:"x: An XML object" }
@Return { value:"Qualified name of the XML as a string" }
documentation {
Get the fully qualified name of the element as a string. Returns an empty string if the XML is not a singleton.
- #x An XML object
- #name Qualified name of the XML as a string
}
public native function <xml x> getElementName() (string name);

@Description { value:"Get the text value of a XML. If the XML is a sequence, concatenation of the text values of the members of the sequence is returned. If the XML is an element, then the text value of the sequence of children is returned.  If the XML is a text item, then the text is returned. Otherwise, an empty string is returned."}
@Param { value:"x: An XML object" }
@Return { value:"Text value of the xml" }
documentation {
Get the text value of a XML. If the XML is a sequence, concatenation of the text values of the members of the sequence is returned. If the XML is an element, then the text value of the sequence of children is returned.  If the XML is a text item, then the text is returned. Otherwise, an empty string is returned.
- #x An XML object
- #text Text value of the xml
}
public native function <xml x> getTextValue() (string text);

@Description { value:"Selects all the children of the elements in an XML, and return as a sequence."}
@Param { value:"x: An XML object" }
@Return { value:"All the children of the elements in the xml" }
documentation {
Selects all the children of the elements in an XML, and return as a sequence.
- #x An XML object
- #xChildren All the children of the elements in the xml
}
public native function <xml x> children() (xml xChildren);

@Description { value:"Selects all the children of the elements in this sequence that matches the given qualified name."}
@Param { value:"x: An XML object" }
@Param { value:"qname: Qualified name of the element" }
@Return { value:"All the children of the elements in this sequence that matches the given qualified name" }
documentation {
Selects all the children of the elements in this sequence that matches the given qualified name.
- #x An XML object
- #xChildren All the children of the elements in this sequence that matches the given qualified name
}
public native function <xml x> selectChildren(string qname) (xml xChildren);

@Description { value:"Set the children of an XML if its a singleton. An Error otherwise. Any existing children will be removed."}
@Param { value:"x: An XML object" }
documentation {
Set the children of an XML if its a singleton. An Error otherwise. Any existing children will be removed.
- #x An XML object
}
public native function <xml x> setChildren(xml children);

@Description { value:"Make a deep copy of an XML."}
@Param { value:"x: An XML object" }
@Return { value:"A Copy of the XML" }
documentation {
Make a deep copy of an XML.
- #x An XML object
- #deepCopy A Copy of the XML
}
public native function <xml x> copy() (xml deepCopy);

@Description { value:"Strips any text items from an XML sequence that are all whitespace."}
@Param { value:"x: An XML object" }
@Return { value:"Striped sequence" }
documentation {
Strips any text items from an XML sequence that are all whitespace.
- #x An XML object
- #xSequence Striped sequence
}
public native function <xml x> strip() (xml xSequence);

@Description { value:"Slice and return a subsequence of the an XML sequence."}
@Param { value:"x: An XML object" }
@Param { value:"startIndex: Start index, inclusive" }
@Param { value:"endIndex: End index, exclusive" }
@Return { value:"Sliced sequence" }
documentation {
Slice and return a subsequence of the an XML sequence.
- #x An XML object
- #startIndex Start index, inclusive
- #endIndex End index, exclusive
- #xSequence Sliced sequence
}
public native function <xml x> slice(int startIndex, int endIndex) (xml xSequence);

@Description { value:"Sets the attributes to the provided attributes map."}
@Param { value:"x: An XML object" }
@Param { value:"attributes: Attributes map" }
documentation {
Sets the attributes to the provided attributes map.
- #x An XML object
- #attributes Attributes map
}
public native function <xml x> setAttributes(map attributes);

@Description { value:"Converts a XML object to a JSON representation"}
@Param { value:"x: A XML object" }
@Param { value:"options: xmlOptions struct for XML to JSON conversion properties" }
@Return { value:"JSON representation of the given XML" }
documentation {
Converts a XML object to a JSON representation
- #x An XML object
- #options xmlOptions struct for XML to JSON conversion properties
- #j JSON representation of the given XML
}
public native function <xml x> toJSON (xmlOptions options) (json j);

@Description { value:"Searches in children recursively for elements matching the qualified name and returns a sequence containing them all. Does not search within a matched result." }
@Param { value:"x: An XML object" }
@Param { value:"qname: Qualified name of the element" }
@Return { value:"All the descendants that matches the given qualified name, as a sequence" }
documentation {
Searches in children recursively for elements matching the qualified name and returns a sequence containing them all. Does not search within a matched result.
- #x An XML object
- #qname Qualified name of the element
- #xDescendants All the descendants that matches the given qualified name, as a sequence
}
public native function <xml x> selectDescendants(string qname) (xml xDescendants);

@Description { value:"Remove an attribute from an XML." }
@Param { value:"x: An XML object" }
@Param { value:"qname: Qualified name of the attribute" }
documentation {
Remove an attribute from an XML.
- #x An XML object
- #qname Qualified name of the attribute
}
public native function <xml x> removeAttribute(string qname);
