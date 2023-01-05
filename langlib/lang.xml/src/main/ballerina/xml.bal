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

import ballerina/jballerina.java;

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

# Returns number of xml items in an xml value.
#
# ```ballerina
# xml `<book><title>Sherlock Holmes</title></book><!--Novel-->`.length() ⇒ 2
# ```
# 
# + x - xml item
# + return - number of xml items in parameter `x`
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

# Returns an iterator over the xml items of an xml sequence.
#
# # Each item is represented by an xml singleton.
# 
# ```ballerina
# object {
#     public isolated function next() returns record {|xml value;|}?;
# } iterator = xml `<student><name>John</name></student><student><name>Peter</name></student>`.iterator();
# iterator.next() ⇒ {"value":`<student><name>John</name></student>`}
# ```
#
# + x - xml sequence to iterate over
# + return - iterator object
public isolated function iterator(xml<ItemType> x) returns object {
    public isolated function next() returns record {| ItemType value; |}?;
} {
    XMLIterator xmlIterator = new(x);
    return xmlIterator;
}

# Returns the item of an xml sequence with given index.
#
# This differs from `x[i]` in that it panics if
# parameter `x` does not have an item with index parameter `i`.
# 
# ```ballerina
# xml x = xml `<book><title>Macbeth</title></book><book><title>Hamlet</title></book>`;
# x.get(1) ⇒ <book><title>Hamlet</title></book>
# 
# x.get(15) ⇒ panic
# ```
#
# + x - the xml sequence
# + i - the index
# + return - the item with index parameter `i` in parameter `x`
public isolated function get(xml<ItemType> x, int i) returns ItemType = @java:Method {
    'class: "org.ballerinalang.langlib.xml.Get",
    name: "get"
} external;

# Concatenates xml and string values.
# 
# ```ballerina
# xml bookA = xml `<book>Sherlock Holmes</book>`;
# xml bookB = xml `<book>Hamlet</book>`;
# xml:concat(bookA, bookB, xml `<book>Macbeth</book>`) ⇒ <book>Sherlock Holmes</book><book>Hamlet</book><book>Macbeth</book>
# 
# bookA.concat(bookB) ⇒ <book>Sherlock Holmes</book><book>Hamlet</book>
# 
# bookB.concat("Novel") ⇒ <book>Hamlet</book>Novel
# 
# xml:concat("Hello", "World") ⇒ HelloWorld
# 
# xml[] subjects = [xml `<subject>English</subject>`, xml `<subject>Math</subject>`, xml `<subject>ICT</subject>`];
# xml:concat(...subjects) ⇒ <subject>English</subject><subject>Math</subject><subject>ICT</subject>
# ```
#
# + xs - xml or string items to concatenate
# + return - an xml sequence that is the concatenation of all the parameter `xs`;
#    an empty xml sequence if the parameter `xs` is empty
public isolated function concat((xml|string)... xs) returns xml = @java:Method {
    'class: "org.ballerinalang.langlib.xml.Concat",
    name: "concat"
} external;

# Returns a string giving the expanded name of an xml element.
#
# ```ballerina
# xml:Element e = xml `<person age="30">John</person>`;
# e.getName() ⇒ person
# ```
# 
# + elem - xml element
# + return - element name
public isolated function getName(Element elem) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.xml.GetName",
    name: "getName"
} external;

# Changes the name of an XML element.
#
# ```ballerina
# xml:Element e = xml `<person>John</person>`;
# e.setName("student");
# e ⇒ <student>John</student>
# ```
# 
# + elem - xml element
# + xName - new expanded name
public isolated function setName(Element elem, string xName) = @java:Method {
    'class: "org.ballerinalang.langlib.xml.SetName",
    name: "setName"
} external;

# Returns the map representing the attributes of an xml element.
#
# This includes namespace attributes.
# The keys in the map are the expanded names of the attributes.
#
# ```ballerina
# xml:Element e = xml `<person id="1012" employed="yes"><name>John</name></person>`;
# e.getAttributes() ⇒ {"id":"1012","employed":"yes"}
# ```
# 
# + x - xml element
# + return - attributes of parameter `x`
public isolated function getAttributes(Element x) returns map<string> = @java:Method {
    'class: "org.ballerinalang.langlib.xml.GetAttributes",
    name: "getAttributes"
} external;

# Returns the children of an xml element.
#
# ```ballerina
# xml:Element e = xml `<books><book>Hamlet</book><book>Macbeth</book></books>`;
# e.getChildren() ⇒ <book>Hamlet</book><book>Macbeth</book>
# ```
# 
# + elem - xml element
# + return - children of parameter `elem`
public isolated function getChildren(Element elem) returns xml = @java:Method {
    'class: "org.ballerinalang.langlib.xml.GetChildren",
    name: "getChildren"
} external;

