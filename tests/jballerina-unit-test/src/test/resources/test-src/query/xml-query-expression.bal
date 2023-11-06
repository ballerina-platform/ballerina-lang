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

type Person record {|
   readonly string name;
   string country;
|};

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
                select x;

    return  books;
}

function testSimpleQueryExprForXML2() returns xml {
    xml theXml = xml `<book>the book</book>`;
    xml bitOfText = xml `bit of text\u2702\u2705`;
    xml compositeXml = theXml + bitOfText;

    xml finalOutput = from var elem in compositeXml
                      select elem;

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
                      select price;

    return  finalOutput;
}

type Book record {|
    string title;
    string author;
|};

class BookGenerator {
    int i = 0;

    public isolated function next() returns record {|Book value;|}|error? {
        self.i += 1;
        if (self.i < 0) {
            return error("Error");
        }
        if (self.i >= 3) {
            return ();
        }
        return {
            value: {
                title: "Title " + self.i.toString(), author: "Author " + self.i.toString()
            }
        };
    }
}

public function testSimpleQueryExprForXML4() {
    error? e = trap simpleQueryExprForXML();
    assertFalse(e is error);

    e = trap simpleQueryExprForXML2();
    assertFalse(e is error);

    e = trap simpleQueryExprForXML3();
    assertFalse(e is error);

    e = trap simpleQueryExprForXML4();
    assertFalse(e is error);

    e = trap simpleQueryExprForXML5();
    assertFalse(e is error);

    e = trap simpleQueryExprForXML6();
    assertFalse(e is error);

    e = trap simpleQueryExprForXML7();
    assertFalse(e is error);

    // issue - #40012
    // e = trap simpleQueryExprForXML8();
    // assertFalse(e is error);

    e = trap simpleQueryExprForXML9();
    assertFalse(e is error);

    e = trap simpleQueryExprForXML10();
    assertFalse(e is error);
}

function simpleQueryExprForXML() returns error? {
    stream<Book, error?> bookStream = [{ author: "Author 1", title: "Title 1" },
                                        {author: "Author 2", title: "Title 2" }].toStream();

    xml xmlValue = check from Book book in bookStream
        select xml `<Book>
                        <Author>${book.author}</Author>
                        <Title>${book.title}</Title>
                    </Book>`;

    xml expectedValue = xml `<Book>
                        <Author>Author 1</Author>
                        <Title>Title 1</Title>
                    </Book><Book>
                        <Author>Author 2</Author>
                        <Title>Title 2</Title>
                    </Book>`;

    assertTrue(xmlValue is xml);
    assertTrue(xmlValue is xml<xml:Element>);
    assertEquality(xmlValue, expectedValue);
}

function simpleQueryExprForXML2() returns error? {
    stream<Book, ()> bookStream = [{ author: "Author 1", title: "Title 1" },
                                        {author: "Author 2", title: "Title 2" }].toStream();

    xml xmlValue = from Book book in bookStream
        select xml `<Book>
                        <Author>${book.author}</Author>
                        <Title>${book.title}</Title>
                    </Book>`;

    xml expectedValue = xml `<Book>
                        <Author>Author 1</Author>
                        <Title>Title 1</Title>
                    </Book><Book>
                        <Author>Author 2</Author>
                        <Title>Title 2</Title>
                    </Book>`;

    assertTrue(xmlValue is xml);
    assertTrue(xmlValue is xml<xml:Element>);
    assertEquality(xmlValue, expectedValue);
}

function simpleQueryExprForXML3() returns error? {
    BookGenerator bookGenerator = new ();
    stream<Book, error?> bookStream = new (bookGenerator);

    xml xmlValue = check from Book book in bookStream
        select xml `<Book>
                        <Author>${book.author}</Author>
                        <Title>${book.title}</Title>
                    </Book>`;

    xml expectedValue = xml `<Book>
                        <Author>Author 1</Author>
                        <Title>Title 1</Title>
                    </Book><Book>
                        <Author>Author 2</Author>
                        <Title>Title 2</Title>
                    </Book>`;

    assertTrue(xmlValue is xml);
    assertTrue(xmlValue is xml<xml:Element>);
    assertEquality(xmlValue, expectedValue);
}

function simpleQueryExprForXML4() returns error? {
    BookGenerator bookGenerator = new ();
    stream<Book, error?> bookStream = new (bookGenerator);

    xml xmlValue = check from Book book in bookStream
        select xml `${book.title} - ${book.author}.`;

    xml expectedValue = xml `Title 1 - Author 1.Title 2 - Author 2.`;

    assertTrue(xmlValue is xml);
    assertTrue(xmlValue is xml<xml:Text>);
    assertEquality(xmlValue.toString(), expectedValue.toString());
}

