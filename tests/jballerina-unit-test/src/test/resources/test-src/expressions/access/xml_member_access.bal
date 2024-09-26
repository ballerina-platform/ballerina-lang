// Copyright (c) 2024 WSO2 LLC. (http://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

function testXmlMemberAccessOnXml() {
    xml seq = xml `text item<elem>element item</elem><!--comment item--><?pi item?>`;

    xml m1 = seq[0];
    assertTrue(m1 is xml:Text);
    assertEquality(xml `text item`, m1);

    xml:Element|xml:Comment|xml:ProcessingInstruction|xml:Text m2 = seq[1];
    assertTrue(m2 is xml:Element);
    assertEquality(xml `<elem>element item</elem>`, m2);

    xml m3 = seq[2];
    assertTrue(m3 is xml:Comment);
    assertEquality(xml `<!--comment item-->`, m3);

    xml m4 = seq[3];
    assertTrue(m4 is xml:ProcessingInstruction);
    assertEquality(xml `<?pi item?>`, m4);

    xml m5 = seq[4];
    assertTrue(m5 is xml<never>);
}

function testXmlMemberAccessOnXmlElementSequences() {
    xml<xml:Element> seq = xml `<e1>element 1</e1><e2>element 2</e2>`;

    xml:Element|xml<never> m1 = seq[0];
    assertTrue(m1 is xml:Element);
    assertEquality(xml `<e1>element 1</e1>`, m1);

    xml:Element|xml<never> m2 = seq[1];
    assertTrue(m2 is xml:Element);
    assertEquality(xml `<e2>element 2</e2>`, m2);

    xml:Element|xml<never> m3 = seq[5];
    assertTrue(m3 is xml<never>);

    var fn = function () {
        xml:Element|xml<never> _ = seq[-5];
        panic error("expected member access to panic");
    };

    error? fnRes = trap fn();
    assertTrue(fnRes is error);
    error err = <error> fnRes;
    assertEquality("xml sequence index out of range. Length: '2' requested: '-5'", err.message());
}

function testXmlMemberAccessOnXmlCommentSequences() {
    xml<xml:Comment> seq = xml `<!--Comment 1--><!--Comment 2--><!--Comment 3-->`;

    xml:Comment|xml<never> m1 = seq[0];
    assertTrue(m1 is xml:Comment);
    assertEquality(xml `<!--Comment 1-->`, m1);

    xml:Comment|xml<never> m2 = seq[2];
    assertTrue(m2 is xml:Comment);
    assertEquality(xml `<!--Comment 3-->`, m2);

    xml:Comment|xml<never> m3 = seq[5];
    assertTrue(m3 is xml<never>);

    var fn = function () {
        xml:Comment|xml<never> _ = seq[-1];
        panic error("expected member access to panic");
    };

    error? fnRes = trap fn();
    assertTrue(fnRes is error);
    error err = <error> fnRes;
    assertEquality("xml sequence index out of range. Length: '3' requested: '-1'", err.message());
}

function testXmlMemberAccessOnXmlTextSequences() {
    xml<xml:Text> seq = xml `baz`;

    xml:Text|xml<never> m1 = seq[0];
    assertTrue(m1 !is xml<never>);
    assertEquality(xml `baz`, m1);

    xml:Text|xml<never> m3 = seq[2];
    assertTrue(m3 is xml<never>);

    var fn = function () {
        xml:Text|xml<never> _ = seq[-1];
        panic error("expected member access to panic");
    };

    error? fnRes = trap fn();
    assertTrue(fnRes is error);
    error err = <error> fnRes;
    assertEquality("xml sequence index out of range. Length: '1' requested: '-1'", err.message());
}

