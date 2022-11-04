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

import simpleclientnegativesix.foo;

client "https://postman-echo.com/get?name=simpleclienttest.yaml" as bar;

foo:client a = new; // error
bar:client b = new ({'limit: 5}); // OK

function testUnquotedClientKeywordUsage() {
    foo:client _ = new; // error
    foo:client[] _ = [new foo:client()]; // error
    bar:client _ = new ({'limit: 5}); // OK
    bar:client[] _ = [new bar:client({'limit: 5})]; // OK

    xmlns "xmlns" as baz;
    _ = baz:client; // error
}
