// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/lang.'xml as xmllib;

function getNameOfElementNegative() returns string {
    xml seq = xmllib:concat(xml `<elem>a</elem>`, xml `<elem>a</elem>`);
    return seq.getName();
}

function testSetElementNameNegative() returns xml {
    xml elem = xml `<elem attr="attr1">content</elem>`;
    xml elemN = xml `<elemN></elemN>`;
    xml seq = xmllib:concat(elem, elemN);
    seq.setName("el2");
    return seq;
}

function testGetChildrenNegative() returns xml {
     xml elem = xml `<elem attr="attr1">content</elem>`;
     xml elemN = xml `<elemN></elemN>`;
     xml seq = xmllib:concat(elem, elemN);
     return seq.getChildren();
}

function testSetChildrenNegative() {
    xml child = xml `<e>child</e>`;
    xml elem = xml `<elem attr="attr1">content</elem>`;
    xml elemN = xml `<elemN></elemN>`;
    xml seq = xmllib:concat(elem, elemN);
    seq.setChildren(child);
}

function testGetAttributesNegative() returns map<string> {
    xml elem = xml `<elem attr="attr1">content</elem>`;
    xml elemN = xml `<elemN></elemN>`;
    xml seq = xmllib:concat(elem, elemN);
    return seq.getAttributes();
}

function testGetTargetNegative() {
   xml elm = xml `<elm>e</elm>`;
   _ = elm.getTarget();
}

function testGetContentNegative() returns string {
    xmllib:Element element = <xmllib:Element> xml `<elem attr="attr1">content</elem>`;
    return element.getContent();
}