function simpleQueryExprForXML5() returns error? {
    BookGenerator bookGenerator = new ();
    stream<Book, error?> bookStream = new (bookGenerator);

    xml xmlValue = check from Book book in bookStream
        select xml `<!--${book.title} - ${book.author}-->`;

    xml expectedValue = xml `<!--Title 1 - Author 1--><!--Title 2 - Author 2-->`;

    assertTrue(xmlValue is xml);
    assertTrue(xmlValue is xml<xml:Comment>);
    assertEquality(xmlValue, expectedValue);
}

function simpleQueryExprForXML6() returns error? {
    BookGenerator bookGenerator = new ();
    stream<Book, error?> bookStream = new (bookGenerator);

    xml xmlValue = check from Book _ in bookStream
        select xml `<?target data?>`;

    xml expectedValue = xml `<?target data?><?target data?>`;

    assertTrue(xmlValue is xml);
    assertTrue(xmlValue is xml<xml:ProcessingInstruction>);
    assertEquality(xmlValue, expectedValue);
}

function simpleQueryExprForXML7() returns error? {
    BookGenerator bookGenerator = new ();
    stream<Book, error?> bookStream = new (bookGenerator);

    xml & readonly xmlValue = check from Book book in bookStream
        select xml `<Book>
                        <Author>${book.author}</Author>
                        <Title>${book.title}</Title>
                    </Book>`;

    xml expectedValue = xml `<Book>
                        <Author>Author 1</Author>
                        <Title>Title 1</Title>
                    </Book><Book>
                        <Author>Author 2</Author>
                        <Title>Title 2</Title>
                    </Book>`;

    assertTrue(xmlValue is xml & readonly);
    assertEquality(xmlValue, expectedValue);
}

// issue - #40012
// function simpleQueryExprForXML8() returns error? {
//     BookGenerator bookGenerator = new ();
//     stream<Book, error?> bookStream = new (bookGenerator);

//     xml|int[] xmlValue = check from Book book in bookStream
//         select xml `<Book>
//                         <Author>${book.author}</Author>
//                         <Title>${book.title}</Title>
//                     </Book>`;

//     xml expectedValue = xml `<Book>
//                         <Author>Author 1</Author>
//                         <Title>Title 1</Title>
//                     </Book><Book>
//                         <Author>Author 2</Author>
//                         <Title>Title 2</Title>
//                     </Book>`;

//     assertTrue(xmlValue is xml<xml:Element>);
//     assertEquality(xmlValue, expectedValue);
// }

function simpleQueryExprForXML9() returns error? {
    BookGenerator bookGenerator = new ();
    stream<Book, error?> bookStream = new (bookGenerator);

    var xmlValue = stream from Book book in bookStream
        select xml `<Book>
                        <Author>${book.author}</Author>
                        <Title>${book.title}</Title>
                    </Book>`;

    xml expectedValue = xml `<Book>
                        <Author>Author 1</Author>
                        <Title>Title 1</Title>
                    </Book>`;

    assertTrue(xmlValue is stream<xml:Element, error?>);
    assertEquality((<record { xml:Element value; }> (check xmlValue.next())).value, expectedValue);
}

function simpleQueryExprForXML10() returns error? {
    BookGenerator bookGenerator = new ();
    stream<Book, error?> bookStream = new (bookGenerator);

    xml:Element[] xmlValue = check from Book book in bookStream
        select xml `<Book>
                        <Author>${book.author}</Author>
                        <Title>${book.title}</Title>
                    </Book>`;

    xml expectedValueElement1 = xml `<Book>
                        <Author>Author 1</Author>
                        <Title>Title 1</Title>
                    </Book>`;
    xml expectedValueElement2 = xml `<Book>
                        <Author>Author 2</Author>
                        <Title>Title 2</Title>
                    </Book>`;

    assertTrue(xmlValue is xml:Element[]);
    assertEquality(xmlValue.length(), 2);
    assertEquality(xmlValue[0], expectedValueElement1);
    assertEquality(xmlValue[1], expectedValueElement2);
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
                  limit 2
                  select book;

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
                  select x;

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
                  select y;

    return  authors;
}

function testSimpleQueryExprForXMLOrNilResult() returns xml? {
    xml book1 = xml `<book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                     </book>`;

    xml book2 = xml `<book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                    </book>`;

    xml book = book1 + book2;

    xml? books = from var x in book/<name>
                select x;

    return  books;
}

function testSimpleQueryExprForXMLOrNilResult2() returns xml? {
    xml theXml = xml `<book>the book</book>`;
    xml bitOfText = xml `bit of text\u2702\u2705`;
    xml compositeXml = theXml + bitOfText;

    xml? finalOutput = from var elem in compositeXml
                      select elem;

    return  finalOutput;
}

function testSimpleQueryExprForXMLOrNilResult3() returns xml? {
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

    xml? finalOutput = from var price in bookstore/**/<price>
                      select price;

    return  finalOutput;
}

function testQueryExprWithLimitForXMLOrNilResult() returns xml? {
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

    xml? authors = from var book in bookStore/<book>/<author>
                  limit 2
                  select book;

    return  authors;
}

