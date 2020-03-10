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

function testXmlComment() returns int {
     xml x1 = xml `<!--h🥺llo-->`;
     return x1.toString().length();
}

function testXmlQName() returns int {
     xml x1 = xml `<book/>`;
     return x1.toString().length();
}

function testXmlText() returns int {
     xml x1 = xml `<book>Netty🔥</book>`;
     return x1.toString().length();
}

function testXmlProcessingIns() returns int {
     xml x1 = xml `<?hi name😂?>`;
     return x1.toString().length();
}

function testXmlStr() returns int {
     xml x1 = xml `hi name😊`;
     return x1.toString().length();
}

function testComplexXml() returns int {
     xml x1 = xml `<book>
                         <name>Sherlock😀 Holmes</name>
                         <author>Sir Arthur🙈 Conan Doyle</author>
                         <!--Price: 🍇$10-->
                       </book>`;
     return x1.toString().length();
}

function testXmlNamespace() returns int {
    xmlns "http://ballerina.com/";
    xmlns "http://ballerina.com/💯aa" as ns0;
    xml x1 = xml `<book ns0:status="available">
                                 <ns0:name>Sherlock🕴️ Holmes</ns0:name>
                                 <author>Sir Arthur🚣 Conan Doyle</author>
                                 <!--Price: 🕳️$10-->
                               </book>`;
    return x1.toString().length();
}

function testXmlInterpolation() returns int {
    xmlns "http://ballerina.com/💯aa" as ns0;
    string title = "(Sir)";

        xml x1 = xml `<ns0:newBook>
                        <name>Sherlock Hol🏁mes</name>
                        <author>${title} Arthur Conan Doyle</author>
                        <!--Price: $${ 40 / 5 + 4 }-->
                      </ns0:newBook>`;
    return x1.toString().length();
}

function testXmlAttributes() returns int {
    xmlns "http://ballerina.com/💯aa" as ns0;
        xml x1 = xml `<ns0:book ns0:status="available" count="5"/>`;
    return x1.toString().length();
}
