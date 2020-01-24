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

public const XML_ELEMENT = "element";
public const XML_SEQUENCE = "sequence";
public const XML_TEXT = "text";
public const XML_COMMENT = "comment";
public const XML_PI = "pi";
public type XMLType XML_ELEMENT | XML_SEQUENCE | XML_TEXT | XML_COMMENT | XML_PI;

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
public function getItemType(xml x) returns XMLType = external;

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
function selectDescendants(xml x, string qname) returns xml = external;

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
