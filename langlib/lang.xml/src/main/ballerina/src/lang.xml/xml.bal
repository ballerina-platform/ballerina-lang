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
public function length(xml x) returns int = external;

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
# + x - xml item to iterate
# + return - iterator object
# A character item is represented by a string of length 1.
# Other items are represented by xml singletons.
public function iterator(xml<ItemType> x) returns abstract object {
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
#
# + elem - xml element
# + return - children of `elem`
public function getChildren(Element elem) returns xml = external;

# Sets the children of `elem` to `children`.
# This panics if it would result in the element structure
# becoming cyclic.
#
# + elem - xml element
# + children - xml or string to set as children
public function setChildren(Element elem, xml|string children) = external;

# Returns the map representing the attributes of `elem`.
# This includes namespace attributes.
# The keys in the map are the expanded names of the attributes.
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
public function createElement(string name, xml children = concat())
    returns Element = external;

# Constructs an xml sequence consisting of only a processing instruction item.
#
# + target - the target part of the processing instruction to be constructed
# + content - the content part of the processing instruction to be constructed
# + return - an xml sequence consisting of a processing instruction with target `target`
#     and content `content`
public function createProcessingInstruction(string target, string content)
    returns ProcessingInstruction = external;

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
public function slice(xml<ItemType> x, int startIndex, int endIndex = x.length())
    returns xml<ItemType> = external;

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
public function elements(xml x) returns xml<Element> = external;

// Functional programming methods

# Applies a function to each item in an xml sequence, and returns an xml sequence of the results.
# Each item is represented as a singleton value.
#
# + x - the xml value
# + func - a function to apply to each child or `item`
# + return - new xml value containing result of applying `func` to each child or `item`
public function map(xml<ItemType> x, function(ItemType item) returns XmlType func)
    returns xml<XmlType> = external;

# Applies a function to each item in an xml sequence.
# Each item is represented as a singleton value.
#
# + x - the xml value
# + func - a function to apply to each item in `x`
public function forEach(xml<ItemType> x, function(ItemType item) returns () func)
    = external;

# Selects the items from an xml sequence for which a function returns true.
# Each item is represented as a singleton value.
#
# + x - xml value
# + func - a predicate to apply to each item to test whether it should be selected
# + return - new xml sequence containing items in `x` for which `func` evaluates to true
public function filter(xml<ItemType> x, function(ItemType item) returns boolean func)
    returns xml = external;

# Constructs an xml value from a string.
# This parses the string using the `content` production of the
# XML 1.0 Recommendation.
#
# + s - a string in XML format
# + return - xml value resulting from parsing `s`, or an error
public function fromString(string s) returns xml|error = external;