function testQueryExprWithWhereLetClausesForXMLOrNilResult() returns xml? {
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

    xml? authors = from var x in bookStore/<book>/<author>
                  let string authorDetails = "<author>Enid Blyton</author>"
                  where x.toString() == authorDetails
                  select x;

    return  authors;
}

function testQueryExprWithMultipleFromClausesForXMLOrNilResult() returns xml? {
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

    xml? authors = from var x in bookStore/<book>/<author>
                  from var y in authorList/<author>/<name>
                  select y;

    return  authors;
}

function testSimpleQueryExprWithVarForXML() returns xml {
    xml book1 = xml `<book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                     </book>`;

    xml book2 = xml `<book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                    </book>`;

    xml book = book1 + book2;

    var books = from var x in book/<name>
                select x;

    return  books;
}

function testSimpleQueryExprWithListForXML() returns xml[] {
    xml book1 = xml `<book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                     </book>`;

    xml book2 = xml `<book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                    </book>`;

    xml book = book1 + book2;

    xml[] books = from var x in book/<name>
                select x;

    return  books;
}

function testSimpleQueryExprWithUnionTypeForXML() returns error|xml {
    xml book1 = xml `<book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                     </book>`;

    xml book2 = xml `<book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                    </book>`;

    xml book = book1 + book2;

    error|xml books = from var x in book/<name>
                select x;

    return  books;
}

function testSimpleQueryExprWithUnionTypeForXML2() returns xml[]|error {
    xml book1 = xml `<book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                     </book>`;

    xml book2 = xml `<book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                    </book>`;

    xml book = book1 + book2;

    xml[]|error books = from var x in book/<name>
                select x;

    return  books;
}

public function testSimpleQueryExprWithXMLElementLiteral() returns xml {
    xml payload = xml `<Root>
                            <data>
                                <record>
                                    <field name="Country or Area" key="ABW">Aruba</field>
                                    <field name="Item" key="EN.ATM.CO2E.KT">CO2 emissions (kt)</field>
                                    <field name="Year">1960</field>
                                    <field name="Value">11092.675</field>
                                </record>
                            </data>
                       </Root>`;

    xml res = from var x in payload/<data>/<*>
             let var year = <xml> x/<'field>[2]
             let var value = <xml> x/<'field>[3].name
             select xml `<entry>${<string> checkpanic value}</entry>`;

    return res;
}

public function testSimpleQueryExprWithNestedXMLElements() returns xml {
    xml payload = xml `<Root>
                            <data>
                                <record>
                                    <field name="Country or Area" key="ABW">Aruba</field>
                                    <field name="Item" key="EN.ATM.CO2E.KT">CO2 emissions (kt)</field>
                                    <field name="Year">1960</field>
                                    <field name="Value">11092.675</field>
                                </record>
                            </data>
                       </Root>`;

    xml res = xml `<doc> ${from var x in payload/<data>/<*>
         let var year = <xml> x/<'field>[2]
         let var value = <xml> x/<'field>[3].name
         select xml `<entry>${<string> checkpanic value}</entry>`} </doc>`;

    return res;
}

function testQueryExpressionIteratingOverXMLInFrom() returns xml {
    xml x = xml `<foo>Hello<bar>World</bar></foo>`;
    xml res = from xml y in x select y;
    return res;
}

function testQueryExpressionIteratingOverXMLTextInFrom() returns xml {
    xml:Text x = xml `hello text`;
    xml res = from xml y in x select y;
    return res;
}

function testQueryExpressionIteratingOverXMLElementInFrom() returns xml {
    xml<xml:Element> x = xml `<foo>Hello<bar>World</bar></foo>`;
    xml<xml:Element> res = from xml:Element y in x select y;
    return res;
}

function testQueryExpressionIteratingOverXMLPIInFrom() returns xml {
    xml<xml:ProcessingInstruction> x = xml `<?xml-stylesheet type="text/xsl" href="style.xsl"?>`;
    xml res = from var y in x select y;
    return res;
}

function testQueryExpressionIteratingOverXMLWithOtherClauses() returns xml {
    xml<xml:Element> bookStore = xml `<bookStore>
                                        <book>
                                            <name>The Enchanted Wood</name>
                                            <author>Enid Blyton</author>
                                        </book>
                                        <book>
                                            <name>Sherlock Holmes</name>
                                            <author>Sir Arthur Conan Doyle</author>
                                        </book>
                                        <book>
                                            <name>The Da Vinci Code</name>
                                            <author>Dan Brown</author>
                                        </book>
                                    </bookStore>`;

    xml res = from xml<xml:Element> book in bookStore/<book>/<author>
              order by book.toString()
              limit 2
              select book;
    return res;
}

function testQueryExpressionIteratingOverXMLInFromWithXMLOrNilResult() returns xml? {
    xml<xml:Comment> x = xml `<!-- this is a comment text -->`;
    xml? res = from var y in x select y;
    return res;
}

