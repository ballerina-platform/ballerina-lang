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

function testSimpleQueryExprForStringNegative() returns error? {
    string:Char chr = "a";
    BookGenerator bookGenerator = new ();
    stream<Book, error?> bookStream = new (bookGenerator);

    string:Char[]|string _ = check from Book _ in bookStream
        select chr;
    string:Char[]|int|string _ = check from Book _ in bookStream
        select chr;
}
