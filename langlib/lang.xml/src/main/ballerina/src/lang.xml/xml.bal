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

const string XML_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";
const string XMLNS_NAMESPACE_URI = "http://www.w3.org/2000/xmlns/";

# Returns number of xml items in `x`.
#
# + x - xml item
# + return - number of XML items in `x`
public function length(xml x) returns int = external;

# Returns an iterator over the xml items of `x`
#
# + x - xml item to iterate
# + return - iterator object
# A character item is represented by a string of length 1.
# Other items are represented by xml singletons.
public function iterator(xml x) returns abstract object {
    public function next() returns record {| (xml|string) value; |}?;
} {
    XMLIterator xmlIterator = new(x);
    return xmlIterator;
}

# Concatenates xml and string values.
#
# + xs - xml or string items to concatenate
# + return - an xml sequence that is the concatenation of all the `xs`;
#    an empty xml sequence if the `xs` are empty
public function concat((xml|string)... xs) returns xml = external;

# Tests whether an xml value is a singleton consisting of only an element item.
#
# + x - xml value
# + return - true if `x` consists of only an element item; false otherwise
public function isElement(xml x) returns boolean = external;

# Tests whether an xml value is a singleton consisting of only a processing instruction item.
#
# + x - xml value
# + return - true if `x` consists of only a processing instruction item; false otherwise
public function isProcessingInstruction(xml x) returns boolean = external;

# Tests whether an xml value is a singleton consisting of only a comment item.
#
# + x - xml value
# + return - true if `x` consists of only a comment item; false other
public function isComment(xml x) returns boolean = external;

# Tests whether an xml sequence consists of zero or more character items.
#
# + x - xml value
# + return - true if `x` is a sequence containing only character items; false otherwise
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

# Change the name of element `elem` to `xName`.
#
# + elem - xml element
# + xName - new expanded name
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
# The keys in the map are the expanded names of the attributes.
# Panics if `isElement(elem)` is not true.
#
# + x - xml element
# + return - attributes of `x`
public function getAttributes(Element x) returns map<string> = external;

# Returns the target part of the processing instruction.
#
# + x - xml processing instruction item
# + return - target part of `x`
public function getTarget(ProcessingInstruction x) returns string = external;

# Returns the content of a text or processing instruction or comment item.
#
# + x - xml item
# + return - the content of `x`
public function getContent(Text|ProcessingInstruction|Comment x) returns string = external;

# Constructs an xml sequence consisting of only a new element item.
#
# + name - the name of the new element
# + children - the children of the new element
# + return - an xml sequence consisting of only a new xml element with name `name`,
#   no attributes, and children `children`
public function createElement(string name, xml children = concat()) returns Element = external;

# Constructs an xml sequence consisting of only a processing instruction item.
#
# + target - the target part of the processing instruction to be constructed
# + content - the content part of the processing instruction to be constructed
# + return - an xml sequence consisting of a processing instruction with target `target`
#     and content `content`
public function createProcessingInstruction(string target, string content) returns ProcessingInstruction
 = external;

# Constructs an xml sequence consisting of only a comment item.
#
# + content - the content of the comment to be constructed.
# + return - an xml sequence consisting of a comment with content `content`
public function createComment(string content) returns Comment = external;

# Returns a subsequence of an xml value.
#
# + x - the xml value
# + startIndex - start index, inclusive
# + endIndex - end index, exclusive
# + return - a subsequence of `x` consisting of items with index >= startIndex and < endIndex
public function slice(xml x, int startIndex, int endIndex = x.length()) returns xml = external;

# Strips the insignificant parts of the an xml value.
# Comment items, processing instruction items are considered insignificant.
# After removal of comments and processing instructions, the text is grouped into
# the biggest possible chunks (i.e. only elements cause division into multiple chunks)
# and a chunk is considered insignificant if the entire chunk is whitespace.
#
# + x - the xml value
# + return - `x` with insignificant parts removed
public function strip(xml x) returns xml = external;

# Selects the elements from an xml value.
#
# + x - the xml value
# + return - an xml sequence consisting of all the element items in `x`
public function elements(xml x) returns xml = external;

// Functional programming methods

# Applies a function to each item in an xml sequence, and returns an xml sequence of the results.
# This represents each item in the same way as the `iterator` function.
#
# + x - the xml value
# + func - a function to apply to each child or `item`
# + return - new xml value containing result of applying paramter `func` to each child or `item`
public function map(xml x, function(xml|string item) returns xml|string func) returns xml = external;

# Applies a function to each item in an xml sequence.
# This represents each item in the same way as the `iterator` function.
#
# + x - the xml value
# + func - a function to apply to each item in `x`
public function forEach(xml x, function(xml|string item) returns () func) = external;

# Selects the items from an xml sequence for which a function returns true.
# This represents each item in the same way as the `iterator` function.
#
# + x - xml value
# + func - a predicate to apply to each item to test whether it should be selected
# + return - new xml sequence containing items in `x` for which `func` evaluates to true
public function filter(xml x, function(xml|string item) returns boolean func) returns xml = external;

# Constructs an xml value from a string.
# This parses the string using the `content` production of the
# XML 1.0 Recommendation.
#
# + s - a string in XML format
# + return - xml value resulting from parsing `s`, or an error
public function fromString(string s) returns xml|error = external;