function testQueryExpressionIteratingOverXMLInFromInInnerQueries() returns xml? {
    xml<xml:Element> bookStore = xml `<bookStore>
                                        <book>
                                            <name>The Enchanted Wood</name>
                                            <author>Enid Blyton</author>
                                        </book>
                                        <book>
                                            <name>Sherlock Holmes</name>
                                            <author>Sir Arthur Conan Doyle</author>
                                        </book>
                                        <book>
                                            <name>The Da Vinci Code</name>
                                            <author>Dan Brown</author>
                                        </book>
                                    </bookStore>`;

    xml res = from var book in (from xml:Element e in bookStore/<book>/<author> select e)
              select book;
    return res;
}

function testXMLTemplateWithQueryExpression() returns xml {
   Person p1 = {name: "Mike", country: "Germany"};
   Person p2 = {name:"Anne", country: "France"};
   Person p3 = {name: "John", country: "Russia"};
   Person[] persons = [p1, p2, p3];
   xml res = xml`<data>${
      from var {name, country} in persons
      select xml`<person country="${country}">${name}</person>`}</data>`;
   return res;
}

function testXMLTemplateWithQueryExpression2() returns xml {
   Person p1 = {name: "Mike", country: "Germany"};
   Person p2 = {name:"Anne", country: "France"};
   Person p3 = {name: "John", country: "Russia"};
   Person[] persons = [p1, p2, p3];

   xml res = xml`<data>${
      from var {name, country} in persons
      select xml`${name}`}</data>`;
   return res;
}

function testXMLTemplateWithQueryExpression3() returns xml {
   Person p1 = {name: "Mike", country: "Germany"};
   Person p2 = {name:"Anne", country: "France"};
   Person p3 = {name: "John", country: "Russia"};
   Person[] persons = [p1, p2, p3];

   xml res = xml`<data>${
      from var {name, country} in persons.toStream()
      select xml`<person country="${country}">${name}</person>`}</data>`;
   return res;
}

function testXMLTemplateWithQueryExpression4() returns xml {
   table<Person> personTable = table key(name) [
       {name: "Mike", country: "Germany"},
       {name:"Anne", country: "France"},
       {name: "John", country: "Russia"}];

   xml res = xml`<data>${
      from var {name, country} in personTable
      order by country descending
      limit 2
      select xml`<person country="${country}">${name}</person>`}</data>`;
   return res;
}

function testQueryExpressionIteratingOverXMLWithNamespaces() returns xml {
    xmlns "foo" as ns;
    xml x = xml `<name><fname ns:status="active">Mike</fname><fname>Jane</fname><lname>Eyre</lname></name>`;

    xml res = from var fname in x/<fname>
              where fname?.ns:status == "active"
              select fname;
    return res;
}

function testQueryExpressionIteratingOverTableReturningXML() returns xml {
   table<Person> personTable = table key(name) [
       {name: "Mike", country: "Germany"},
       {name:"Anne", country: "France"},
       {name: "John", country: "Russia"}];

   xml res = from var {name, country} in personTable
             order by country descending
             limit 2
             select xml`<person country="${country}">${name}</person>`;
   return res;
}

function testQueryExpressionIteratingOverStreamReturningXML() returns xml {
   Person p1 = {name: "Mike", country: "Germany"};
   Person p2 = {name:"Anne", country: "France"};
   Person p3 = {name: "John", country: "Russia"};
   Person[] personList = [p1, p2, p3];

   xml res = from var {name, country} in personList.toStream()
             order by country descending
             limit 2
             select xml`<person country="${country}">${name}</person>`;
   return res;
}

function testSimpleQueryExprForXMLWithReadonly1() {
    xml & readonly book1 = xml `<name>Sherlock Holmes</name>`;

    xml & readonly books1 = from var x in book1
        select x;

    any _ = <readonly> books1;
    assertEquality(books1.toString(), (xml `<name>Sherlock Holmes</name>`).toString());

    xml & readonly books2 = from var x in book1
        where x is xml:Element
        select x;
    any _ = <readonly> books2;
    assertEquality(books2.toString(), (xml `<name>Sherlock Holmes</name>`).toString());

    xml<xml:Element> & readonly books3 = from xml x in book1
        where x is xml:Element
        select x;
    any _ = <readonly> books3;
    assertEquality(books3.toString(), (xml `<name>Sherlock Holmes</name>`).toString());

    xml:Comment[] & readonly comments = [xml `<!--This is a comment 1-->`, xml `<!--This is a comment 1-->`];

    xml & readonly cmnt1 = from xml x in comments
        select x;
    any _ = <readonly> cmnt1;
    assertEquality(cmnt1.toString(), (xml `<!--This is a comment 1--><!--This is a comment 1-->`).toString());

    xml & readonly cmnt2 = from var x in comments
        select x;
    any _ = <readonly> cmnt2;
    assertEquality(cmnt2.toString(), (xml `<!--This is a comment 1--><!--This is a comment 1-->`).toString());

    xml<xml:Comment> & readonly cmnt3 = from xml:Comment x in comments
        select x;
    any _ = <readonly> cmnt3;
    assertEquality(cmnt3.toString(), (xml `<!--This is a comment 1--><!--This is a comment 1-->`).toString());

    xml:ProcessingInstruction[] & readonly processingInstructions = [xml `<?target data?>`, xml `<?target url?>`];

    xml & readonly pi1 = from xml x in processingInstructions
        select x;
    any _ = <readonly> pi1;
    assertEquality(pi1.toString(), (xml `<?target data?><?target url?>`).toString());

    xml & readonly pi2 = from var x in processingInstructions
        select x;
    any _ = <readonly> pi2;
    assertEquality(pi2.toString(), (xml `<?target data?><?target url?>`).toString());

    xml<xml:ProcessingInstruction> & readonly pi3 = from xml:ProcessingInstruction x in processingInstructions
        select x;
    any _ = <readonly> pi3;
    assertEquality(pi3.toString(), (xml `<?target data?><?target url?>`).toString());

    xml:Text[] texts = [xml `text 1`, xml `text 2`];

    xml & readonly txt1 = from xml x in texts
        select x;
    any _ = <readonly> txt1;
    assertEquality(txt1.toString(), (xml `text 1text 2`).toString());

    xml:Text txt2 = from  xml:Text x in texts
        select x;
    any _ = <readonly> txt2;
    assertEquality(txt2.toString(), (xml `text 1text 2`).toString());

    xml<xml:Text> txt3 = from xml:Text x in texts
        select x;
    assertEquality(txt3.toString(), (xml `text 1text 2`).toString());
}

