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

function foreachTest() returns (int, string)[] {
    (int, string)[] titles = [];
    int count = 0;

    foreach i, x in bookstore["book"] {
        titles[count] = (i, x["title"].getTextValue());
        count += 1;
    }

    return titles;
}

function foreachOpTest() returns (int, string)[] {
    (int, string)[] titles = [];
    int count = 0;

    bookstore["book"].foreach(function ((int, xml) entry) {
        var (index, value) = entry;
        titles[count] = (index, value["title"].getTextValue());
        count += 1;
    });

    return titles;
}

function mapOpTest() returns xml[] {
    xml[] titles = bookstore["book"].map(function (xml book) returns xml {
        return book["author"];
    });
    return titles;
}

function filterOpTest() returns xml[] {
    xml[] books = bookstore["book"].filter(function (xml book) returns boolean {
                                               var result = <int>book["year"].getTextValue();
                                               if (result is int) {
                                                  return result > 2004;
                                               } else {
                                                  return false;
                                               }
                                            });
    return books;
}

function chainedIterableOps() returns xml[] {
    xml[] authors = bookstore["book"].filter(function (xml book) returns boolean {
                                               var result = <int>book["year"].getTextValue();
                                               if (result is int) {
                                                  return result > 2004;
                                               } else {
                                                  return false;
                                               }
                                            }).map(function (xml book) returns xml {
                                                  return book["author"];
                                            });
    return authors;
}
