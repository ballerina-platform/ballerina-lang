// Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

import ballerina/test;
import ballerina/jballerina.java;

type Account record {
    string id;
    string name;
    string email;
    string phone;
};

service class AccountService {
    resource function get account/[string account\-id]() returns Account => {
        id: account\-id,
        name: "John Doe",
        email: "john.doe@email.com",
        phone: "1234567890"
    };
    resource function get packages(int? 'limit = ()) returns json => {
        'limit: 'limit,
        body: "hello"
    };
}

public function main() {
    testIdentifierDecoding();
    testEscapeSpecialCharacters();
    testUnescapeSpecialCharacters();
    testFunctionParameters();
}

function testFunctionParameters() {
    test:assertEquals(getParameterName(function(string account\-id) {}), "account-id");
    test:assertEquals(getParameterNameFromResource(AccountService), "account-id");
    test:assertEquals(getParameterDefaultFunctionNameFromResource(AccountService),
    "$AccountService_$get$packages_limit");
}

function testIdentifierDecoding() {
    test:assertEquals(decodeIdentifier("üňĩćőđę_ƈȏɳʂʈ_IL"), "üňĩćőđę_ƈȏɳʂʈ_IL");
    test:assertEquals(decodeIdentifier("const_IL_123"), "const_IL_123");
    test:assertEquals(decodeIdentifier(" &0047&0058@&0091`{~_IL"), " /:@[`{~_IL");
}

function testEscapeSpecialCharacters() {
    test:assertEquals(escapeSpecialCharacters("'version"), "\\'version");
    test:assertEquals(escapeSpecialCharacters(" ' $0047$0058@$0091`{~_IL"), "\\ \\'\\ \\$0047\\$0058\\@\\$0091\\`\\{\\~_IL");
    test:assertEquals(escapeSpecialCharacters("üňĩćőđę_ƈȏɳʂʈ_IL"), "üňĩćőđę_ƈȏɳʂʈ_IL");
    test:assertEquals(escapeSpecialCharacters("'ü?ňĩ$ć%őđ&ę_ƈ-ȏɳ+ʂʈ_=L#"), "\\'ü\\?ňĩ\\$ć\\%őđ\\&ę_ƈ\\-ȏɳ\\+ʂʈ_\\=L\\#");
    test:assertEquals(escapeSpecialCharacters("\u{03C0}A9őđ&ę\u{00A9}\u{00AE}\u{2122}@3)^~`\u{03A3}\u{03A8}"), "πA9őđ\\&ę©®™\\@3\\)\\^\\~\\`ΣΨ");
    test:assertEquals(escapeSpecialCharacters("\u{1f479}\u{1F47A}\u{1f47B}\u{1F63A}\u{1F408}\u{1F981}\u{1F600}"), "👹👺👻😺🐈🦁😀");
    test:assertEquals(escapeSpecialCharacters("\u{1f479}'\u{1F47A}`\u{1f47B}&\u{1F63A}\u{1F408}\u{1F981}\u{1F600}"), "👹\\'👺\\`👻\\&😺🐈🦁😀");
}

function testUnescapeSpecialCharacters() {
    test:assertEquals(unescapeSpecialCharacters("\\'version"), "'version");
    test:assertEquals(unescapeSpecialCharacters("\\ \\'\\ \\$0047\\$0058\\@\\$0091\\`\\{\\~_IL"), " ' $0047$0058@$0091`{~_IL");
    test:assertEquals(unescapeSpecialCharacters("\\'ü\\?ňĩ\\$ć\\%őđ\\&ę_ƈ\\-ȏɳ\\+ʂʈ_\\=L\\#"), "'ü?ňĩ$ć%őđ&ę_ƈ-ȏɳ+ʂʈ_=L#");
    test:assertEquals(unescapeSpecialCharacters("πA9őđ\\&ę©®™\\@3\\)\\^\\~\\`ΣΨ"), "πA9őđ&ę©®™@3)^~`ΣΨ");
    test:assertEquals(unescapeSpecialCharacters("πA9őđ\\&ę©®™\\@3\\)\\^\\~\\`ΣΨ"), "\u{03C0}A9őđ&ę\u{00A9}\u{00AE}\u{2122}@3)^~`\u{03A3}\u{03A8}");
    test:assertEquals(unescapeSpecialCharacters("👹👺👻😺🐈🦁😀\u{1f920}👐"), "👹👺👻😺🐈🦁😀🤠👐");
    test:assertEquals(unescapeSpecialCharacters("\\👹👺\\👻😺🐈\\\\🦁😀\\\\\u{1f920}👐"), "👹👺👻😺🐈\\🦁😀\\🤠👐");
    test:assertEquals(unescapeSpecialCharacters("\\u{1f479}\\u{1F47A}\u{1f47B}\\\u{1F63A}\\\\\\\u{1F408}\u{1F981}\\\\\u{1F600}\\🤠\\u{1F450}\\"), "👹👺👻😺\\🐈🦁\\😀🤠👐");
}

function decodeIdentifier(string s) returns string = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function escapeSpecialCharacters(string s) returns string = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function unescapeSpecialCharacters(string s) returns string = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function getParameterName(function f) returns string = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function getParameterNameFromResource(typedesc<any> serviceObj) returns string = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function getParameterDefaultFunctionNameFromResource(typedesc<any> serviceObj) returns string = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;
