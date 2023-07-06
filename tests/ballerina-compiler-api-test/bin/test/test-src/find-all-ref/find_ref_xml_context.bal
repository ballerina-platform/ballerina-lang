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

xmlns "http://ballerina.io/b" as ns0;

function test() {
    xmlns "http://ballerina.io/c" as ns1;

    xml x1 = xml `<root ns0:id="456">
                    <foo>123</foo>
                    <bar ns1:status="complete"></bar>
                  </root>`;

    xml x2 = x1.<ns0:*>;
    xml x3 = x1.<ns0:*|ns1:*>;
    xml x4 = x1/<ns1:child>;
    var v1 = x1.ns0:id;
}
