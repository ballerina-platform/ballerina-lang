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

import ballerina/test;

// An XML value represents an immutable sequence of zero or more of the items that can
// occur inside an XML element, specifically:
// ● elements
// ● characters
// ● processing instructions
// ● comments
@test:Config {}
function testXmlValue() {
    xml xmlElement = xml `<book>The Lost World</book>`;
    xml xmlCharacters = xml `Hello, world!`;
    xml xmlComment = xml `<!--I am a comment-->`;
    xml xmlPI = xml `<?target data?>`;

    xml xmlSequence1 = xmlElement + xmlCharacters + xmlComment + xmlPI;
    xml xmlSequence2 = xmlSequence1 + xmlElement;
    test:assertFalse(xmlSequence1 === xmlSequence2, msg = "expected original value to remain immutable");
}

// The attributes of an element are represented by a map<string>.
@test:Config {}
function testXmlAttributes() {
    xml x1 = xml `<book>The Lost World</book>`;
    map<string> attributeMap = map<string>.convert(x1@);
    test:assertEquals(attributeMap.length(), 0, msg = "expected attribute count to be zero");

    xml x2 = xml `<book status="available" count="5"/>`;
    attributeMap = map<string>.convert(x2@);
    test:assertEquals(attributeMap.length(), 2, msg = "expected attribute count to be zero");
    test:assertEquals(attributeMap.status, "available", msg = "expected value to be \"available\"");
    test:assertEquals(attributeMap.count, "5", msg = "expected value to be \"5\"");
}

// The content of each element in the sequence is itself a distinct XML value.
// Although the sequence is immutable, an element can be mutated to change its content to be another XML value.
@test:Config {}
function testXmlSequenceElements() {
    xml xmlElement1 = xml `<book status="available">The Lost World</book>`;
    xml xmlElement2 = xml `<author>Arthur Conan Doyle</author>`;
    xml xmlCharacters = xml `Book Store`;

    xml xmlSequence1 = xmlElement1 + xmlCharacters;

    xml xmlElement3 = xmlElement1;
    xmlElement1.appendChildren(xmlElement2);
    test:assertTrue(xmlElement1 === xmlElement3, msg = "expected the value itself to be mutated");

    xml xmlSequence2 = xmlElement1 + xmlCharacters;
    test:assertTrue(xmlSequence1 == xmlSequence2, msg = "expected updates to element to be reflected in the sequence");
}

// An XML value is iterable as a sequence of its items, where each character item is
// represented by a string with a single code point and other items are represented by a
// singleton XML value.
@test:Config {}
function testXmlIteration() {
    xml xmlElement = xml `<book>The Lost World</book>`;
    xml xmlComment = xml `<!--I am a comment-->`;
    xml xmlPI = xml `<?target data?>`;

    anydata[] arr = [];
    int index = 0;

    foreach xml|string item in xmlElement {
        arr[index] = item;
        index += 1;
    }
    test:assertEquals(arr.length(), 1, msg = "expected iteration to result in one element");
    test:assertEquals(arr[0], xmlElement, msg = "expected iteration to produce the same element");

    arr = [];
    index = 0;

    xml xmlSequence = xmlElement + xmlComment + xmlPI;
    foreach xml|string item in xmlSequence {
        arr[index] = item;
        index += 1;
    }
    test:assertEquals(arr.length(), 3, msg = "expected iteration to result in three elements");
    test:assertEquals(arr[0], xmlElement, msg = "expected iteration to produce the same element");
    test:assertEquals(arr[1], xmlComment, msg = "expected iteration to produce the same element");
    test:assertEquals(arr[2], xmlPI, msg = "expected iteration to produce the same element");
}

// A single XML item, such as an element, is represented by a sequence consisting of just that item.
@test:Config {}
function testXmlSingleton() {
    xml x1 = xml `<book>The Lost World</book>`;
    test:assertTrue(x1.isSingleton(), msg = "expected xml to be a singleton");

    xml x2 = xml `<author>Doyle</author>`;
    xml x3 = x1 + x2;
    test:assertFalse(x3.isSingleton(), msg = "expected xml to not be a singleton");

    xml x4 = x3.select("book");
    test:assertTrue(x4.isSingleton(), msg = "expected xml to be a singleton");
}