# Sets the children of an xml element.
#
# This panics if it would result in the element structure
# becoming cyclic.
# 
# ```ballerina
# xml:Element employees = xml `<employees><employee>David</employee><employee>Peter</employee></employees>`;
# employees.setChildren(xml `<employee>Alice</employee><employee>Bob</employee>`);
# employees ⇒ <employees><employee>Alice</employee><employee>Bob</employee></employees>
# 
# xml:Element student = xml `<student id="1205">John</student>`;
# student.setChildren("Jane");
# student ⇒ <student id="1205">Jane</student>
# ```
#
# + elem - xml element
# + children - xml or string to set as children
public isolated function setChildren(Element elem, xml|string children) = @java:Method {
    'class: "org.ballerinalang.langlib.xml.SetChildren",
    name: "setChildren"
} external;

# Returns the descendants of an xml element.
#
# The descendants of an element are the children of the element
# together with, for each of those children that is an element,
# the descendants of that element, ordered so that
# each element immediately precedes all its descendants.
# The order of the items in the returned sequence will thus correspond
# to the order in which the first character of the representation
# of the item would occur in the representation of the element in XML syntax.
#
# ```ballerina
# xml:Element e = xml `<person><name>John Doe</name><age>30</age></person>`;
# e.getDescendants() ⇒ <name>John Doe</name>John Doe<age>30</age>30
# ```
# 
# + elem - xml element
# + return - descendants of parameter `elem`
public isolated function getDescendants(Element elem) returns xml = @java:Method {
    'class: "org.ballerinalang.langlib.xml.GetDescendants",
    name: "getDescendants"
} external;

# Returns a string with the character data of an xml value.
#
# The character data of an xml value is as follows:
# * the character data of a text item is a string with one character for each
#     character information item represented by the text item;
# * the character data of an element item is the character data of its children;
# * the character data of a comment item is the empty string;
# * the character data of a processing instruction item is the empty string;
# * the character data of an empty xml sequence is the empty string;
# * the character data of the concatenation of two xml sequences x1 and x2 is the
#    concatenation of the character data of x1 and the character data of x2.
#
# ```ballerina
# xml x = xml `<book>Jane Eyre</book>`;
# x.data() ⇒ Jane Eyre
# ```
# 
# + x - the xml value
# + return - a string consisting of all the character data of parameter `x`
public isolated function data(xml x) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.xml.Data",
    name: "data"
} external;

# Returns the target part of the processing instruction.
#
# ```ballerina
# xml:ProcessingInstruction p = xml `<?sort ascending?>`;
# p.getTarget() ⇒ sort
# ```
# 
# + x - xml processing instruction item
# + return - target part of parameter `x`
public isolated function getTarget(ProcessingInstruction x) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.xml.GetTarget",
    name: "getTarget"
} external;

# Returns the content of a processing instruction or comment item.
#
# ```ballerina
# xml:ProcessingInstruction procInstruction = xml `<?sort ascending?>`;
# procInstruction.getContent() ⇒ ascending
# 
# xml:Comment comment = xml `<!--Employees by department-->`;
# comment.getContent() ⇒ Employees by department
# ```
# 
# + x - xml item
# + return - the content of parameter `x`
public isolated function getContent(ProcessingInstruction|Comment x) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.xml.GetContent",
    name: "getContent"
} external;

# Creates a new xml element item.
#
# ```ballerina
# xml:createElement(
#     "book", 
#     {genre: "Mystery", year: "1892"}, 
#     xml `<title>Sherlock Holmes</title><author>Arthur Conan Doyle</author>`
# ) ⇒ <book genre="Mystery" year="1892"><title>Sherlock Holmes</title><author>Arthur Conan Doyle</author></book>
# 
# xml:createElement("person") ⇒ <person/>
# 
# xml:createElement("student", {id: "1209"}) ⇒ <student id="1209"/>
# 
# xml:createElement("employee", children = xml `<name>John</name>`) ⇒ <employee><name>John</name></employee>
# ```
# 
# + name - the name of the new element
# + attributes - the attributes of the new element
# + children - the children of the new element
# + return - an xml sequence consisting of only a new xml element with name `name`,
#   attributes `attributes`, and children `children`
# The element's attribute map is a newly created map, into which any attributes specified
# by the `attributes` map are copied.

public isolated function createElement(string name, map<string> attributes = {}, xml children = xml``)
    returns Element = @java:Method {
        'class: "org.ballerinalang.langlib.xml.CreateElement",
        name: "createElement"
} external;

