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

public function basicXMLConstrainedType() {
    'xml:Element elem1 = xml `<hello>xml content</hello>`;
    xml<'xml:Element> elementSequence = xml `<hello>xml content</hello>`;

    'xml:Element refer = <'xml:Element> elementSequence[0];
     xml two = elementSequence[1]; // returns an empty sequence

    assert(elementSequence.length(), 1);
    assert((elementSequence[0]/*).toString(),"xml content");

    'xml:Comment comment = xml `<!-- this is a comment text -->`;
    xml<'xml:Comment> commentSequence = comment;
    assert(commentSequence.length(), 1);
    assert((commentSequence[0]).toString(),"<!-- this is a comment text -->");

}

public function xmlConstraintMultipleElement() {
    'xml:Element elem1 = xml `<hello>xml element 1</hello>`;
    'xml:Element elem2 = xml `<hello>xml element 2</hello>`;
    xml<'xml:Element> elementSequence = elem1 + elem2;
    assert(elementSequence.length(), 2);
    assert((elementSequence[0]/*).toString(),"xml element 1");
    assert((elementSequence[1]/*).toString(),"xml element 2");

    'xml:Comment comment = xml `<!-- this is a comment text -->`;
    'xml:ProcessingInstruction pi = xml `<?xml-stylesheet href="mystyle.css" type="text/css"?>`;
     xml commentPISeq = comment + pi;
     assert(commentPISeq.length(), 2);
     assert((commentPISeq[0]).toString(),"<!-- this is a comment text -->");
     assert((commentPISeq[1]).toString(),"<?xml-stylesheet href=\"mystyle.css\" type=\"text/css\"?>");
}

public function xmlConstraintRuntimeCast() {
    'xml:Element elem1 = xml `<hello>xml element 1</hello>`;
    'xml:Element elem2 = xml `<hello>xml element 2</hello>`;
    xml<'xml:Element> elementSequence = <xml<'xml:Element>> 'xml:concat(elem1, elem2);
    assert(elementSequence.length(), 2);
    assert((elementSequence[0]/*).toString(),"xml element 1");
    assert((elementSequence[1]/*).toString(),"xml element 2");

    'xml:Comment comment = xml `<!-- this is a comment text -->`;
    xml<'xml:Element|'xml:Comment> seq = <xml<'xml:Element|'xml:Comment>> 'xml:concat(elem1, comment);
    assert(elementSequence.length(), 2);
    assert((seq[0]/*).toString(),"xml element 1");
    assert((seq[1]).toString(),"<!-- this is a comment text -->");
}

public function xmlCastSingleElementAsConstrainedSequence() {
    string elemText = "<hello>xml element 1</hello>";
    xml<'xml:Element> elementSequence = <xml<'xml:Element>> checkpanic 'xml:fromString(elemText);
    assert(elementSequence.length(), 1);
    assert((elementSequence[0]/*).toString(),"xml element 1");
}

public function xmlConstraintRuntimeCastInvalid() {
    'xml:Element elem1 = xml `<hello>xml element 1</hello>`;
    'xml:Element elem2 = xml `<hello>xml element 2</hello>`;
    xml<'xml:Comment> elementSequence = <xml<'xml:Comment>> 'xml:concat(elem1, elem2);
}

public function xmlConstraintRuntimeCastUnionInvalid() {
    'xml:Element elem1 = xml `<hello>xml element 1</hello>`;
    'xml:Comment comment = xml `<!-- this is a comment text -->`;
    xml<'xml:Element|'xml:Text> seq = <xml<'xml:Element|'xml:Text>> 'xml:concat(elem1, comment);
}

public function xmlElementToConstraintClassInvalid() {
    xml<'xml:Comment> c = xml `<!-- comment -->`;
    anydata a = c;
    xml<'xml:Element> p = <xml<'xml:Element>> a;
}

public function xmlSubtypeArray() {
    xml[] ar = [xml `<elem>hello</elem>`];
    ar.push(xml `<!-- cmnt -->`);
    ar.push(xml `<?Hello DATA ?>`);

    assert(ar.length(), 3);
    assert(ar[0] is 'xml:Element, true);
    assert((ar[0]/*).toString(), "hello");
    assert(ar[1] is 'xml:Comment, true);
    assert((ar[1]).toString(),"<!-- cmnt -->");

    'xml:Element elem1 = <'xml:Element> ar[0];
    assert((elem1/*).toString(), "hello");
    xml<'xml:Comment> comSeq = <xml<'xml:Comment>> ar[1];
    assert((comSeq[0]).toString(),"<!-- cmnt -->");
}

public function xmlSubtypeArrayTwo() {
    'xml:Element[] ar = [xml `<elem>hello</elem>`];
    ar.push(xml `<elem>second elem</elem>`);
    assert(ar.length(), 2);
    assert((ar[0]/*).toString(), "hello");

    'xml:Element elem1 = ar[0];
    assert((elem1/*).toString(), "hello");
    xml<'xml:Element> elemSeq = ar[1];
    assert((elemSeq[0]/*).toString(), "second elem");

    xml<'xml:Element>[] arrayOfElementSeq = [];
    'xml:Element elementA  = xml `<elem>AAA</elem>`;
    xml<'xml:Element> elementSequence = xml `<hello>BBB</hello>`;
    arrayOfElementSeq.push(elementA);
    arrayOfElementSeq.push(elementSequence);
    assert(arrayOfElementSeq.length(), 2);
    assert((arrayOfElementSeq[0]/*).toString(), "AAA");
    assert((arrayOfElementSeq[1]/*).toString(), "BBB");
}

public function xmlSubtypeMap() {
   'xml:Comment comment = xml `<!-- cmnt -->`;
   xml<'xml:Element> singleElementSequence = xml `<hello>SSS</hello>`;
   map<xml> mp = {
       "element1" : xml `<elem>element1</elem>`,
       "sequnce1" : <'xml:Element> singleElementSequence
   };
   mp["comment"] = comment;
   assert(mp.length(), 3);
   assert((mp.get("element1")/*).toString(), "element1");
   assert((mp.get("sequnce1")/*).toString(), "SSS");
   assert((mp.get("comment")).toString(), "<!-- cmnt -->");

   map<'xml:Element> eMap = {
       "elementA" : xml `<hello>AAA</hello>`
   };
   'xml:Element elementB = xml `<hello>BBB</hello>`;
   eMap["elementB"] = elementB;
   eMap["elementS"] = <'xml:Element> singleElementSequence;
   assert(eMap.length(), 3);
   assert((eMap.get("elementA")/*).toString(), "AAA");
   assert((eMap.get("elementB")/*).toString(), "BBB");
   assert((eMap.get("elementS")/*).toString(), "SSS");

   map<xml<'xml:Element>> eElemSeqMap = {
       "seq1" : singleElementSequence
   };
   eElemSeqMap["seq2"] = elementB;
   assert(eElemSeqMap.length(), 2);
   assert((eElemSeqMap.get("seq1")/*).toString(), "SSS");
   assert((eElemSeqMap.get("seq2")/*).toString(), "BBB");
}

function assert(anydata actual, anydata expected) {
    if (expected != actual) {
        typedesc<anydata> expT = typeof expected;
        typedesc<anydata> actT = typeof actual;
        string reason = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
        error e = error(reason);
        panic e;
    }
}
