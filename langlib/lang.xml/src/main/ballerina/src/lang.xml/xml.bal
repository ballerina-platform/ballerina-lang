// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/java;

# The namespace URI bound to the `xml` prefix.
public const string XML_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";
# The namespace URI bound to the `xmlns` prefix.
public const string XMLNS_NAMESPACE_URI = "http://www.w3.org/2000/xmlns/";

# The expanded name of the `xml:space` attribute.
public const string space = "{http://www.w3.org/XML/1998/namespace}space";
# The expanded name of the `xml:lang` attribute.
public const string lang = "{http://www.w3.org/XML/1998/namespace}lang";
# The expanded name of the `xml:base` attribute.
public const string base = "{http://www.w3.org/XML/1998/namespace}base";

# Type for singleton elements.
# Built-in subtype of xml.
@builtinSubtype
public type Element xml;

# Type for singleton processing instructions.
# Built-in subtype of xml.
@builtinSubtype
public type ProcessingInstruction xml;

# Type for singleton comments.
# Built-in subtype of xml.
@builtinSubtype
public type Comment xml;

# Type for zero or more text characters.
# Built-in subtype of xml.
# Adjacent xml text items are automatically concatenated,
# so an xml sequence belongs to this type if it is a singleton test sequence
# or the empty sequence.
@builtinSubtype
public type Text xml;

# Returns number of xml items in `x`.
#
# + x - xml item
# + return - number of XML items in `x`
public isolated function length(xml x) returns int = @java:Method {
    'class: "org.ballerinalang.langlib.xml.Length",
    name: "length"
} external;

# A type parameter that is a subtype of any singleton or empty xml sequence.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type ItemType Element|Comment|ProcessingInstruction|Text;
# A type parameter that is a subtype of `xml`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type XmlType xml;

# Returns an iterator over the xml items of `x`
#
# + x - xml sequence to iterate over
# + return - iterator object
# Each item is represented by an xml singleton.
public isolated function iterator(xml<ItemType> x) returns object {
    public isolated function next() returns record {| (xml|string) value; |}?;
} {
    XMLIterator xmlIterator = new(x);
    return xmlIterator;
}

# Returns the item of `x` with index `i`.
# This differs from `x[i]` in that it panics if
# `x` does not have an item with index `i`.
#
# + x - the xml sequence
# + i - the index
# + return - the item with index `i` in `x`
public isolated function get(xml<ItemType> x, int i) returns xml = @java:Method {
    'class: "org.ballerinalang.langlib.xml.Get",
    name: "get"
} external;

# Concatenates xml and string values.
#
# + xs - xml or string items to concatenate
# + return - an xml sequence that is the concatenation of all the `xs`;
#    an empty xml sequence if the `xs` are empty
public isolated function concat((xml|string)... xs) returns xml = @java:Method {
    'class: "org.ballerinalang.langlib.xml.Concat",
    name: "concat"
} external;

# Returns a string giving the expanded name of `elem`.
#
# + elem - xml element
# + return - element name
public isolated function getName(Element elem) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.xml.GetName",
    name: "getName"
} external;

# Change the name of element `elem` to `xName`.
#
# + elem - xml element
# + xName - new expanded name
public isolated function setName(Element elem, string xName) = @java:Method {
    'class: "org.ballerinalang.langlib.xml.SetName",
    name: "setName"
} external;

# Returns the children of `elem`.
#
# + elem - xml element
# + return - children of `elem`
public isolated function getChildren(Element elem) returns xml = @java:Method {
    'class: "org.ballerinalang.langlib.xml.GetChildren",
    name: "getChildren"
} external;

# Sets the children of `elem` to `children`.
# This panics if it would result in the element structure
# becoming cyclic.
#
# + elem - xml element
# + children - xml or string to set as children
public isolated function setChildren(Element elem, xml|string children) = @java:Method {
    'class: "org.ballerinalang.langlib.xml.SetChildren",
    name: "setChildren"
} external;

# Returns the map representing the attributes of `elem`.
# This includes namespace attributes.
# The keys in the map are the expanded names of the attributes.
#
# + x - xml element
# + return - attributes of `x`
public isolated function getAttributes(Element x) returns map<string> = @java:Method {
    'class: "org.ballerinalang.langlib.xml.GetAttributes",
    name: "getAttributes"
} external;

# Returns the target part of the processing instruction.
#
# + x - xml processing instruction item
# + return - target part of `x`
public isolated function getTarget(ProcessingInstruction x) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.xml.GetTarget",
    name: "getTarget"
} external;

# Returns the content of a text or processing instruction or comment item.
#
# + x - xml item
# + return - the content of `x`
public isolated function getContent(Text|ProcessingInstruction|Comment x) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.xml.GetContent",
    name: "getContent"
} external;

# Constructs an xml sequence consisting of only a new element item.
#
# + name - the name of the new element
# + children - the children of the new element
# + return - an xml sequence consisting of only a new xml element with name `name`,
#   no attributes, and children `children`
public isolated function createElement(string name, xml children = concat())
    returns Element = @java:Method {
        'class: "org.ballerinalang.langlib.xml.CreateElement",
        name: "createElement"
} external;

