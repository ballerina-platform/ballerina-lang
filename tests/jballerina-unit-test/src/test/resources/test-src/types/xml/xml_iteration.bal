import ballerina/lang.'int as intlib;

// Sample XML taken from: https://www.w3schools.com/xml/books.xml
xml bookstore = xml `<bookstore>
                        <book category="cooking">
                            <title lang="en">Everyday Italian</title>
                            <author>Giada De Laurentiis</author>
                            <year>2005</year>
                            <price>30.00</price>
                        </book>
                        <book category="children">
                            <title lang="en">Harry Potter</title>
                            <author>J. K. Rowling</author>
                            <year>2005</year>
                            <price>29.99</price>
                        </book>
                        <book category="web">
                            <title lang="en">XQuery Kick Start</title>
                            <author>James McGovern</author>
                            <author>Per Bothner</author>
                            <author>Kurt Cagle</author>
                            <author>James Linn</author>
                            <author>Vaidyanathan Nagarajan</author>
                            <year>2003</year>
                            <price>49.99</price>
                        </book>
                        <book category="web" cover="paperback">
                            <title lang="en">Learning XML</title>
                            <author>Erik T. Ray</author>
                            <year>2003</year>
                            <price>39.95</price>
                        </book>
                    </bookstore>`;

string[] titles = ["Everyday Italian", "Harry Potter", "XQuery Kick Start", "Learning XML"];
string[] titlesOrigin = ["<root>interpolation1 text1 text2<foo>123</foo><bar></bar></root>",
    "text2interpolation1", "<!--commentinterpolation1-->", "text3", "<?foo ?>"];

