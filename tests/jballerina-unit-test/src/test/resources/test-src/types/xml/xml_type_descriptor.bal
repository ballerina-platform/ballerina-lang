// Copyright (c) 2024 WSO2 Inc. (http://www.wso2.org).
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

type R readonly;
type N xml<never>;
type T xml:Text;
type RN readonly & N;
type RO_E xml:Element & readonly;
type RO_C xml:Comment & readonly;
type RO_P xml:ProcessingInstruction & readonly;

type RO_XML xml<N|RO_E|RO_C|RO_P|T>;

function testXmlTypeDescriptorWithTypeParameter() {
    RO_XML a = xml `<foo/>`;
    anydata b = a;
    assertTrue(b is readonly & xml:Element);
    assertTrue(b is RO_XML);
    assertEquals(xml `<foo/>`, b);

    a = xml `text`;
    b = a;
    assertTrue(b is xml:Text);
    assertTrue(b is RO_XML);
    assertEquals(xml `text`, b);

    xml<readonly & xml:Element> c = xml `<bar/>`;
    anydata d = c;
    assertTrue(d is xml:Element);
    assertTrue(d is RO_XML);
    assertEquals(xml `<bar/>`, d);

    xml<xml:Element> e = xml `<bar/>`;
    anydata f = e;
    assertTrue(f is xml:Element);
    assertFalse(f is RO_XML);
    assertEquals(xml `<bar/>`, f);
}

function assertTrue(boolean b) {
    assertEquals(true, b);
}

function assertFalse(boolean b) {
    assertEquals(false, b);
}

function assertEquals(anydata expected, anydata actual) {
    if expected != actual {
        panic error(string `expected ${expected.toBalString()}, found ${actual.toBalString()}`);
    }
}
