// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testSimpleQueryExprForXMLWithReadonly1() {
    xml book1 = xml `<name>Sherlock Holmes</name>`;

    xml & readonly _ = from var x in book1
        select x;

    xml:Element & readonly _ = from var x in book1
        where x is xml:Element
        select x;

    xml<xml:Element> & readonly _ = from xml x in book1
        where x is xml:Element
        select x;

    xml:Comment[] comments = [xml `<!--This is a comment 1-->`, xml `<!--This is a comment 1-->`];

    xml & readonly _ = from xml x in comments
        select x;

    xml:Comment & readonly _ = from var x in comments
        select x;

    xml<xml:Comment> & readonly _ = from xml:Comment x in comments
        select x;

    xml:ProcessingInstruction[] processingInstructions = [xml `<?target data?>`, xml `<?target url?>`];

    xml & readonly _ = from xml x in processingInstructions
        select x;

    xml:ProcessingInstruction & readonly _ = from var x in processingInstructions
        select x;

    xml<xml:ProcessingInstruction> & readonly _ = from xml:ProcessingInstruction x in processingInstructions
        select x;
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
        } else if (self.i >= 3) {
            return ();
        }
        return {
            value: {
                title: "Title " + self.i.toString(), author: "Author " + self.i.toString()
            }
        };
    }
}

BookGenerator bookGenerator = new ();
stream<Book, error?> bookStream = new (bookGenerator);

function testSimpleQueryExprForXML() {
    xml:Element _ = check from Book book in bookStream
        select xml `<Book>
                        <Author>${book.author}</Author>
                        <Title>${book.title}</Title>
                    </Book>`;

    xml:Element|xml _ = check from Book book in bookStream
        select xml `<Book>
                        <Author>${book.author}</Author>
                        <Title>${book.title}</Title>
                    </Book>`;

    xml:Text|xml _ = check from Book book in bookStream
        select xml `<Book>
                        <Author>${book.author}</Author>
                        <Title>${book.title}</Title>
                    </Book>`;
    xml:Element[]|xml _ = check from Book book in bookStream
        select xml `<Book>
                        <Author>${book.author}</Author>
                        <Title>${book.title}</Title>
                    </Book>`;
}
