// Copyright (c) 2023 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/lang.regexp;

public function testNegativeIndexFind() returns error? {
    string:RegExp r = re `e`;
    _ = check (trap r.find("Hello", -3));
}

public function testInvalidIndexFind() returns error? {
    string:RegExp r = re `r`;
    _ = check (trap r.find("World", 12));
}

public function testLongIndexFind() returns error? {
    string:RegExp r = re `r`;
    _ = check (trap r.find("World", 68719476704));
}

public function testNegativeIndexFindAll() returns error? {
    string:RegExp r = re `/(g|ng)/gim`;
    string phrase = "There once was a king who liked to sing in the rain";
    _ = check (trap r.findAll(phrase, -8));
}

public function testInvalidIndexFindAll() returns error? {
    string:RegExp r = re `/(op|p)/gim`;
    string phrase = "Don't stoP going up till you get to the tOp if you want to shop";
    _ = check (trap r.findAll(phrase, 112));
}

public function testLongIndexFindAll() returns error? {
    string:RegExp r = re `/(ab|c)/gim`;
    string phrase = "Don't stoP going up till you get to the tOp if you want to shop";
    _ = check (trap r.findAll(phrase, 137438953408));
}

public function testNegativeIndexFindGroups() returns error? {
    string:RegExp r = re `([bB].tt[a-z]*)`;
    _ = check (trap r.findGroups("Butter was bought by Betty but the butter was bitter", -3));
}

public function testInvalidIndexFindGroups() returns error? {
    string:RegExp r = re `([bB].tt[a-z]*)`;
    _ = check (trap r.findGroups("Butter was bought by Betty but the butter was bitter", 97));
}

public function testLongIndexFindGroups() returns error? {
    string:RegExp r = re `([aA].ee[A-Z]*)`;
    _ = check (trap r.findGroups("Butter was bought by Betty but the butter was bitter", 274877906816));
}

public function testNegativeIndexFindAllGroups() returns error? {
    string:RegExp r = re `(([a-z]u)(bble))`;
    _ = check (trap r.findAllGroups("rubble, trouble, bubble, hubble", -4));
}

public function testInvalidIndexFindAllGroups() returns error? {
    string:RegExp r = re `(([a-z]u)(bble))`;
    _ = check (trap r.findAllGroups("rubble, trouble, bubble, hubble", 123));
}

public function testLongIndexFindAllGroups() returns error? {
    string:RegExp r = re `(([0-9A-Z]u)+(ful))`;
    _ = check (trap r.findAllGroups("rubble, trouble, bubble, hubble", 549755813632));
}

public function testInvalidRegexpPatternSyntax1() returns error? {
    string:RegExp x = re `(?i-s:[[A\\sB\WC\Dd\\]\])`;
    _ = check (trap x.findAll(":*1*7").toBalString());
}

public function testInvalidRegexpPatternSyntax2() returns error? {
    string:RegExp x = re `(?xsmi:[]\P{sc=Braille})`;
    _ = check (trap x.findAll(":*A*a").toBalString());
}

public function testInvalidRegexpPatternSyntax3() returns error? {
    string:RegExp x = re `(?xsmi:[]\P{sc=Braille})`;
    _ = check (trap x.matchGroupsAt(":*A*a"));
}

public function testNegativeDuplicateFlags1() returns error? {
    anydata|error result = trap re `${"(?i-i:ABC))"}`;
    check assertEqualMessage(result, "Invalid insertion in regular expression: duplicate flag 'i'");
}

public function testNegativeDuplicateFlags2() returns error? {
    anydata|error result = trap re `ABC${"(?ims-xi:ABC))"}(?-:)`;
    check assertEqualMessage(result, "Invalid insertion in regular expression: duplicate flag 'i'");
}

public function testNegativeDuplicateFlags3() returns error? {
    anydata|error result = regexp:fromString("(?ms-ims:ABC))");
    check assertEqualMessage(result, "Failed to parse regular expression: duplicate flag 'm'");
}

public function testNegativeDuplicateFlags4() returns error? {
    anydata|error result = regexp:fromString("(?imi:(ABC))");
    check assertEqualMessage(result, "Failed to parse regular expression: duplicate flag 'i'");
}

public function testNegativeDuplicateFlags5() returns error? {
    anydata|error result = regexp:fromString("(?-imi:(ABC))");
    check assertEqualMessage(result, "Failed to parse regular expression: duplicate flag 'i'");
}

public function testNegativeInvalidFlags1() returns error? {
    anydata|error result = trap re `${"(?i-mk:ABC))"}`;
    check assertEqualMessage(result, "Invalid insertion in regular expression: invalid flag in regular expression");
}

public function testNegativeInvalidFlags2() returns error? {
    anydata|error result = trap re `ABC${"(?lms-xi:ABC))"}(?-:)`;
    check assertEqualMessage(result, "Invalid insertion in regular expression: invalid flag in regular expression");
}

public function testNegativeInvalidFlags3() returns error? {
    anydata|error result = regexp:fromString("(?ls-ims:ABC))");
    check assertEqualMessage(result, "Failed to parse regular expression: invalid flag in regular expression");
}

public function testNegativeInvalidFlags4() returns error? {
    anydata|error result = regexp:fromString("(?ii-mk:ABC))");
    check assertEqualMessage(result, "Failed to parse regular expression: invalid flag in regular expression");
}

function assertEqualMessage(anydata|error actual, anydata|error expected) returns error? {
    anydata expectedValue = (expected is error)? check (<error> expected).detail().get("message").ensureType() : expected;
    anydata actualValue = (actual is error)? check (<error> actual).detail().get("message").ensureType() : actual;
    if expectedValue == actualValue {
        return;
    }
    panic error(string `expected '${expectedValue.toBalString()}', found '${actualValue.toBalString()}'`);
}
