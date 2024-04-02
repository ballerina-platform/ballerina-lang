// Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

import testorg/foo;

xmlns "http://ballerinalang.org" as ns;

function testXmlRequiredAttributeAccessNegative() returns error? {
    xml x = xml `<a ns:A="content" id="identity"></a>`;

    string _ = check x.foo:XMLD; // const exist but not public
    string _ = check x.foo:XMLE; // Referred value not a const
    string _ = check x.foo:XMLG; // non-existing const
    string _ = check x.bar:Y; // non-existing prefix
}

function testXmlOptionalAttributeAccessNegative() returns error? {
    xml x = xml `<a ns:A="content" id="identity"></a>`;

    string? _ = check x?.foo:XMLD;
    string? _ = check x?.foo:XMLE;
    string? _ = check x?.foo:XMLG;
    string? _ = check x?.bar:Y;
}