function testSimpleQueryExprForXMLWithReadonly2() {
    xml:Element theXml = xml `<book>the book</book>`;
    xml bitOfText = xml `bit of text\u2702\u2705`;
    xml:ProcessingInstruction processingInstruction = xml `<?target data?>`;
    xml:Comment comment = xml `<!--This is a comment 1-->`;
    xml compositeXml1 = theXml + bitOfText + processingInstruction + comment;

    xml & readonly finalOutput1 = from var elem in compositeXml1.cloneReadOnly()
        select elem;
    any _ = <readonly> finalOutput1;
    assertEquality(finalOutput1.toString(), (xml `<book>the book</book>bit of text\u2702\u2705<?target data?><!--This is a comment 1-->`).toString());

    xml<xml:ProcessingInstruction|xml:Comment|xml:Element> compositeXml2 = theXml + processingInstruction + comment;

    xml & readonly finalOutput2 = from var elem in compositeXml2.cloneReadOnly()
        select elem;
    any _ = <readonly> finalOutput2;
    assertEquality(finalOutput2.toString(), (xml `<book>the book</book><?target data?><!--This is a comment 1-->`).toString());

    xml<xml:ProcessingInstruction|xml:Comment|xml:Element> & readonly finalOutput3 = from var elem in compositeXml2.cloneReadOnly()
        select elem;
    any _ = <readonly> finalOutput3;
    assertEquality(finalOutput3.toString(), (xml `<book>the book</book><?target data?><!--This is a comment 1-->`).toString());
}

function testSimpleQueryExprForXMLWithReadonly3() {
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

    xml & readonly finalOutput = from var price in bookstore/**/<price>
        select price.cloneReadOnly();
    any _ = <readonly> finalOutput;
    assertEquality(finalOutput.toString(), (xml `<price>30.00</price><price>29.99</price><price>49.99</price><price>39.95</price>`).toString());
}

function testQueryExprWithLimitForXMLWithReadonly() {
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

    xml & readonly authors = from var book in bookStore/<book>/<author>
        limit 2
        select book.cloneReadOnly();
    any _ = <readonly> authors;
    assertEquality(authors.toString(), (xml `<author>Sir Arthur Conan Doyle</author><author>Dan Brown</author>`).toString());
}

function testQueryExprWithWhereLetClausesForXMLWithReadonly() {
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

    xml & readonly authors = from var x in bookStore/<book>/<author>
        let string authorDetails = "<author>Enid Blyton</author>"
        where x.toString() == authorDetails
        select x.cloneReadOnly();
    any _ = <readonly> authors;
    assertEquality(authors.toString(), (xml `<author>Enid Blyton</author>`).toString());
}

function testQueryExprWithMultipleFromClausesForXMLWithReadonly() {
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

    xml & readonly authors = from var x in bookStore/<book>/<author>
        from var y in authorList/<author>/<name>
        select y.cloneReadOnly();
    any _ = <readonly> authors;
    assertEquality(authors.toString(), (xml `<name>Sir Arthur Conan Doyle</name><name>Dan Brown</name><name>Sir Arthur Conan Doyle</name><name>Dan Brown</name>`).toString());
}

function testSimpleQueryExprForXMLOrNilResultWithReadonly1() {
    xml book1 = xml `<book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                     </book>`;

    xml book2 = xml `<book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                    </book>`;

    xml book = book1 + book2;

    xml? & readonly books = from var x in book/<name>
        select x.cloneReadOnly();
    any _ = <readonly> books;
    assertEquality(books.toString(), (xml `<name>Sherlock Holmes</name><name>The Da Vinci Code</name>`).toString());
}