# Constructs an xml sequence consisting of only a processing instruction item.
#
# + target - the target part of the processing instruction to be constructed
# + content - the content part of the processing instruction to be constructed
# + return - an xml sequence consisting of a processing instruction with target `target`
#     and content `content`
public isolated function createProcessingInstruction(string target, string content)
    returns ProcessingInstruction = @java:Method {
        'class: "org.ballerinalang.langlib.xml.CreateProcessingInstruction",
        name: "createProcessingInstruction"
} external;

# Constructs an xml sequence consisting of only a comment item.
#
# + content - the content of the comment to be constructed.
# + return - an xml sequence consisting of a comment with content `content`
public isolated function createComment(string content) returns Comment = @java:Method {
    'class: "org.ballerinalang.langlib.xml.CreateComment",
    name: "createComment"
} external;

# Constructs an xml sequence representing zero of more parsed characters.
#
# + chars - the characters
# + return - an xml sequence that is either empty or consists of one text item
# The constructed sequence will be empty when the length of `chars` is zero.
public isolated function createText(string chars) returns Text = @java:Method {
    'class: "org.ballerinalang.langlib.xml.CreateText",
    name: "createText"
} external;

# Returns a subsequence of an xml value.
#
# + x - the xml value
# + startIndex - start index, inclusive
# + endIndex - end index, exclusive
# + return - a subsequence of `x` consisting of items with index >= startIndex and < endIndex
public isolated function slice(xml<ItemType> x, int startIndex, int endIndex = x.length())
    returns xml<ItemType> = @java:Method {
        'class: "org.ballerinalang.langlib.xml.Slice",
        name: "slice"
} external;

# Strips the insignificant parts of the an xml value.
# Comment items, processing instruction items are considered insignificant.
# After removal of comments and processing instructions, the text is grouped into
# the biggest possible chunks (i.e. only elements cause division into multiple chunks)
# and a chunk is considered insignificant if the entire chunk is whitespace.
#
# + x - the xml value
# + return - `x` with insignificant parts removed
public isolated function strip(xml x) returns xml = @java:Method {
    'class: "org.ballerinalang.langlib.xml.Strip",
    name: "strip"
} external;

# Selects elements from an xml value.
# If `nm` is `()`, selects all elements;
# otherwise, selects only elements whose expanded name is `nm`.
#
# + x - the xml value
# + nm - the expanded name of the elements to be selected, or `()` for all elements
# + return - an xml sequence consisting of all the element items in `x` whose expanded name is `nm`,
#  or, if `nm` is `()`, all element items in `x`
public isolated function elements(xml x, string? nm = ()) returns xml<Element> = @java:Method {
    'class: "org.ballerinalang.langlib.xml.Elements",
    name: "elements"
} external;

# Returns the children of elements in an xml value.
# When `x` is of type Element, it is equivalent to `getChildren`.
# + x - xml value
# + return - xml sequence containing the children of each element x concatenated in order
# This is equivalent to `elements(x).map(getChildren)`.
public isolated function children(xml x) returns xml = @java:Method {
    'class: "org.ballerinalang.langlib.xml.Children",
    name: "children"
} external;

# Selects element children of an xml value
# + x - the xml value
# + nm - the expanded name of the elements to be selected, or `()` for all elements
# + return - an xml sequence consisting of child elements of elements in `x`; if `nm`
#  is `()`, returns a sequence of all such elements;
#  otherwise, include only elements whose expanded name is `nm`
# This is equivalent to `children(x).elements(nm)`.
public isolated function elementChildren(xml x, string? nm = ()) returns xml<Element> = @java:Method {
    'class: "org.ballerinalang.langlib.xml.ElementChildren",
    name: "elementChildren"
} external;

// Functional programming methods

# Applies a function to each item in an xml sequence, and returns an xml sequence of the results.
# Each item is represented as a singleton value.
#
# + x - the xml value
# + func - a function to apply to each child or `item`
# + return - new xml value containing result of applying `func` to each child or `item`
public isolated function 'map(xml<ItemType> x, @isolatedParam function(ItemType item) returns XmlType func)
    returns xml<XmlType> = @java:Method {
        'class: "org.ballerinalang.langlib.xml.Map",
        name: "map"
} external;

# Applies a function to each item in an xml sequence.
# Each item is represented as a singleton value.
#
# + x - the xml value
# + func - a function to apply to each item in `x`
public isolated function forEach(xml<ItemType> x, @isolatedParam function(ItemType item) returns () func) = @java:Method {
    'class: "org.ballerinalang.langlib.xml.ForEach",
    name: "forEach"
} external;

# Selects the items from an xml sequence for which a function returns true.
# Each item is represented as a singleton value.
#
# + x - xml value
# + func - a predicate to apply to each item to test whether it should be selected
# + return - new xml sequence containing items in `x` for which `func` evaluates to true
public isolated function filter(xml<ItemType> x, @isolatedParam function(ItemType item) returns boolean func)
    returns xml = @java:Method {
        'class: "org.ballerinalang.langlib.xml.Filter",
        name: "filter"
} external;

# Constructs an xml value from a string.
# This parses the string using the `content` production of the
# XML 1.0 Recommendation.
#
# + s - a string in XML format
# + return - xml value resulting from parsing `s`, or an error
public isolated function fromString(string s) returns xml|error = @java:Method {
    'class: "org.ballerinalang.langlib.xml.FromString",
    name: "fromString"
} external;
