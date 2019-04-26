// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testXML() returns (xml, any, any, xml, xml, xml) {
    xmlns "http://wso2.com/" as ns0;

    xml x = xml `<ns0:foo a="hello world" xmlns:ns1="http://ballerinalang.org/"><ns1:bar1>hello1</ns1:bar1><bar2>hello2</bar2></ns0:foo>`;

    x@[ns0:b] = "active";
    xml x2 = x["{http://ballerinalang.org/}bar1"];
    return (x, x@, x@[ns0:b], x.*, x2, x2[0]);
}
