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

function foreachTest() returns [int, string][] {
    [int, string][] titles = [];

    count = 0;

    int i = 0;
    foreach var x in bookstore/<book> {
        titles[count] = [i, (x/<title>/*).toString()];
        count += 1;
        i += 1;
    }

    return titles;
}

int count = 0;

function foreachOpTest() returns [int, string][] {
    [int, string][] titles = [];

    count = 0;

    (bookstore/<book>).forEach(function (xml entry) {
        titles[count] = [count, (entry/<title>/*).toString()];
        count += 1;
    });

    return titles;
}

function mapOpTest() returns xml {
    xml titles = (bookstore/<book>).map(function (xml book) returns xml {
        return book/<author>;
    });
    return titles;
}

function filterOpTest() returns xml {
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
    return books;
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
