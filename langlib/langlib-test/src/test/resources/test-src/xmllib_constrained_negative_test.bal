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

import ballerina/lang.'xml;

public function invalidSubtype() {
    'xml:Comment c1 = xml `<hello>xml content</hello>`;
    'xml:ProcessingInstruction p1 = xml `<hello>another xml content</hello>`;
}

public function invalidConstraint() {
    xml<'xml:Comment> c1 = xml `<hello>xml content</hello>`;
    xml<'xml:ProcessingInstruction> p1 = xml `<hello>another xml content</hello>`;

    xml<'xml:Element> elementSequence = xml `<hello>xml content</hello>`;
    'xml:Comment refer = elementSequence[0];

    'xml:Comment comment = xml `<!-- this is a comment text -->`;
    xml<'xml:Element> commentSequence = comment;
}

public function xmlConstraintMultipleElement() {
    'xml:Element elem1 = xml `<hello>xml element 1</hello>`;
    'xml:Element elem2 = xml `<hello>xml element 2</hello>`;
    xml<'xml:Comment> elementSequence = elem1 + elem2;

    'xml:Comment comment = xml `<!-- this is a comment text -->`;
    xml<'xml:Element> seq = elem1 + comment;
}

public function xmlSubtypeArrayNegative() {
    'xml:Element[] ar = [xml `<!-- cmnt -->`];
    ar.push(xml `<?Hello DATA ?>`);
    ar.push(xml `abc`);

    xml<'xml:Comment> commentSequence = xml `<!-- cmnt -->`;
    ar.push(commentSequence);

    xml<'xml:Comment>[] arrayOfCommentSeq = [xml `<hello>xml element 1</hello>`];

    xml<'xml:Element> elementSequence = xml `<hello>xml content</hello>`;
    arrayOfCommentSeq.push(elementSequence);
}

public function xmlSubtypeMapNegative() {
    map<'xml:Element> eMap = {
       "elementA" : xml `<!-- cmnt -->`
    };
    'xml:Element CommentA = xml `<!-- cmnt -->`;
    xml<'xml:Comment> CommentSeq = xml `<!-- cmntseq -->`;
    eMap["CommentA"] = CommentA;
    eMap["seq"] = CommentSeq;
}
