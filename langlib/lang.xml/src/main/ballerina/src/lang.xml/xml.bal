// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

const string XML_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";
const string XMLNS_NAMESPACE_URI = "http://www.w3.org/2000/xmlns/";

type XMLIterator object {

    private xml m;

    public function __init(xml m) {
        self.m = m;
    }

    public function next() returns record {|
        (xml|string) value;
    |}? = external;
};

# Returns number of XML items in `x`.
#
# + x - xml item
# + return - number of XML items in `x`
public function length(xml x) returns int = external;

# Returns an iterator over the xml items of `x`
#
# + x - xml item to iterate
# + return - iterator object
public function iterator(xml x) returns abstract object {
    public function next() returns record {|
        (xml|string) value;
    |}?;
    } {
    XMLIterator xmlIterator = new(x);
    return xmlIterator;
}
# Check whether the XML sequence contains only a single element.
#
# + x - The xml source
# + return - Boolean flag indicating whether the XML sequence contains only a single element
public function isSingleton(xml x) returns boolean = external;

# Check whether the XML sequence is empty.
#
# + x - The xml source
# + return - Boolean flag indicating whether the XML sequence is empty
public function isEmpty(xml x) returns boolean = external;

# Get all the items that are of element type in an XML sequence.
#
# + x - The xml source
# + return - All the elements-type items in the given XML sequence
public function elements(xml x) returns xml = external;

# Get all the items that are of element type, and matches the given qualified name, in an XML sequence.
#
# + x - The xml source
# + qname - Qualified name of the element
# + return - All the elements-type items in the given XML sequence, that matches the qualified name
public function select(xml x, string qname) returns xml = external;

# Get the type of a XML as a string. If the XML is singleton, type can be one of 'element', 'text', 'comment' or 'pi'.
# Returns an empty string if the XML is not a singleton.
#
# + x - The xml source
# + return - Type of the XML as a string
public function getItemType(xml x) returns string = external;

# Get the fully qualified name of the element as a string. Returns an empty string if the XML is not a singleton.
#
# + x - The xml source
# + return - Qualified name of the XML as a string
public function getElementName(xml x) returns string = external;

# Get the text value of a XML. If the XML is a sequence, concatenation of the text values of the members of the
# sequence is returned. If the XML is an element, then the text value of the sequence of children is returned. If
# the XML is a text item, then the text is returned. Otherwise, an empty string is returned.
#
# + x - The xml source
# + return - Text value of the xml
public function getTextValue(xml x) returns string = external;

# Make a deep copy of an XML.
#
# + x - The xml source
# + return - A Copy of the XML
public function copy(xml x) returns xml = external;

# Strips any text items from an XML sequence that are all whitespace.
#
# + x - The xml source
# + return - Striped sequence
public function strip(xml x) returns xml = external;

# Slice and return a subsequence of the an XML sequence.
#
# + x - The xml source
# + startIndex - Start index, inclusive
# + endIndex - End index, exclusive
# + return - Sliced sequence
public function slice(xml x, int startIndex, int endIndex) returns xml = external;

# Sets the attributes to the provided attributes map.
#
# + x - The xml source
# + attributes - Attributes map
public function setAttributes(xml x, map<any> attributes) = external;

# Searches in children recursively for elements matching the qualified name and returns a sequence containing them
# all. Does not search within a matched result.
#
# + x - The xml source
# + qname - Qualified name of the element
# + return - All the descendants that matches the given qualified name, as a sequence
public function selectDescendants(xml x, string qname) returns xml = external;

# Remove an attribute from an XML.
#
# + x - The xml source
# + qname - Qualified name of the attribute
public function removeAttribute(xml x, string qname) = external;

# Append children to an XML if its an element type XML. Error otherwise.
# New children will be appended at the end of the existing children.
#
# + x - The xml source
# + children - children
public function appendChildren(xml x, xml children) = external;

# Remove children matching the given name from an XML. This operation has no effect
# if the XML is not an element type XML.
#
# + x - The xml source
# + qname - Namespace qualified name of the children to be removed
public function removeChildren(xml x, string qname) = external;

# Concatenate all the `xs`. Empty xml sequence if empty.
#
# + xs - xml or string items to concat
# + return - xml sequence containing `xs`
public function concat((xml|string)... xs) returns xml = external;

