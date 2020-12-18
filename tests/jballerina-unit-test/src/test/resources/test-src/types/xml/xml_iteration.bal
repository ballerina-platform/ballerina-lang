import ballerina/io;
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
        count +=1;
        i +=1;
    }

    return titles;
}

int count = 0;

function foreachOpTest() returns [int, string][] {
    [int, string][] titles = [];

    count = 0;

    (bookstore/<book>).forEach(function (xml|string entry) {
        if entry is xml {
            titles[count] = [count, (entry/<title>/*).toString()];
            count += 1;
        }
    });

    return titles;
}

function mapOpTest() returns xml {
    xml titles = (bookstore/<book>).map(function (xml|string book) returns xml {
        if book is xml {
            return book/<author>;
        }
        return xml ` `;
    });
    return titles;
}

function filterOpTest() returns xml {
    xml books = (bookstore/<book>).filter(function (xml|string book) returns boolean {
                                                if book is xml {
                                                    var result = intlib:fromString((book/<year>/*).toString());
                                                    if (result is int) {
                                                       return result > 2004;
                                                    } else {
                                                       return false;
                                                    }
                                                }
                                                return false;
                                            }).map(function(xml|string value) returns xml {
                                                if value is xml {
                                                    return value;
                                                }
                                                return xml ` `;
                                            });
    return books;
}

function chainedIterableOps() returns xml {
    xml authors = (bookstore/<book>).filter(function (xml|string book) returns boolean {
                                                if book is xml {
                                                    var result = intlib:fromString((book/<year>/*).toString());
                                                    if (result is int) {
                                                       return result > 2004;
                                                    } else {
                                                       return false;
                                                    }
                                                }
                                                return false;
                                            }).map(function (xml|string book) returns xml {
                                                    if book is xml {
                                                        return book/<author>;
                                                    }
                                                    return xml ` `;
                                                });
    return authors;
}

xml theXml = xml `<book>the book</book>`;
xml bitOfText = xml `bit of text\u2702\u2705`;
xml compositeXml = theXml + bitOfText;

xml xdata = xml `<p:person xmlns:p="foo" xmlns:q="bar">
        <p:name>bob</p:name>
        <p:address>
            <p:city>NY</p:city>
            <q:country>US</q:country>
        </p:address>
        <q:ID>1131313</q:ID>
    </p:person>`;

function xmlSequenceIter() returns string {
    string result = "";

    foreach xml elem in compositeXml {
        string str = io:sprintf("%s\n", elem);
        result += str;
    }
    return result;
}

function xmlCharItemIter() returns string {
    string result = "";
    int i = 0;
    foreach xml elem in bitOfText {
        string str = io:sprintf("%s\n", elem);
        result += str;
        i += 1;
    }
    return result;
}

function xmlTypeParamElementIter() {
    xml<'xml:Element> el1 = xml `<foo>foo</foo><bar/>`;

    string result = "";
    foreach 'xml:Element elem in el1 {
        string str = io:sprintf("%s\n", elem);
        result += str;
    }
    assert(result, "<foo>foo</foo>\n");

    record {| 'xml:Element value; |}? nextElement1 = el1.iterator().next();
    assert(result, "<foo>foo</foo>\n");
}

function xmlTypeParamTextIter() {
    xml<'xml:Text> txt1 = xml `bit of text\u2702\u2705`;
    'xml:Text txt2 = xml `bit of text\u2702\u2705`;

    string result = "";
    foreach 'xml:Text elem in txt1 {
        string str = io:sprintf("%s\n", elem);
        result += str;
    }
    assert(result, "bit of text\\u2702\\u2705\n");

    result = "";
    foreach 'xml:Text elem in txt2 {
        string str = io:sprintf("%s\n", elem);
        result += str;
    }
    assert(result, "bit of text\\u2702\\u2705\n");

    record {| 'xml:Text value; |}? nextText1 = txt1.iterator().next();
    assert(result, "bit of text\\u2702\\u2705\n");

    record {| 'xml:Text value; |}? nextText2 = txt2.iterator().next();
    assert(result, "bit of text\\u2702\\u2705\n");
}

function xmlTypeParamCommentIter() {
     xml<'xml:Comment> comment1 = xml `<!--I am a comment-->`;

     string result = "";
     foreach 'xml:Comment elem in comment1 {
         string str = io:sprintf("%s\n", elem);
         result += str;
     }
     assert(result, "");

     record {| 'xml:Comment value; |}? nextComment1 = comment1.iterator().next();
     assert(result, "");
}

function xmlTypeParamPIIter() {
     xml<'xml:ProcessingInstruction> pi1 = xml `<?target data?>`;

     string result = "";
     foreach 'xml:ProcessingInstruction elem in pi1 {
         string str = io:sprintf("%s\n", elem);
         result += str;
     }
     assert(result, "");

     record {| 'xml:ProcessingInstruction value; |}? nextPI1 = pi1.iterator().next();
     assert(result, "");
}

function xmlTypeParamUnionIter() {
    xml<'xml:Element|'xml:Text> el1 = xml `<foo>foo</foo><bar/>`;

    string result = "";
    foreach 'xml:Element|'xml:Text elem in el1 {
        string str = io:sprintf("%s\n", elem);
        result += str;
    }
    assert(result, "<foo>foo</foo>\n");

    record {| 'xml:Element|'xml:Text value; |}? nextUnionXMLVal1 = el1.iterator().next();
    assert(result, "<foo>foo</foo>\n");
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