function testXmlMemberAccessOnXmlProcessingInstructionSequences() {
    xml<xml:ProcessingInstruction> seq = xml `<?p1 v1?><?p2 v2?>`;

    xml:ProcessingInstruction|xml<never> m1 = seq[0];
    assertTrue(m1 is xml:ProcessingInstruction);
    assertEquality(xml `<?p1 v1?>`, m1);

    xml:ProcessingInstruction|xml<never> m3 = seq[2];
    assertTrue(m3 is xml<never>);

    var fn = function () {
        xml:ProcessingInstruction|xml<never> _ = seq[-1];
        panic error("expected member access to panic");
    };

    error? fnRes = trap fn();
    assertTrue(fnRes is error);
    error err = <error> fnRes;
    assertEquality("xml sequence index out of range. Length: '2' requested: '-1'", err.message());
}

function testXmlMemberAccessOnXmlSingletons() {
    xml:Element elem = xml `<elem>element value</elem>`;
    xml:Element|xml<never> m1 = elem[0];
    assertTrue(m1 is xml:Element);
    assertEquality(elem, m1);
    assertTrue(elem[3] is xml<never>);

    xml:Text text = xml `text value`;
    xml:Text m2 = text[0];
    assertTrue(m2 !is xml<never>);
    assertEquality(text, m2);
    assertTrue(text[3] is xml<never>);

    xml:Comment comment = xml `<!--comment value-->`;
    xml:Comment|xml<never> m3 = comment[0];
    assertTrue(m3 is xml:Comment);
    assertEquality(comment, m3);
    assertTrue(comment[2] is xml<never>);

    xml:ProcessingInstruction pi = xml `<?pi value?>`;
    xml:ProcessingInstruction|xml<never> m4 = pi[0];
    assertTrue(m4 is xml:ProcessingInstruction);
    assertEquality(pi, m4);
    assertTrue(pi[4] is xml<never>);
}

function testXmlMemberAccessOnXmlUnions() {
    xml<xml:Element>|xml<xml:Comment> seq = <xml<xml:Element>> xml `<foo/><bar>val</bar>`;
    xml:Element|xml:Comment|xml<never> m1 = seq[0];
    assertTrue(m1 is xml:Element);
    assertEquality(xml `<foo/>`, m1);

    xml:Element|xml:Comment|xml<never> m2 = seq[1];
    assertTrue(m2 is xml:Element);
    assertEquality(xml `<bar>val</bar>`, m2);

    assertTrue(seq[4] is xml<never>);

    xml<xml:Comment>|xml:ProcessingInstruction|xml<never> seq2 = xml `<?pi val?>`;
    xml:Comment|xml:ProcessingInstruction|xml<never> m3 = seq2[0];
    assertTrue(m3 is xml:ProcessingInstruction);
    assertEquality(xml `<?pi val?>`, m3);

    assertTrue(seq2[1] is xml<never>);
}

function testXmlMemberAccessOnXmlUnionSequences() {
    xml<xml:Element|xml:Comment> seq = xml `<foo/><!--comment val--><bar>val</bar>`;
    xml:Element|xml:Comment|xml<never> m1 = seq[0];
    assertTrue(m1 is xml:Element);
    assertEquality(xml `<foo/>`, m1);

    xml:Element|xml:Comment|xml<never> m2 = seq[1];
    assertTrue(m2 is xml:Comment);
    assertEquality(xml `<!--comment val-->`, m2);

    assertEquality(xml `<bar>val</bar>`, seq[2]);

    assertTrue(seq[4] is xml<never>);

    xml<xml:Comment|xml:ProcessingInstruction|never> seq2 = xml `<?pi val?>`;
    xml:Comment|xml:ProcessingInstruction|xml<never> m3 = seq2[0];
    assertTrue(m3 is xml:ProcessingInstruction);
    assertEquality(xml `<?pi val?>`, m3);

    assertTrue(seq2[1] is xml<never>);
}

function testXmlMemberAccessOnEmptySequenceType() {
    xml<never> seq = xml ``;
    xml<never> val = seq[0];
    assertEquality(xml ``, val);
}

function assertTrue(anydata actual) {
    assertEquality(true, actual);
}

function assertEquality(anydata expected, anydata actual) {
    if expected != actual {
        panic error(string `Expected ${expected.toBalString()}, found ${actual.toBalString()}`);
    }
}