# Returns true if `x` is a singleton xml sequence consisting of an element item.
#
# + x - xml value
# + return - true if `x` is an xml element item
public function isElement(xml x) returns boolean = external;

# Returns true if `x` is a singleton xml sequence consisting of a processing instruction item.
#
# + x - xml value
# + return - true if `x` is a xml processing instruction
public function isProcessingInstruction(xml x) returns boolean = external;

# Returns true if `x` is a singleton xml sequence consisting of a comment item.
#
# + x - xml value
# + return - true if `x` is a xml comment item
public function isComment(xml x) returns boolean = external;

# Returns true if `x` is an xml sequence consisting of one or more character items.
#
# + x - xml value
# + return - true if `x` is a sequence containing any charactor items
public function isText(xml x) returns boolean = external;

# Represents a parameter for which isElement must be true.
type Element xml;
# Represents a parameter for which isProcessingInstruction must be true.
type ProcessingInstruction xml;
# Represents a parameter for which isComment must be true.
type Comment xml;
# Represents a parameter for which isText must be true.
type Text xml;

# Returns a string giving the expanded name of `elem`.
#
# + elem - xml element
# + return - element name
public function getName(Element elem) returns string = external;

# Change the name of element `elmem` to `xName`.
#
# + elem - xml element
# + xName - new name
public function setName(Element elem, string xName) = external;

# Returns the children of `elem`.
# Panics if `isElement(elem)` is not true.
#
# + elem - xml element
# + return - children of `elem`
public function getChildren(Element elem) returns xml = external;

# Sets the children of `elem` to `children`.
# Panics if `isElement(elem)` is not true.
#
# + elem - xml element
# + children - xml or string to set as children
public function setChildren(Element elem, xml|string children) = external;

# Returns the map representing the attributes of `elem`.
# This includes namespace attributes.
# The keys in the map are the expanded name of the attribute.
# Panics if `isElement(elem)` is not true.
# There is no setAttributes function.
#
# + x - xml element
# + return - attributes of `x`
public function getAttributes(Element x) returns map<string> = external;

# Returns the target part of the processing instruction.
#
# + x - xml processing instruction item
# + return - target potion of `x`
public function getTarget(ProcessingInstruction x) returns string = external;

# Returns the content of a text or processing instruction or comment item.
#
# + x - xml item
# + return - content of `x`
public function getContent(Text|ProcessingInstruction|Comment x) returns string = external;

# Creates an element with the specified children
# The attributes are empty initially
#
# + name - element name
# + children - children of element
# + return - new xml element
// todo: 2nd arg should be xml children = concat()
// https://github.com/ballerina-platform/ballerina-lang/issues/16953
public function createElement(string name, xml children) returns Element = external;

# Creates a processing instruction with the specified `target` and `content`.
#
# + target - target potion of xml processing instruction
# + content - content potion of xml processing instruction
# + return - new xml processing instruction element
public function createProcessingInstruction(string target, string content) returns ProcessingInstruction
 = external;

# Creates a comment with the specified `content`.
#
# + content - comment content
# + return - xml comment element
public function createComment(string content) returns Comment = external;

// Functional programming methods

# For xml sequence returns the result of applying function `func` to each member of sequence `item`.
# For xml element returns the result of applying function `funct` to `item`.
#
# + arr - the x
# + func - a function to apply to each child or `item`
# + return - new xml value containing result of applying function `func` to each child or `item`
public function map(xml x, function(xml|string item) returns xml|string func) returns xml = external;

# For xml sequence apply the `func` to children of `item`.
# For xml element apply the `func` to `item`.
#
# + x - the xml value
# + func - a function to apply to each child or `item`
public function forEach(xml x, function(xml|string item) returns () func) = external;

# For xml sequence returns a new xml sequence constructed from children of `x` for which `func` returns true.
# For xml element returns a new xml sequence constructed from `x` if `x` applied to `funct` returns true, else
# returns an empty sequence.
#
# + x - xml value
# + func - a predicate to apply to each child to determine if it should be included
# + return - new xml sequence containing filtered children
public function filter(xml x, function(xml|string item) returns boolean func) returns xml = external;

# This is the inverse of `value:toString` applied to an `xml`.
#
# + s - string representation of xml
# + return - parsed xml value or error
public function fromString(string s) returns xml|error = external;
