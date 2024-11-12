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

function testXmlMemberAccessOnXmlNegative() {
    xml seq = xml `text item<elem>element item</elem><!--comment item--><?pi item?>`;

    xml:Element _ = seq[0];
    xml:Element|xml:Comment|xml:ProcessingInstruction _ = seq[1];
    xml:Comment|xml:ProcessingInstruction|xml:Text _ = seq[2];
}

function testXmlMemberAccessOnXmlElementSequencesNegative() {
    xml<xml:Element> seq = xml `<e1>element 1</e1><e2>element 2</e2>`;
    xml:Element _ = seq[0];
    xml<never> _ = seq[1];
}

function testXmlMemberAccessOnXmlCommentSequencesNegative() {
    xml<xml:Comment> seq = xml `<!--Comment 1--><!--Comment 2--><!--Comment 3-->`;
    xml:Comment _ = seq[0];
    xml<never> _ = seq[2];
}

function testXmlMemberAccessOnXmlTextSequencesNegative() {
    xml<xml:Text> seq = xml `baz`;
    xml<never> _ = seq[0];
}

function testXmlMemberAccessOnXmlProcessingInstructionSequencesNegative() {
    xml<xml:ProcessingInstruction> seq = xml `<?p1 v1?><?p2 v2?>`;
    xml:ProcessingInstruction _ = seq[0];
    xml<never> _ = seq[2];
}

function testXmlMemberAccessOnXmlSingletonsNegative() {
    xml:Element elem = xml `<elem>element value</elem>`;
    xml:Element _ = elem[0];

    xml:Text text = xml `text value`;
    xml<never> _ = text[0];

    xml:Comment comment = xml `<!--comment value-->`;
    xml:Comment _ = comment[0];

    xml:ProcessingInstruction pi = xml `<?pi value?>`;
    xml:ProcessingInstruction _ = pi[0];
}

function testXmlMemberAccessOnXmlUnionsNegative() {
    xml<xml:Element>|xml<xml:Comment> seq = <xml<xml:Element>> xml `<foo/><bar>val</bar>`;
    xml:Element|xml:Comment _ = seq[0];
    xml:Element|xml<never> _ = seq[1];

    xml<xml:Comment>|xml:ProcessingInstruction|xml<never> seq2 = xml `<?pi val?>`;
    xml:Comment|xml<never> _ = seq2[0];
}

function testXmlMemberAccessOnXmlUnionSequencesNegative() {
    xml<xml:Element|xml:Comment> seq = xml `<foo/><!--comment val--><bar>val</bar>`;
    xml:Element|xml:Comment _ = seq[0];
    xml:Comment|xml<never> _ = seq[1];

    xml<xml:Comment|xml:ProcessingInstruction|never> seq2 = xml `<?pi val?>`;
    xml:Comment|xml<never> _ = seq2[0];
}

function testXmlMemberAccessOnEmptySequenceTypeNegative() {
    xml<never> seq = xml ``;
    xml:Element _ = seq[0];
}
