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

# Returns the content of a text or processing instruction or comment item.
#
# + x - xml item
# + return - the content of `x`
public function getContent(Text|ProcessingInstruction|Comment x) returns string = external;

# Returns a subsequence of an xml value.
#
# + x - the xml value
# + startIndex - start index, inclusive
# + endIndex - end index, exclusive
# + return - a subsequence of `x` consisting of items with index >= startIndex and < endIndex
public function slice(xml x, int startIndex, int endIndex = x.length()) returns xml = external;

# Selects the elements from an xml value.
#
# + x - the xml value
# + return - an xml sequence consisting of all the element items in `x`
public function elements(xml x) returns xml = external;
# Returns number of xml items in `x`.
#
# + x - xml item
# + return - number of XML items in `x`
public function length(xml x) returns int = external;

# Concatenation of all text and text descendants
# as a single string (same as XPath).
# So xml`this is an <b>important</b> point`.stringValue() ==
# "this is an important point"
# stringValue(x) is defined as follows
# - if x is the empty sequence, the empty string
# - if x is a comment or PI item, the empty string
# - if x is a text item, a string with the item's characters
# - if x is an element item, the stringValue of the children
# - if x is a concatenation of x1 and x2,
#   then stringValue(x1) + stringValue(x2)
#
# This provides a way to convert xml:Text to string
# + x - The xml source
# + return - Text value of the xml
public function stringValue(xml x) returns string = external;

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