function testSimpleQueryExprForXMLOrNilResultWithReadonly2() {
    xml theXml = xml `<book>the book</book>`;
    xml bitOfText = xml `bit of text\u2702\u2705`;
    xml compositeXml = theXml + bitOfText;

    xml? & readonly finalOutput = from var elem in compositeXml
        select elem.cloneReadOnly();
    any _ = <readonly> finalOutput;
    assertEquality(finalOutput.toString(), (xml `<book>the book</book>bit of text\u2702\u2705`).toString());
}

function testSimpleQueryExprForXMLOrNilResultWithReadonly3() {
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

    xml? & readonly finalOutput = from var price in bookstore/**/<price>
        select price.cloneReadOnly();
    any _ = <readonly> finalOutput;
    assertEquality(finalOutput.toString(), (xml `<price>30.00</price><price>29.99</price><price>49.99</price><price>39.95</price>`).toString());
}

function testQueryExprWithLimitForXMLOrNilResultWithReadonly() {
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

    xml? & readonly authors = from var book in bookStore/<book>/<author>
        limit 2
        select book.cloneReadOnly();
    any _ = <readonly> authors;
    assertEquality(authors.toString(), (xml `<author>Sir Arthur Conan Doyle</author><author>Dan Brown</author>`).toString());
}

function testQueryExprWithWhereLetClausesForXMLOrNilResultWithReadonly() {
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

    xml? & readonly authors = from var x in bookStore/<book>/<author>
        let string authorDetails = "<author>Enid Blyton</author>"
        where x.toString() == authorDetails
        select x.cloneReadOnly();
    any _ = <readonly> authors;
    assertEquality(authors.toString(), (xml `<author>Enid Blyton</author>`).toString());
}

function testQueryExprWithMultipleFromClausesForXMLOrNilResultWithReadonly() {
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

    xml? & readonly authors = from var x in bookStore/<book>/<author>
        from var y in authorList/<author>/<name>
        select y.cloneReadOnly();
    any _ = <readonly> authors;
    assertEquality(authors.toString(), (xml `<name>Sir Arthur Conan Doyle</name><name>Dan Brown</name><name>Sir Arthur Conan Doyle</name><name>Dan Brown</name>`).toString());
}

function testSimpleQueryExprWithVarForXMLWithReadonly() {
    xml book1 = xml `<book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                     </book>`;

    xml book2 = xml `<book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                    </book>`;

    xml book = book1 + book2;

    xml & readonly books = from var x in book/<name>
        select x.cloneReadOnly();
    any _ = <readonly> books;
    assertEquality(books.toString(), (xml `<name>Sherlock Holmes</name><name>The Da Vinci Code</name>`).toString());
}

function testSimpleQueryExprWithListForXMLWithReadonly() {
    xml book1 = xml `<book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                     </book>`;

    xml book2 = xml `<book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                    </book>`;

    xml book = book1 + book2;

    xml:Element[] & readonly books = from var x in book/<name>
        select x.cloneReadOnly();
    any _ = <readonly> books;
    assertEquality(books.toString(), ([xml `<name>Sherlock Holmes</name>`, xml `<name>The Da Vinci Code</name>`]).toString());
}

function testSimpleQueryExprWithUnionTypeForXMLWithReadonly1() {
    xml book1 = xml `<book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                     </book>`;

    xml book2 = xml `<book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                    </book>`;

    xml book = book1 + book2;

    error|xml & readonly books = from var x in book/<name>
        select x.cloneReadOnly();
    any _ = <readonly> checkpanic books;
    assertEquality((checkpanic books).toString(), (xml `<name>Sherlock Holmes</name><name>The Da Vinci Code</name>`).toString());
}

function testSimpleQueryExprWithUnionTypeForXMLWithReadonly2() {
    xml book1 = xml `<book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                     </book>`;

    xml book2 = xml `<book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                    </book>`;

    xml book = book1 + book2;

    xml[] & readonly books = from var x in book/<name>
        select x.cloneReadOnly();
    any _ = <readonly> books;
    assertEquality(books.toString(), ([xml `<name>Sherlock Holmes</name>`, xml `<name>The Da Vinci Code</name>`]).toString());
}

public function testSimpleQueryExprWithXMLElementLiteralWithReadonly() {
    xml payload = xml `<Root>
                            <data>
                                <record>
                                    <field name="Country or Area" key="ABW">Aruba</field>
                                    <field name="Item" key="EN.ATM.CO2E.KT">CO2 emissions (kt)</field>
                                    <field name="Year">1960</field>
                                    <field name="Value">11092.675</field>
                                </record>
                            </data>
                       </Root>`;

    xml & readonly res = from var x in payload/<data>/<*>
        let var year = <xml>x/<'field>[2]
        let var value = <xml>x/<'field>[3].name
        select xml `<entry>${<string>checkpanic value} ${year}</entry>`;
    any _ = <readonly> res;
    assertEquality(res.toString(), (xml `<entry>Value <field name="Year">1960</field></entry>`).toString());
}

