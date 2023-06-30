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
    any|error? err = trap r.find("Hello", -3);
    assertTrue(err is error);
    check assertEqualMessage(<error> err, "start index cannot be less than 0");
}

public function testInvalidIndexFind() returns error? {
    string:RegExp r = re `r`;
    any|error? err = trap r.find("World", 12);
    assertTrue(err is error);
    check assertEqualMessage(<error> err, "start index '12' cannot be greater than input string length '5'");
}

public function testLongIndexFind() returns error? {
    string:RegExp r = re `r`;
    any|error? err = trap r.find("World", 68719476704);
    assertTrue(err is error);
    check assertEqualMessage(<error> err, "index number too large: 68,719,476,704");
}

public function testNegativeIndexFindAll() returns error? {
    string:RegExp r = re `/(g|ng)/gim`;
    string phrase = "There once was a king who liked to sing in the rain";
    any|error? err = trap r.findAll(phrase, -8);
    assertTrue(err is error);
    check assertEqualMessage(<error> err, "start index cannot be less than 0");
}

public function testInvalidIndexFindAll() returns error? {
    string:RegExp r = re `/(op|p)/gim`;
    string phrase = "Don't stoP going up till you get to the tOp if you want to shop";
    any|error? err = trap r.findAll(phrase, 112);
    assertTrue(err is error);
    check assertEqualMessage(<error> err, "start index '112' cannot be greater than input string length '63'");
}

public function testLongIndexFindAll() returns error? {
    string:RegExp r = re `/(ab|c)/gim`;
    string phrase = "Don't stoP going up till you get to the tOp if you want to shop";
    any|error? err = trap r.findAll(phrase, 137438953408);
    assertTrue(err is error);
    check assertEqualMessage(<error> err, "index number too large: 137,438,953,408");
}

public function testNegativeIndexFindGroups() returns error? {
    string:RegExp r = re `([bB].tt[a-z]*)`;
    any|error? err = trap r.findGroups("Butter was bought by Betty but the butter was bitter", -3);
    assertTrue(err is error);
    check assertEqualMessage(<error> err, "start index cannot be less than 0");
}

public function testInvalidIndexFindGroups() returns error? {
    string:RegExp r = re `([bB].tt[a-z]*)`;
    any|error? err = trap r.findGroups("Butter was bought by Betty but the butter was bitter", 97);
    assertTrue(err is error);
    check assertEqualMessage(<error> err, "start index '97' cannot be greater than input string length '52'");
}

public function testLongIndexFindGroups() returns error? {
    string:RegExp r = re `([aA].ee[A-Z]*)`;
    any|error? err = trap r.findGroups("Butter was bought by Betty but the butter was bitter", 274877906816);
    assertTrue(err is error);
    check assertEqualMessage(<error> err, "index number too large: 274,877,906,816");
}

public function testNegativeIndexFindAllGroups() returns error? {
    string:RegExp r = re `(([a-z]u)(bble))`;
    any|error? err = trap r.findAllGroups("rubble, trouble, bubble, hubble", -4);
    assertTrue(err is error);
    check assertEqualMessage(<error> err, "start index cannot be less than 0");
}

public function testInvalidIndexFindAllGroups() returns error? {
    string:RegExp r = re `(([a-z]u)(bble))`;
    any|error? err = trap r.findAllGroups("rubble, trouble, bubble, hubble", 123);
    assertTrue(err is error);
    check assertEqualMessage(<error> err, "start index '123' cannot be greater than input string length '31'");
}

public function testLongIndexFindAllGroups() returns error? {
    string:RegExp r = re `(([0-9A-Z]u)+(ful))`;
    any|error? err = trap r.findAllGroups("rubble, trouble, bubble, hubble", 549755813632);
    assertTrue(err is error);
    check assertEqualMessage(<error> err, "index number too large: 549,755,813,632");
}

public function testNegativeIndexMatchAt() returns error? {
    string:RegExp r = re `e`;
    any|error? err = trap r.matchAt("Hello", -3);
    assertTrue(err is error);
    check assertEqualMessage(<error> err, "start index cannot be less than 0");
}

public function testInvalidIndexMatchAt() returns error? {
    string:RegExp r = re `e`;
    any|error? err = trap r.matchAt("World", 12);
    assertTrue(err is error);
    check assertEqualMessage(<error> err, "start index '12' cannot be greater than input string length '5'");
}

public function testLongIndexMatchAt() returns error? {
    string:RegExp r = re `e`;
    any|error? err = trap r.matchAt("World", 68719476704);
    assertTrue(err is error);
    check assertEqualMessage(<error> err, "start index cannot be less than 0");
}

public function testNegativeIndexMatchGroupsAt() returns error? {
    string:RegExp r = re `e`;
    any|error? err = trap r.matchGroupsAt("Hello", -3);
    assertTrue(err is error);
    check assertEqualMessage(<error> err, "start index cannot be less than 0");
}

public function testInvalidIndexMatchGroupsAt() returns error? {
    string:RegExp r = re `e`;
    any|error? err = trap r.matchGroupsAt("World", 12);
    assertTrue(err is error);
    check assertEqualMessage(<error> err, "start index '12' cannot be greater than input string length '5'");
}

public function testLongIndexMatchGroupsAt() returns error? {
    string:RegExp r = re `e`;
    any|error? err = trap r.matchGroupsAt("World", 68719476704);
    assertTrue(err is error);
    check assertEqualMessage(<error> err, "start index cannot be less than 0");
}

public function testNegativeIndexReplace() returns error? {
    string:RegExp r = re `e`;
    any|error? err = trap r.replace("Hello", "", -3);
    assertTrue(err is error);
    check assertEqualMessage(<error> err, "start index cannot be less than 0");
}

