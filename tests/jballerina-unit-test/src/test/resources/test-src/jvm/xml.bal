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
import ballerina/lang.'xml as xmllib;

xml globalXML = xml `<test><name>ballerina</name></test>`;

function testXML() returns [xml, map<string>, (string|error), xml] {
    xmlns "http://wso2.com/" as ns0;

    xmllib:Element x = <xmllib:Element> xml `<ns0:foo a="hello world" xmlns:ns1="http://ballerinalang.org/"><ns1:bar1>hello1</ns1:bar1><bar2>hello2</bar2></ns0:foo>`;
    map<string> attrMap = x.getAttributes();
    attrMap[ns0:b] = "active";
    return [x, x.getAttributes(), x.ns0:b, x/*];
}

function testXmlNavigation() returns [xml, xml, xml, xml, xml, xml, xml, xml] {
    xml x1 = xml `<name1><fname><foo>1</foo><bar>2</bar></fname><lname1><foo>3</foo><bar>4</bar></lname1></name1>`;
    xml x2 = xml `<name2><fname><foo>5</foo><bar>6</bar></fname><lname2><foo>7</foo><bar>8</bar></lname2></name2>`;
    xml x3 = x1 + x2 + xml `<foo>apple</foo>`;

    xml x4 = x3/*;
    xml x5 = x1/<fname>;
    xml x6 = x3/<fname>/<foo>[0];
    xml x7 = x3/<fname>/<foo>[1];
    xml x8 = x3/<fname>[1];
    xml x9 = x3/<fname>[0]/<foo>;
    xml x10 = x3/*/<bar>[0];
    xml x11 = x3/*/*;

    return [x4, x5, x6, x7, x8, x9, x10, x11];
}

function testDollarSignOnXMLLiteralTemplate() returns [xml, xml, xml] {
    string a = "hello";
    xml x1 = xml `<foo id="hello $${ 3 + 6 / 3}" >${a}</foo>`;
    xml x2 = xml `<foo id="hello $$${ 3 + 6 / 3}" >$${a}</foo>`;
    xml x3 = xml `<foo id="hello $$ ${ 3 + 6 / 3}" >$$ ${a}</foo>`;

    return [x1, x2, x3];
}

function testGetGlobalXML() returns xml {
    return globalXML;
}

function testXMLWithNumericEscapes() returns xml[10] {
    xml x1 = xml `\u{61}`;
    xml x2 = xml `\u{61}ppl\\u{65}`;
    xml x3 = xml `A \u{5C} B`;
    xml x4 = xml `A \\u{5C} B`;
    xml x5 = xml `"\u{D800}"`;
    xml x6 = xml `\u{DFFF}"\u{DFFF}"`;
    xml x7 = xml `"\u{12FFFF} ABC \u{DFFF} DEF \u{DAFF}"`;
    xml x8 = xml `\u{12FFFF} ABC \u{DFFFAAA} DEF \u{FFFFFFF}`;
    xml x9 = xml `\u{001B[`;
    xml x10 = xml `"\u{001B["`;

    return [x1, x2, x3, x4, x5, x6, x7, x8, x9, x10];
}