# Creates a new xml processing instruction item.
#
# ```ballerina
# xml:createProcessingInstruction("sort", "descending") ⇒ <?sort descending?>
# ```
# 
# + target - the target part of the processing instruction to be constructed
# + content - the content part of the processing instruction to be constructed
# + return - an xml sequence consisting of a processing instruction with parameter `target` as the target 
#     and parameter `content` as the content
public isolated function createProcessingInstruction(string target, string content)
    returns ProcessingInstruction = @java:Method {
        'class: "org.ballerinalang.langlib.xml.CreateProcessingInstruction",
        name: "createProcessingInstruction"
} external;

# Creates a new xml comment item.
#
# ```ballerina
# xml:createComment("Example comment") ⇒ <!--Example comment-->
# ```
# 
# + content - the content of the comment to be constructed.
# + return - an xml sequence consisting of a comment with parameter  `content` as the content
public isolated function createComment(string content) returns Comment = @java:Method {
    'class: "org.ballerinalang.langlib.xml.CreateComment",
    name: "createComment"
} external;

# Constructs an xml value of type Text.
#
# The constructed sequence will be empty when the length of parameter `data` is zero.
#
# ```ballerina
# xml:createText("Hello!") ⇒ Hello!
# ```
# 
# + data - the character data of the Text item
# + return - an xml sequence that is either empty or consists of one text item
public isolated function createText(string data) returns Text = @java:Method {
    'class: "org.ballerinalang.langlib.xml.CreateText",
    name: "createText"
} external;

# Returns a subsequence of an xml value.
#
# ```ballerina
# xml x = xml `<book>HTML</book><book>Invisible Man</book><book>David Copperfield</book><book>Jane Eyre</book>`;
# x.slice(2) ⇒ <book>David Copperfield</book><book>Jane Eyre</book>
# 
# x.slice(1, 3) ⇒ <book>Invisible Man</book><book>David Copperfield</book>
# ```
# 
# + x - the xml value
# + startIndex - start index, inclusive
# + endIndex - end index, exclusive
# + return - a subsequence of parameter `x` consisting of items with index >= parameter `startIndex` and < parameter `endIndex`
public isolated function slice(xml<ItemType> x, int startIndex, int endIndex = x.length())
    returns xml<ItemType> = @java:Method {
        'class: "org.ballerinalang.langlib.xml.Slice",
        name: "slice"
} external;

# Strips the insignificant parts of the an xml value.
#
# Comment items, processing instruction items are considered insignificant.
# After removal of comments and processing instructions, the text is grouped into
# the biggest possible chunks (i.e., only elements cause division into multiple chunks)
# and a chunk is considered insignificant if the entire chunk is whitespace.
#
# ```ballerina
# xml x = xml `<?publication year="1604"?><!--This is a play-->
#                   <book author="William Shakespeare"><title>Othello</title></book>`;
# x.strip() ⇒ <book author="William Shakespeare"><title>Othello</title></book>
# ```
# 
# + x - the xml value
# + return - `x` with insignificant parts removed
public isolated function strip(xml x) returns xml = @java:Method {
    'class: "org.ballerinalang.langlib.xml.Strip",
    name: "strip"
} external;

# Selects elements from an xml value.
#
# If parameter `nm` is `()`, selects all elements;
# otherwise, selects only elements whose expanded name is parameter `nm`.
#
# ```ballerina
# xml x = xml `<!--Mystery--><novel>Sherlock Holmes</novel><!--Drama--><play>Hamlet</play>
#             <novel>Jane Eyre</novel><play>Macbeth</play>`;
# x.elements() ⇒ <novel>Sherlock Holmes</novel><play>Hamlet</play><novel>Jane Eyre</novel><play>Macbeth</play>
# 
# x.elements("novel") ⇒ <novel>Sherlock Holmes</novel><novel>Jane Eyre</novel>
# ```
# 
# + x - the xml value
# + nm - the expanded name of the elements to be selected, or `()` for all elements
# + return - an xml sequence consisting of all the element items in parameter `x` whose expanded name is parameter `nm`,
#  or, if parameter `nm` is `()`, all element items in parameter `x`
public isolated function elements(xml x, string? nm = ()) returns xml<Element> = @java:Method {
    'class: "org.ballerinalang.langlib.xml.Elements",
    name: "elements"
} external;

# Returns the children of elements in an xml value.
#
# When parameter `x` is of type `Element`, it is equivalent to function `getChildren`.
# This is equivalent to `elements(x).map(getChildren)`.
#
# ```ballerina
# xml x = xml `<books><book><title>Hamlet</title></book><book><title>Macbeth</title></book></books>`;
# x.children() ⇒ <book><title>Hamlet</title></book><book><title>Macbeth</title></book>
# ```
# 
# + x - xml value
# + return - xml sequence containing the children of each element x concatenated in order
public isolated function children(xml x) returns xml = @java:Method {
    'class: "org.ballerinalang.langlib.xml.Children",
    name: "children"
} external;

