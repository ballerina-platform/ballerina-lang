function testSimpleQueryExprForXML() returns xml {
    xml book1 = xml `<book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                     </book>`;

    xml book2 = xml `<book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                    </book>`;

    xml book = book1 + book2;

    xml books = from var x in book/<name>
                select <xml> x;

    return  books;
}

function testSimpleQueryExprForXML2() returns xml {
    xml theXml = xml `<book>the book</book>`;
    xml bitOfText = xml `bit of text\u2702\u2705`;
    xml compositeXml = theXml + bitOfText;

    xml finalOutput = from var elem in compositeXml
                      select <xml> elem;

    return  finalOutput;
}

function testSimpleQueryExprForXML3() returns xml {
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

    xml finalOutput = from var price in bookstore/**/<price>
                      select <xml> price;

    return  finalOutput;
}

function testQueryExprWithLimitForXML() returns xml {
    xml bookStore = xml `<bookStore>
                     <book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                     </book>
                     <book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                     </book>
                     <book>
                           <name>The Enchanted Wood</name>
                           <author>Enid Blyton</author>
                     </book>
                  </bookStore>`;

    xml authors = from var book in bookStore/<book>/<author>
                  select <xml> book
                  limit 2;

    return  authors;
}

function testQueryExprWithWhereLetClausesForXML() returns xml {
    xml bookStore = xml `<bookStore>
                     <book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                           <price>45</price>
                     </book>
                     <book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                           <price>55</price>
                     </book>
                     <book>
                           <name>The Enchanted Wood</name>
                           <author>Enid Blyton</author>
                           <price>70</price>
                     </book>
                  </bookStore>`;

    xml authors = from var x in bookStore/<book>/<author>
                  let string authorDetails = "<author>Enid Blyton</author>"
                  where x.toString() == authorDetails
                  select <xml> x;

    return  authors;
}

function testQueryExprWithMultipleFromClausesForXML() returns xml {
    xml bookStore = xml `<bookStore>
                     <book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                           <price>45</price>
                     </book>
                     <book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                           <price>55</price>
                     </book>
                  </bookStore>`;

    xml authorList = xml `<authorList>
                   <author>
                           <name>Sir Arthur Conan Doyle</name>
                           <country>UK</country>
                   </author>
                   <author>
                           <name>Dan Brown</name>
                           <country>US</country>
                   </author>
                 </authorList>`;

    xml authors = from var x in bookStore/<book>/<author>
                  from var y in authorList/<author>/<name>
                  select <xml> y;

    return  authors;
}
