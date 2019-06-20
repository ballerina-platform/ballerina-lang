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

xml globalXML = xml `<test><name>ballerina</name></test>`;

function testXML() returns [xml, any, any, xml, xml, xml] {
    xmlns "http://wso2.com/" as ns0;

    xml x = xml `<ns0:foo a="hello world" xmlns:ns1="http://ballerinalang.org/"><ns1:bar1>hello1</ns1:bar1><bar2>hello2</bar2></ns0:foo>`;

    x@[ns0:b] = "active";
    xml x2 = x["{http://ballerinalang.org/}bar1"];
    return [x, x@, x@[ns0:b], x.*, x2, x2[0]];
}

function testFieldBasedAccess() returns [xml, xml, xml, xml, xml, xml] {
    xml x1 = xml `<name1><fname><foo>1</foo><bar>2</bar></fname><lname1><foo>3</foo><bar>4</bar></lname1></name1>`;
    xml x2 = xml `<name2><fname><foo>5</foo><bar>6</bar></fname><lname2><foo>7</foo><bar>8</bar></lname2></name2>`;
    xml x3 = x1 + x2 + xml `<foo>apple</foo>`;

    xml x4 = x3.*;
    xml x5 = x1["fname"];
    xml x6 = x3.fname.foo[1];
    xml x7 = x3.fname[1].foo;
    xml x8 = x3.*.bar[1];
    xml x9 = x3.*.*;

    return [x4, x5, x6, x7, x8, x9];
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