public function testSimpleQueryExprWithNestedXMLElementsWithReadonly() {
    xml payload = xml `<Root>
                            <data>
                                <record>
                                    <field name="Country or Area" key="ABW">Aruba</field>
                                    <field name="Item" key="EN.ATM.CO2E.KT">CO2 emissions (kt)</field>
                                    <field name="Year">1960</field>
                                    <field name="Value">11092.675</field>
                                </record>
                            </data>
                       </Root>`;

    xml & readonly res = xml `<doc> ${from var x in payload/<data>/<*>
        let var value = <xml>x/<'field>[3].name
        select xml `<entry>${<string>checkpanic value}</entry>`} </doc>`;
    any _ = <readonly> res;
    assertEquality(res.toString(), (xml `<doc> <entry>Value</entry> </doc>`).toString());
}

function testQueryExpressionIteratingOverXMLInFromWithReadonly() {
    xml x = xml `<foo>Hello<bar>World</bar></foo>`;
    xml & readonly res = from xml y in x
        select y.cloneReadOnly();
    any _ = <readonly> res;
    assertEquality(res.toString(), (xml `<foo>Hello<bar>World</bar></foo>`).toString());
}

function testQueryExpressionIteratingOverXMLTextInFromWithReadonly() {
    xml:Text x = xml `hello text`;
    xml & readonly res = from xml y in x
        select y;
    any _ = <readonly> res;
    assertEquality(res.toString(), (xml `hello text`).toString());
}

function testQueryExpressionIteratingOverXMLElementInFromWithReadonly() {
    xml<xml:Element> x = xml `<foo>Hello<bar>World</bar></foo>`;
    xml<xml:Element> & readonly res = from xml:Element y in x
        select y.cloneReadOnly();
    any _ = <readonly> res;
    assertEquality(res.toString(), (xml `<foo>Hello<bar>World</bar></foo>`).toString());
}

function testQueryExpressionIteratingOverXMLPIInFromWithReadonly() {
    xml<xml:ProcessingInstruction> x = xml `<?xml-stylesheet type="text/xsl" href="style.xsl"?>`;
    xml & readonly res = from var y in x
        select y.cloneReadOnly();
    any _ = <readonly> res;
    assertEquality(res.toString(), (xml `<?xml-stylesheet type="text/xsl" href="style.xsl"?>`).toString());
}

function testQueryExpressionIteratingOverXMLWithOtherClausesWithReadonly() {
    xml<xml:Element> bookStore = xml `<bookStore>
                                        <book>
                                            <name>The Enchanted Wood</name>
                                            <author>Enid Blyton</author>
                                        </book>
                                        <book>
                                            <name>Sherlock Holmes</name>
                                            <author>Sir Arthur Conan Doyle</author>
                                        </book>
                                        <book>
                                            <name>The Da Vinci Code</name>
                                            <author>Dan Brown</author>
                                        </book>
                                    </bookStore>`;

    xml & readonly res = from xml<xml:Element> book in bookStore/<book>/<author>
        order by book.toString()
        limit 2
        select book.cloneReadOnly();
    any _ = <readonly> res;
    assertEquality(res.toString(), (xml `<author>Dan Brown</author><author>Enid Blyton</author>`).toString());
}

function testQueryExpressionIteratingOverXMLInFromWithXMLOrNilResultWithReadonly() {
    xml<xml:Comment> x = xml `<!-- this is a comment text -->`;
    xml? & readonly res = from var y in x
        select y.cloneReadOnly();
    any _ = <readonly> res;
    assertEquality(res.toString(), (xml `<!-- this is a comment text -->`).toString());
}

function testQueryExpressionIteratingOverXMLInFromInInnerQueriesWithReadonly() {
    xml<xml:Element> bookStore = xml `<bookStore>
                                        <book>
                                            <name>The Enchanted Wood</name>
                                            <author>Enid Blyton</author>
                                        </book>
                                        <book>
                                            <name>Sherlock Holmes</name>
                                            <author>Sir Arthur Conan Doyle</author>
                                        </book>
                                        <book>
                                            <name>The Da Vinci Code</name>
                                            <author>Dan Brown</author>
                                        </book>
                                    </bookStore>`;

    xml & readonly res = from var book in (from xml:Element e in bookStore/<book>/<author>
            select e)
        select book.cloneReadOnly();
    any _ = <readonly> res;
    assertEquality(res.toString(), (xml `<author>Enid Blyton</author><author>Sir Arthur Conan Doyle</author><author>Dan Brown</author>`).toString());
}

function testXMLTemplateWithQueryExpressionWithReadonly1() {
    Person p1 = {name: "Mike", country: "Germany"};
    Person p2 = {name: "Anne", country: "France"};
    Person p3 = {name: "John", country: "Russia"};
    Person[] persons = [p1, p2, p3];
    xml & readonly res = xml `<data>${
    from var {name, country} in persons
    select xml `<person country="${country}">${name}</person>`}</data>`;
    any _ = <readonly> res;
    assertEquality(res.toString(), (xml `<data><person country="Germany">Mike</person><person country="France">Anne</person><person country="Russia">John</person></data>`).toString());
}

