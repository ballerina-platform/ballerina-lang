// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

type Book record {|
    string title;
    string author;
|};

function testQueryExprForAmbiguousTypes() returns error? {
    string:Char chr = "a";
    string[] strArr = ["a", "b", "c"];

    string:Char[]|string _ = from var i in strArr select chr;
    string|string:Char[] _ = from var j in strArr select chr;
    [string:Char, string:Char]|int|string|(int|string)[] _ = from var x in ["a", "b", 1, "2"]  select chr;

    Book[] bookList = [];
    xml|xml:Element xmlValue = from Book book in bookList
        select xml `<Book>
                        <Author>${book.author}</Author>
                        <Title>${book.title}</Title>
                    </Book>`;
}

function testQueryExprForErrStream() returns error? {
    int[] numList = [1, 2, 3];
    string:Char chr = "a";
    stream<int, error?> _ = stream from var i in numList select chr;
    stream<int, error?>|string _ = stream from var i in numList select chr;
}