public function testInvalidIndexReplace() returns error? {
    string:RegExp r = re `e`;
    any|error? err = trap r.replace("World", "", 12);
    assertTrue(err is error);
    check assertEqualMessage(<error> err, "start index '12' cannot be greater than input string length '5'");
}

public function testLongIndexReplace() returns error? {
    string:RegExp r = re `e`;
    any|error? err = trap r.replace("World", "", 68719476704);
    assertTrue(err is error);
    check assertEqualMessage(<error> err, "index number too large: 68,719,476,704");
}

public function testNegativeIndexReplaceAll() returns error? {
    string:RegExp r = re `e`;
    any|error? err = trap r.replace("Hello", "", -3);
    assertTrue(err is error);
    check assertEqualMessage(<error> err, "start index cannot be less than 0");
}

public function testInvalidIndexReplaceAll() returns error? {
    string:RegExp r = re `e`;
    any|error? err = trap r.replace("World", "", 12);
    assertTrue(err is error);
    check assertEqualMessage(<error> err, "start index '12' cannot be greater than input string length '5'");
}

public function testLongIndexReplaceAll() returns error? {
    string:RegExp r = re `e`;
    any|error? err = trap r.replace("World", "", 68719476704);
    assertTrue(err is error);
    check assertEqualMessage(<error> err, "index number too large: 68,719,476,704");
}

public function testInvalidRegexpPatternSyntax1() returns error? {
    string:RegExp x = re `(?i-s:[[A\\sB\WC\Dd\\]\])`;
    _ = check (trap x.findAll(":*1*7").toBalString());
}

public function testInvalidRegexpPatternSyntax2() returns error? {
    string:RegExp x = re `(?xsmi:[[ABC]\P{sc=Braille})`;
    _ = check (trap x.findAll(":*A*a").toBalString());
}

public function testInvalidRegexpPatternSyntax3() returns error? {
    string:RegExp x = re `(?xsmi:[[ABC]\P{sc=Braille})`;
    _ = check (trap x.matchGroupsAt(":*A*a"));
}

public function testNegativeEmptyCharClass1() returns error? {
    string pattern = "[]";
    anydata|error result = trap re `${pattern}`;
    check assertEqualMessage(result, "empty character class disallowed in insertion substring '[]'");
}

public function testNegativeEmptyCharClass2() returns error? {
    anydata|error result = trap re `(abc${"[]"})`;
    check assertEqualMessage(result, "empty character class disallowed in insertion substring '[]'");
}

public function testNegativeEmptyCharClass3() returns error? {
    anydata|error result = trap regexp:fromString("(([abc])|([]))");
    check assertEqualMessage(result, "Failed to parse regular expression: empty character class disallowed in '(([abc])|([]))'");
}

public function testNegativeDuplicateFlags1() returns error? {
    anydata|error result = trap re `${"(?i-i:ABC)"}`;
    check assertEqualMessage(result, "duplicate flag 'i' in insertion substring '(?i-i:ABC)'");
}

public function testNegativeDuplicateFlags2() returns error? {
    anydata|error result = trap re `ABC${"(?ims-xi:ABC)"}(?-:)`;
    check assertEqualMessage(result, "duplicate flag 'i' in insertion substring '(?ims-xi:ABC)'");
}

public function testNegativeDuplicateFlags3() returns error? {
    anydata|error result = regexp:fromString("(?ms-ims:ABC)");
    check assertEqualMessage(result, "Failed to parse regular expression: duplicate flag 'm' in '(?ms-ims:ABC)'");
}

public function testNegativeDuplicateFlags4() returns error? {
    anydata|error result = regexp:fromString("(?imi:(ABC))");
    check assertEqualMessage(result, "Failed to parse regular expression: duplicate flag 'i' in '(?imi:(ABC))'");
}

public function testNegativeDuplicateFlags5() returns error? {
    anydata|error result = regexp:fromString("(?-imi:(ABC))");
    check assertEqualMessage(result, "Failed to parse regular expression: duplicate flag 'i' in '(?-imi:(ABC))'");
}

public function testNegativeInvalidFlags1() returns error? {
    anydata|error result = trap re `${"(?i-mk:ABC)"}`;
    check assertEqualMessage(result, "invalid flag 'k' in insertion substring '(?i-mk:ABC)'");
}

public function testNegativeInvalidFlags2() returns error? {
    anydata|error result = trap re `ABC${"(?lms-xi:ABC))"}(?-:)`;
    check assertEqualMessage(result, "invalid flag 'l' in insertion substring '(?lms-xi:ABC))'");
}

public function testNegativeInvalidFlags3() returns error? {
    anydata|error result = regexp:fromString("(?ls-ims:ABC))");
    check assertEqualMessage(result, "Failed to parse regular expression: invalid flag 'l' in '(?ls-ims:ABC))'");
}

public function testNegativeInvalidFlags4() returns error? {
    anydata|error result = regexp:fromString("(?ii-mk:ABC))");
    check assertEqualMessage(result, "Failed to parse regular expression: invalid flag 'k' in '(?ii-mk:ABC))'");
}

function assertEqualMessage(anydata|error actual, anydata|error expected) returns error? {
    anydata expectedValue = (expected is error)? check (<error> expected).detail().get("message").ensureType() : expected;
    anydata actualValue = (actual is error)? check (<error> actual).detail().get("message").ensureType() : actual;
    if expectedValue == actualValue {
        return;
    }
    panic error(string `expected '${expectedValue.toBalString()}', found '${actualValue.toBalString()}'`);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}