function testXMLTemplateWithQueryExpressionWithReadonly2() {
    Person p1 = {name: "Mike", country: "Germany"};
    Person p2 = {name: "Anne", country: "France"};
    Person p3 = {name: "John", country: "Russia"};
    Person[] persons = [p1, p2, p3];

    xml & readonly res = xml `<data>${
    from var {name, country} in persons
    select xml `${name}`}</data>`;
    any _ = <readonly> res;
    assertEquality(res.toString(), (xml `<data>MikeAnneJohn</data>`).toString());
}

function testXMLTemplateWithQueryExpressionWithReadonly3() {
    Person p1 = {name: "Mike", country: "Germany"};
    Person p2 = {name: "Anne", country: "France"};
    Person p3 = {name: "John", country: "Russia"};
    Person[] persons = [p1, p2, p3];

    xml & readonly res = xml `<data>${
    from var {name, country} in persons.toStream()
    select xml `<person country="${country}">${name}</person>`}</data>`;
    any _ = <readonly> res;
    assertEquality(res.toString(), (xml `<data><person country="Germany">Mike</person><person country="France">Anne</person><person country="Russia">John</person></data>`).toString());
}

function testXMLTemplateWithQueryExpressionWithReadonly4() {
    table<Person> personTable = table key(name) [
            {name: "Mike", country: "Germany"},
            {name: "Anne", country: "France"},
            {name: "John", country: "Russia"}
        ];

    xml & readonly res = xml `<data>${
    from var {name, country} in personTable
    order by country descending
    limit 2
    select xml `<person country="${country}">${name}</person>`}</data>`;
    any _ = <readonly> res;
    assertEquality(res.toString(), (xml `<data><person country="Russia">John</person><person country="Germany">Mike</person></data>`).toString());
}

function testQueryExpressionIteratingOverXMLWithNamespacesWithReadonly() {
    xmlns "foo" as ns;
    xml x = xml `<name><fname ns:status="active">Mike</fname><fname>Jane</fname><lname>Eyre</lname></name>`;

    xml & readonly res = from var fname in x/<fname>
        where fname?.ns:status == "active"
        select fname.cloneReadOnly();
    any _ = <readonly> res;
    assertEquality(res.toString(), (xml `<fname xmlns:ns="foo" ns:status="active">Mike</fname>`).toString());
}

function testQueryExpressionIteratingOverTableReturningXMLWithReadonly() {
    table<Person> personTable = table key(name) [
            {name: "Mike", country: "Germany"},
            {name: "Anne", country: "France"},
            {name: "John", country: "Russia"}
        ];

    xml & readonly res = from var {name, country} in personTable
        order by country descending
        limit 2
        select xml `<person country="${country}">${name}</person>`;
    any _ = <readonly> res;
    assertEquality(res.toString(), (xml `<person country="Russia">John</person><person country="Germany">Mike</person>`).toString());
}

function testQueryExpressionIteratingOverStreamReturningXMLWithReadonly() {
    Person p1 = {name: "Mike", country: "Germany"};
    Person p2 = {name: "Anne", country: "France"};
    Person p3 = {name: "John", country: "Russia"};
    Person[] personList = [p1, p2, p3];

    xml & readonly res = from var {name, country} in personList.toStream()
        order by country descending
        limit 2
        select xml `<person country="${country}">${name}</person>`;
    any _ = <readonly> res;
    assertEquality(res.toString(), (xml `<person country="Russia">John</person><person country="Germany">Mike</person>`).toString());
}

function testQueryExpressionXmlStartWithWhiteSpace() {
    Person[] personList = [{name: "John", country: "Australia"}, {name: "Mike", country : "Canada"}];
    xml xmlValue = xml `
        <html>
            <head>
                <title>Dynamic Table</title>
            </head>
            <body>
                <table border="1">
                    <tr>
                        <th>Name</th>
                        <th>Country</th>
                    </tr>
                    ${from var {name, country} in personList
    select xml ` <tr>
                     <th>${name}</th>
                     <th>${country}</th>
                </tr>`}
                </table>
            </body>
        </html>
    `;
    xml expected = xml `
        <html>
            <head>
                <title>Dynamic Table</title>
            </head>
            <body>
                <table border="1">
                    <tr>
                        <th>Name</th>
                        <th>Country</th>
                    </tr>
                     <tr>
                     <th>John</th>
                     <th>Australia</th>
                </tr> <tr>
                     <th>Mike</th>
                     <th>Canada</th>
                </tr>
                </table>
            </body>
        </html>
    `;
    assertEquality(xmlValue, expected);
}

function assertEquality(any|error actual, any|error expected) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

function assertTrue(any|error actual) {
    assertEquality(actual, true);
}

function assertFalse(any|error actual) {
    assertEquality(actual, false);
}