function foreachTest() {
    int i = 0;
    foreach var x in bookstore/<book> {
        assert((x/<title>/*).toString(), titles[i]);
        i += 1;
    }

    i = 0;
    string v1 = "interpolation1";
    xml seq = xml `<root>${v1} text1 text2<foo>123</foo><bar></bar></root>text2${v1}<!--comment${v1}-->text3<?foo?>`;
    foreach var x in seq {
        assert(x.toString(), titlesOrigin[i]);
        i += 1;
    }
}

function foreachOpTest() {
    int i = 0;
    (bookstore/<book>).forEach(function (xml entry) {
        assert((entry/<title>/*).toString(), titles[i]);
        i += 1;
    });

    i = 0;
    string v1 = "interpolation1";
    xml seq = xml `<root>${v1} text1 text2<foo>123</foo><bar></bar></root>text2${v1}<!--comment${v1}-->text3<?foo?>`;
    seq.forEach(function (xml entry) {
        assert(entry.toString(), titlesOrigin[i]);
        i += 1;

    });
}

function mapOpTest() {
    string authors = "<author>Giada De Laurentiis</author><author>J. K. Rowling</author><author>James McGovern</author>"+
    "<author>Per Bothner</author><author>Kurt Cagle</author><author>James Linn</author><author>Vaidyanathan Nagarajan"+
    "</author><author>Erik T. Ray</author>";
    xml titles = (bookstore/<book>).map(function (xml book) returns xml {
        return book/<author>;
    });
    assert(titles.toString(), authors);

    string v1 = "interpolation1";
    xml seq = xml `<root>${v1} text1 text2<foo>123</foo><bar></bar></root>text2${v1}<!--comment${v1}-->text3<?foo?>`;
    xml elementChildren = seq[0].map(function (xml root) returns xml {
        return root/*;
    });
    assert(elementChildren.toString(), "interpolation1 text1 text2<foo>123</foo><bar></bar>");
}

function filterOpTest() {
    xml books = (bookstore/<book>).filter(function (xml book) returns boolean {
        var result = intlib:fromString((book/<year>/*).toString());
        if (result is int) {
            return result > 2004;
        } else {
            return false;
        }
        }).map(function(xml value) returns xml {
            return value;
        });
    assert((books/<title>).toString(), "<title lang=\"en\">Everyday Italian</title><title lang=\"en\">Harry Potter</title>");
    assert((books/<author>).toString(), "<author>Giada De Laurentiis</author><author>J. K. Rowling</author>");
    assert((books/<year>).toString(), "<year>2005</year><year>2005</year>");
    assert((books/<price>).toString(), "<price>30.00</price><price>29.99</price>");

    string v1 = "interpolation1";
    xml seq = xml `<root>${v1} text1<foo>100</foo><foo>200</foo></root>text2${v1}<!--comment${v1}--><?foo?>`;
    xml elements = seq[0];
    xml elementChildren = (elements/<foo>).filter(function (xml foo) returns boolean {
        var result = intlib:fromString((foo/*).toString());
        if (result is int) {
            return result > 100;
        } else {
            return false;
        }
        }).map(function(xml value) returns xml {
            return value;
        });
    assert(elementChildren.toString(), "<foo>200</foo>");
}

function chainedIterableOps() returns xml {
    xml authors = (bookstore/<book>).filter(function (xml book) returns boolean {
        var result = intlib:fromString((book/<year>/*).toString());
        if (result is int) {
            return result > 2004;
        } else {
            return false;
        }
        }).map(function (xml book) returns xml {
            return book/<author>;
        });
    return authors;
}

xml theXml = xml `<book>the book</book>`;
xml bitOfText = xml `bit of text\u2702\u2705`;
xml compositeXml = theXml + bitOfText;
string output = "";

xml xdata = xml `<p:person xmlns:p="foo" xmlns:q="bar">
        <p:name>bob</p:name>
        <p:address>
            <p:city>NY</p:city>
            <q:country>US</q:country>
        </p:address>
        <q:ID>1131313</q:ID>
    </p:person>`;

function concatString (string v) {
    output += v + "\n";
}

function xmlSequenceIter() returns string {
    output = "";

    foreach xml elem in compositeXml {
        concatString(elem.toString());
    }
    return output;
}

function xmlCharItemIter() returns string {
    output = "";
    int i = 0;
    foreach xml elem in bitOfText {
        concatString(elem.toString());
        i += 1;
    }
    return output;
}

function testXmlElementSequenceIteration() {
    xml el = xml `<foo>foo</foo>`;
    xml<'xml:Element> seq = <xml<'xml:Element>> el.concat(xml `<bar/>`);

    output = "";
    foreach 'xml:Element elem in seq {
        concatString(elem.toString());
    }
    assert(output, "<foo>foo</foo>\n<bar></bar>\n");

    record {| 'xml:Element value; |}? nextElement1 = seq.iterator().next();
    assert(nextElement1.toString(), "{\"value\":`<foo>foo</foo>`}");
}

function testXmlTextSequenceIteration() {
    xml<'xml:Text> txt1 = xml `bit of text\u2702\u2705`;
    'xml:Text txt2 = xml `bit of text\u2702\u2705`;

    output = "";
    foreach 'xml:Text elem in txt1 {
        concatString(elem.toString());
    }
    assert(output, "bit of text\\u2702\\u2705\n");

    output = "";
    foreach 'xml:Text elem in txt2 {
        concatString(elem.toString());
    }
    assert(output, "bit of text\\u2702\\u2705\n");

    record {| 'xml:Text value; |}? nextText1 = txt1.iterator().next();
    assert(nextText1.toString(), "{\"value\":`bit of text\\u2702\\u2705`}");

    record {| 'xml:Text value; |}? nextText2 = txt2.iterator().next();
    assert(nextText2.toString(), "{\"value\":`bit of text\\u2702\\u2705`}");
}

function testXmlCommentSequenceIteration() {
     xml<'xml:Comment> comment1 = xml `<!--I am a comment-->`;

     output = "";
     foreach 'xml:Comment elem in comment1 {
         concatString(elem.toString());
     }
     assert(output, "<!--I am a comment-->\n");

     record {| 'xml:Comment value; |}? nextComment1 = comment1.iterator().next();
     assert(nextComment1.toString(), "{\"value\":`<!--I am a comment-->`}");
}

function testXmlPISequenceIteration() {
     xml<'xml:ProcessingInstruction> pi1 = xml `<?target data?>`;

     output = "";
     foreach 'xml:ProcessingInstruction elem in pi1 {
         concatString(elem.toString());
     }
     assert(output, "<?target data?>\n");

     record {| 'xml:ProcessingInstruction value; |}? nextPI1 = pi1.iterator().next();
     assert(nextPI1.toString(), "{\"value\":`<?target data?>`}");
}

function testXmlUnionSequenceIteration() {
    xml el = xml `<foo>foo</foo>`;
    xml<'xml:Element|'xml:Text> seq = <xml<'xml:Element|'xml:Text>> el.concat(xml `<bar/>`);

    output = "";
    foreach 'xml:Element|'xml:Text elem in seq {
        concatString(elem.toString());
    }
    assert(output, "<foo>foo</foo>\n<bar></bar>\n");

    record {| 'xml:Element|'xml:Text value; |}? nextUnionXMLVal1 = seq.iterator().next();
    assert(nextUnionXMLVal1.toString(), "{\"value\":`<foo>foo</foo>`}");
}

function testXmlSequenceIteration() {
    string v1 = "interpolation1";
    xml seq = xml `<root>${v1} text1 text2<foo>123</foo><bar></bar></root>text2${v1}<!--comment${v1}-->text3<?foo?>`;
    assert('xml:length(seq).toString(), "5");

    output = "";
    foreach 'xml:Element|'xml:Text|'xml:Comment|'xml:ProcessingInstruction  elem in seq {
        concatString(elem.toString());
    }
    assert(output, "<root>interpolation1 text1 text2<foo>123</foo><bar></bar></root>\ntext2interpolation1\n"+
    "<!--commentinterpolation1-->\ntext3\n<?foo ?>\n");

    output = "";
    foreach xml  elem in seq {
            concatString(elem.toString());
    }
    assert(output, "<root>interpolation1 text1 text2<foo>123</foo><bar></bar></root>\ntext2interpolation1\n"+
        "<!--commentinterpolation1-->\ntext3\n<?foo ?>\n");

    record {| 'xml:Element|'xml:Text|'xml:Comment|'xml:ProcessingInstruction value; |}? nextXMLVal1 = seq.iterator().next();
    assert(nextXMLVal1.toString(), "{\"value\":`<root>interpolation1 text1 text2<foo>123</foo><bar></bar></root>`}");

    record {| xml value; |}? nextXMLVal2 = seq.iterator().next();
    assert(nextXMLVal2.toString(), "{\"value\":`<root>interpolation1 text1 text2<foo>123</foo><bar></bar></root>`}");
}

function xmlTypeParamCommentIter() {
    'xml:Comment comment2 = xml `<!--I am a comment-->`;
    record {| 'xml:Comment value; |}? nextComment2 = comment2.iterator().next();
    assert(nextComment2.toString(), "{\"value\":`<!--I am a comment-->`}");
}

function xmlTypeParamElementIter() {
    'xml:Element el2 = xml `<foo>foo</foo>`;
    record {| 'xml:Element value; |}? nextElement2 = el2.iterator().next();
    assert(nextElement2.toString(), "{\"value\":`<foo>foo</foo>`}");
}

function xmlTypeParamPIIter() {
    'xml:ProcessingInstruction pi2 = xml `<?target data?>`;
    record {| 'xml:ProcessingInstruction value; |}? nextPI2 = pi2.iterator().next();
    assert(nextPI2.toString(), "{\"value\":`<?target data?>`}");
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