# Selects element children of an xml value.
#
# This is equivalent to `children(x).elements(nm)`.
#
# ```ballerina
# xml x = xml `<book><play>Hamlet</play></book><book><novel>Macbeth</novel></book>`;
# x.elementChildren() ⇒ <play>Hamlet</play><novel>Macbeth</novel>
# 
# x.elementChildren("novel") ⇒ <novel>Macbeth</novel>
# ```
# 
# + x - the xml value
# + nm - the expanded name of the elements to be selected, or `()` for all elements
# + return - an xml sequence consisting of child elements of elements in parameter `x`; if parameter `nm`
#  is `()`, returns a sequence of all such elements;
#  otherwise, include only elements whose expanded name is parameter `nm`
public isolated function elementChildren(xml x, string? nm = ()) returns xml<Element> = @java:Method {
    'class: "org.ballerinalang.langlib.xml.ElementChildren",
    name: "elementChildren"
} external;

// Functional programming methods

# Applies a function to each item in an xml sequence, and returns an xml sequence of the results.
#
# Each item is represented as a singleton value.
#
# ```ballerina
# xml x = xml `<book>Hamlet</book><book>Macbeth</book>`;
# x.map(function (xml xmlContent) returns xml => 
#   xml `<book kind="play">${xmlContent.children()}</book>`
# ) ⇒ <book kind="play">Hamlet</book><book kind="play">Macbeth</book>
# ```
# 
# + x - the xml value
# + func - a function to apply to each child or `item`
# + return - new xml value containing result of applying parameter `func` to each child or `item`
public isolated function 'map(xml<ItemType> x, @isolatedParam function(ItemType item) returns XmlType func)
    returns xml<XmlType> = @java:Method {
        'class: "org.ballerinalang.langlib.xml.Map",
        name: "map"
} external;

# Applies a function to each item in an xml sequence.
#
# Each item is represented as a singleton value.
#
# ```ballerina
# xml books = xml `<book>Sherlock Holmes</book><book>Invisible Man</book>`;
# xml titles = xml ``;
# 
# books.forEach(function (xml xmlItem) {
#     titles += xml `<novel>${xmlItem.data()}</novel>`;
# });
# 
# titles ⇒ <novel>Sherlock Holmes</novel><novel>Invisible Man</novel>
# ```
# 
# + x - the xml value
# + func - a function to apply to each item in parameter `x`
public isolated function forEach(xml<ItemType> x, @isolatedParam function(ItemType item) returns () func) = @java:Method {
    'class: "org.ballerinalang.langlib.xml.ForEach",
    name: "forEach"
} external;

# Selects the items from an xml sequence for which a function returns true.
#
# Each item is represented as a singleton value.
#
# ```ballerina
# xml x = xml `<novel>Sherlock Holemes</novel><play>Hamlet</play><novel>Invisible Man</novel><play>Romeo and Juliet</play>`;
# x.filter(x => x is xml:Element && x.getName() == "play") ⇒ <play>Hamlet</play><play>Romeo and Juliet</play>
# ```
# 
# + x - xml value
# + func - a predicate to apply to each item to test whether it should be selected
# + return - new xml sequence containing items in parameter `x` for which function `func` evaluates to true
public isolated function filter(xml<ItemType> x, @isolatedParam function(ItemType item) returns boolean func)
    returns xml = @java:Method {
        'class: "org.ballerinalang.langlib.xml.Filter",
        name: "filter"
} external;

# Constructs an xml value from a string.
#
# This parses the string using the `content` production of the
# XML 1.0 Recommendation.
#
# ```ballerina
# xml:fromString("<book>Hamlet</book><book>Sherlock Holmes</book>") ⇒ <book>Hamlet</book><book>Sherlock Holmes</book>
# 
# xml:fromString("<a>b") ⇒ error
# ```
# 
# + s - a string in XML format
# + return - xml value resulting from parsing parameter `s`, or an error
public isolated function fromString(string s) returns xml|error = @java:Method {
    'class: "org.ballerinalang.langlib.xml.FromString",
    name: "fromString"
} external;

# Selects all the items in a sequence that are of type `xml:Text`.
#
# ```ballerina
# xml x = xml `John<!-- middle name --><mname>Alex</mname> Doe`;
# x.text() ⇒ John Doe
# ```
# 
# + x - the xml value
# + return - an xml sequence consisting of selected text items
public isolated function text(xml x) returns Text = @java:Method {
    'class: "org.ballerinalang.langlib.xml.Text",
    name: "text"
} external;
