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

function testRegExpValueWithLiterals() {
    string:RegExp x1 = re `A`;
    assertEquality("A", x1.toString());

    string:RegExp x2 = re `ABC`;
    assertEquality("ABC", x2.toString());

    string:RegExp x3 = re `AB+C*`;
    assertEquality("AB+C*", x3.toString());

    string:RegExp x4 = re `AB+C*D{1}`;
    assertEquality("AB+C*D{1}", x4.toString());

    string:RegExp x5 = re `AB+C*D{1,}`;
    assertEquality("AB+C*D{1,}", x5.toString());

    string:RegExp x6 = re `AB+C*D{1,4}`;
    assertEquality("AB+C*D{1,4}", x6.toString());

    string:RegExp x7 = re `A?B+C*?D{1,4}`;
    assertEquality("A?B+C*?D{1,4}", x7.toString());
}

function testRegExpValueWithEscapes() {
    string:RegExp x1 = re `\r\n\^`;
    assertEquality("\\r\\n\\^", x1.toString());

    string:RegExp x2 = re `\*\d`;
    assertEquality("\\*\\d", x2.toString());

    string:RegExp x3 = re `A\sB\WC\Dd\\`;
    assertEquality("A\\sB\\WC\\Dd\\\\", x3.toString());

    string:RegExp x4 = re `\s{1}\p{sc=Braille}*`;
    assertEquality("\\s{1}\\p{sc=Braille}*", x4.toString());

    string:RegExp x5 = re `AB+\p{gc=Lu}{1,}`;
    assertEquality("AB+\\p{gc=Lu}{1,}", x5.toString());

    string:RegExp x6 = re `A\p{Lu}??B+\W\(+?C*D{1,4}?`;
    assertEquality("A\\p{Lu}??B+\\W\\(+?C*D{1,4}?", x6.toString());

    string:RegExp x7 = re `\p{sc=Latin}\p{gc=Lu}\p{Lt}\tA+?\)*`;
    assertEquality("\\p{sc=Latin}\\p{gc=Lu}\\p{Lt}\\tA+?\\)*", x7.toString());
}

function testRegExpValueWithCharacterClass() {
    string:RegExp x1 = re `[\r\n\^]`;
    assertEquality("[\\r\\n\\^]", x1.toString());

    string:RegExp x2 = re `[\*\d]`;
    assertEquality("[\\*\\d]", x2.toString());

    string:RegExp x3 = re `[A\sB\WC\Dd\\]`;
    assertEquality("[A\\sB\\WC\\Dd\\\\]", x3.toString());

    string:RegExp x4 = re `[\s\p{sc=Braille}]`;
    assertEquality("[\\s\\p{sc=Braille}]", x4.toString());

    string:RegExp x5 = re `[AB\p{gc=Lu}]+?`;
    assertEquality("[AB\\p{gc=Lu}]+?", x5.toString());

    string:RegExp x6 = re `[A\p{Lu}B\W\(CD]*`;
    assertEquality("[A\\p{Lu}B\\W\\(CD]*", x6.toString());

    string:RegExp x7 = re `[\p{sc=Latin}\p{gc=Lu}\p{Lt}\tA\)]??`;
    assertEquality("[\\p{sc=Latin}\\p{gc=Lu}\\p{Lt}\\tA\\)]??", x7.toString());
}

function testRegExpValueWithCharacterClass2() {
    string:RegExp x1 = re `[^\r\n\^a-z]`;
    assertEquality("[^\\r\\n\\^a-z]", x1.toString());

    string:RegExp x2 = re `[\*a-d\de-f]`;
    assertEquality("[\\*a-d\\de-f]", x2.toString());

    string:RegExp x3 = re `[A\sA-GB\WC\DJ-Kd\\]*`;
    assertEquality("[A\\sA-GB\\WC\\DJ-Kd\\\\]*", x3.toString());

    string:RegExp x4 = re `[\sA-F\p{sc=Braille}K-Mabc-d\--]`;
    assertEquality("[\\sA-F\\p{sc=Braille}K-Mabc-d\\--]", x4.toString());

    string:RegExp x5 = re `[A-B\p{gc=Lu}\s-\SAD\s-\w]`;
    assertEquality("[A-B\\p{gc=Lu}\\s-\\SAD\\s-\\w]", x5.toString());

    string:RegExp x6 = re `[\s-\wA\p{Lu}B\W\(CDA-B]+?`;
    assertEquality("[\\s-\\wA\\p{Lu}B\\W\\(CDA-B]+?", x6.toString());

    string:RegExp x7 = re `[\p{Lu}-\w\p{sc=Latin}\p{gc=Lu}\p{Lu}-\w\p{Lt}\tA\)\p{Lu}-\w]{12,32}?`;
    assertEquality("[\\p{Lu}-\\w\\p{sc=Latin}\\p{gc=Lu}\\p{Lu}-\\w\\p{Lt}\\tA\\)\\p{Lu}-\\w]{12,32}?", x7.toString());
}

function testRegExpValueWithCapturingGroups() {
    string:RegExp x1 = re `(?:ABC)`;
    assertEquality("(?:ABC)", x1.toString());

    string:RegExp x2 = re `(?i:ABC)`;
    assertEquality("(?i:ABC)", x2.toString());

    string:RegExp x3 = re `(?i-m:AB+C*)`;
    assertEquality("(?i-m:AB+C*)", x3.toString());

    string:RegExp x4 = re `(?im-sx:AB+C*D{1})`;
    assertEquality("(?im-sx:AB+C*D{1})", x4.toString());

    string:RegExp x5 = re `(?:AB+C*D{1,})`;
    assertEquality("(?:AB+C*D{1,})", x5.toString());

    string:RegExp x6 = re `(?imxs:AB+C*D{1,4})`;
    assertEquality("(?imxs:AB+C*D{1,4})", x6.toString());

    string:RegExp x7 = re `(?imx-s:A?B+C*?D{1,4})`;
    assertEquality("(?imx-s:A?B+C*?D{1,4})", x7.toString());
}

function testRegExpValueWithCapturingGroups2() {
    string:RegExp x1 = re `(\r\n\^)`;
    assertEquality("(\\r\\n\\^)", x1.toString());

    string:RegExp x2 = re `(?:\*\d)`;
    assertEquality("(?:\\*\\d)", x2.toString());

    string:RegExp x3 = re `(?i:A\sB\WC\Dd\\)`;
    assertEquality("(?i:A\\sB\\WC\\Dd\\\\)", x3.toString());

    string:RegExp x4 = re `(?i-s:\s{1}\p{sc=Braille}*)`;
    assertEquality("(?i-s:\\s{1}\\p{sc=Braille}*)", x4.toString());

    string:RegExp x5 = re `(?ismx:AB+\p{gc=Lu}{1,})`;
    assertEquality("(?ismx:AB+\\p{gc=Lu}{1,})", x5.toString());

    string:RegExp x6 = re `(?im-sx:A\p{Lu}??B+\W\(+?C*D{1,4}?)`;
    assertEquality("(?im-sx:A\\p{Lu}??B+\\W\\(+?C*D{1,4}?)", x6.toString());

    string:RegExp x7 = re `(?ims-x:\p{sc=Latin}\p{gc=Lu}\p{Lt}\tA+?\)*)`;
    assertEquality("(?ims-x:\\p{sc=Latin}\\p{gc=Lu}\\p{Lt}\\tA+?\\)*)", x7.toString());
}

function testRegExpValueWithCapturingGroups3() {
    string:RegExp x1 = re `([\r\n\^])`;
    assertEquality("([\\r\\n\\^])", x1.toString());

    string:RegExp x2 = re `(?i:[\*\d])`;
    assertEquality("(?i:[\\*\\d])", x2.toString());

    string:RegExp x3 = re `(?i-s:[A\sB\WC\Dd\\])`;
    assertEquality("(?i-s:[A\\sB\\WC\\Dd\\\\])", x3.toString());

    string:RegExp x4 = re `(?ix:sm:[\s\p{sc=Braille}])`;
    assertEquality("(?ix:sm:[\\s\\p{sc=Braille}])", x4.toString());

    string:RegExp x5 = re `(?isxm:[AB\p{gc=Lu}]+?)`;
    assertEquality("(?isxm:[AB\\p{gc=Lu}]+?)", x5.toString());

    string:RegExp x6 = re `(?isx-m:[A\p{Lu}B\W\(CD]*)`;
    assertEquality("(?isx-m:[A\\p{Lu}B\\W\\(CD]*)", x6.toString());

    string:RegExp x7 = re `([\p{sc=Latin}\p{gc=Lu}\p{Lt}\tA\)]??)`;
    assertEquality("([\\p{sc=Latin}\\p{gc=Lu}\\p{Lt}\\tA\\)]??)", x7.toString());
}

function testRegExpValueWithCapturingGroups4() {
    string:RegExp x1 = re `([^\r\n\^a-z])`;
    assertEquality("([^\\r\\n\\^a-z])", x1.toString());

    string:RegExp x2 = re `(?i:[\*a-d\de-f])`;
    assertEquality("(?i:[\\*a-d\\de-f])", x2.toString());

    string:RegExp x3 = re `(?im-sx:[A\sA-GB\WC\DJ-Kd\\]*)`;
    assertEquality("(?im-sx:[A\\sA-GB\\WC\\DJ-Kd\\\\]*)", x3.toString());

    string:RegExp x4 = re `(?i-s:[\sA-F\p{sc=Braille}K-Mabc-d\--])`;
    assertEquality("(?i-s:[\\sA-F\\p{sc=Braille}K-Mabc-d\\--])", x4.toString());

    string:RegExp x5 = re `(?imx-s:[A-B\p{gc=Lu}\s-\SAD\s-\w])`;
    assertEquality("(?imx-s:[A-B\\p{gc=Lu}\\s-\\SAD\\s-\\w])", x5.toString());

    string:RegExp x6 = re `(?imxs:[\s-\wA\p{Lu}B\W\(CDA-B]+?)`;
    assertEquality("(?imxs:[\\s-\\wA\\p{Lu}B\\W\\(CDA-B]+?)", x6.toString());

    string:RegExp x7 = re `(?i-sxm:[\p{Lu}-\w\p{sc=Latin}\p{gc=Lu}\p{Lu}-\w\p{Lt}\tA\)\p{Lu}-\w]{12,32}?)`;
    assertEquality("(?i-sxm:[\\p{Lu}-\\w\\p{sc=Latin}\\p{gc=Lu}\\p{Lu}-\\w\\p{Lt}\\tA\\)\\p{Lu}-\\w]{12,32}?)", x7.toString());
}

function testRegExpValueWithCapturingGroups5() {
    string:RegExp x1 = re `((ab)|(a))((c)|(bc))`;
    assertEquality("((ab)|(a))((c)|(bc))", x1.toString());

    string:RegExp x2 = re `(?:ab|cd)+|ef`;
    assertEquality("(?:ab|cd)+|ef", x2.toString());

    string:RegExp x3 = re `(?:(?i-m:ab|cd))+|ef`;
    assertEquality("(?:(?i-m:ab|cd))+|ef", x3.toString());

    string:RegExp x4 = re `(?:(?i-m:ab|cd)|aa|abcdef[a-zefg-ijk-]|ba|b|c{1,3}^)+|ef`;
    assertEquality("(?:(?i-m:ab|cd)|aa|abcdef[a-zefg-ijk-]|ba|b|c{1,3}^)+|ef", x4.toString());

    string:RegExp x5 = re `(z)((a+)?(b+)?(c))*`;
    assertEquality("(z)((a+)?(b+)?(c))*", x5.toString());

    string:RegExp x6 = re `(?:)`;
    assertEquality("(?:)", x6.toString());

    string:RegExp x7 = re `(?i:a|b)`;
    assertEquality("(?i:a|b)", x7.toString());

    string:RegExp x8 = re `(?im:a|b|[])`;
    assertEquality("(?im:a|b|[])", x8.toString());

    string:RegExp x9 = re `(?i-m:[0-9])`;
    assertEquality("(?i-m:[0-9])", x9.toString());

    string:RegExp x10 = re `(?im-s:[z-z])`;
    assertEquality("(?im-s:[z-z])", x10.toString());

    string:RegExp x11 = re `(?im-sx:abc{1})`;
    assertEquality("(?im-sx:abc{1})", x11.toString());

    string:RegExp x12 = re `(?im:\\u0042)`;
    assertEquality("(?im:\\\\u0042)", x12.toString());

    string:RegExp x13 = re `java(script)?`;
    assertEquality("java(script)?", x13.toString());

    string:RegExp x14 = re `(x*)(x+)`;
    assertEquality("(x*)(x+)", x14.toString());

    string:RegExp x15= re `(\d*)(\d+)`;
    assertEquality("(\\d*)(\\d+)", x15.toString());

    string:RegExp x16= re `(\d*)\d(\d+)`;
    assertEquality("(\\d*)\\d(\\d+)", x16.toString());
}

function testComplexRegExpValue() {
    string:RegExp x1 = re `s$`;
    assertEquality("s$", x1.toString());

    string:RegExp x2 = re `^[^p]`;
    assertEquality("^[^p]", x2.toString());

    string:RegExp x3 = re `^p[b-z]`;
    assertEquality("^p[b-z]", x3.toString());

    string:RegExp x4 = re `^ab`;
    assertEquality("^ab", x4.toString());

    string:RegExp x5 = re `^\^+`;
    assertEquality("^\\^+", x5.toString());

    string:RegExp x6 = re `^^^^^^^robot$$$$`;
    assertEquality("^^^^^^^robot$$$$", x6.toString());

    string:RegExp x7 = re `^.*?$`;
    assertEquality("^.*?$", x7.toString());

    string:RegExp x8 = re `^.*?(:|$)`;
    assertEquality("^.*?(:|$)", x8.toString());

    string:RegExp x9 = re `^.*(:|$)`;
    assertEquality("^.*(:|$)", x9.toString());

    string:RegExp x10 = re `\d{2,4}`;
    assertEquality("\\d{2,4}", x10.toString());

    string:RegExp x11 = re `cx{0,93}c`;
    assertEquality("cx{0,93}c", x11.toString());

    string:RegExp x12 = re `B{42,93}c`;
    assertEquality("B{42,93}c", x12.toString());

    string:RegExp x13 = re `[^"]*`;
    assertEquality("[^\"]*", x13.toString());

    string:RegExp x14 = re `["'][^"']*["']`;
    assertEquality("[\"'][^\"']*[\"']", x14.toString());

    string:RegExp x15 = re `cd*`;
    assertEquality("cd*", x15.toString());

    string:RegExp x16 = re `x*y+$`;
    assertEquality("x*y+$", x16.toString());

    string:RegExp x17 = re `[\d]*[\s]*bc.`;
    assertEquality("[\\d]*[\\s]*bc.", x17.toString());

    string:RegExp x18 = re `.*`;
    assertEquality(".*", x18.toString());

    string:RegExp x19 = re `[xyz]*1`;
    assertEquality("[xyz]*1", x19.toString());

    string:RegExp x20 = re `cd?e`;
    assertEquality("cd?e", x20.toString());

    string:RegExp x21 = re `cdx?e`;
    assertEquality("cdx?e", x21.toString());

    string:RegExp x22 = re `X?y?z?`;
    assertEquality("X?y?z?", x22.toString());

    string:RegExp x23 = re `ab?c?d?x?y?z`;
    assertEquality("ab?c?d?x?y?z", x23.toString());

    string:RegExp x24 = re `\??\??\??\??\??`;
    assertEquality("\\??\\??\\??\\??\\??", x24.toString());

    string:RegExp x25 = re `.?.?.?.?.?.?.?`;
    assertEquality(".?.?.?.?.?.?.?", x25.toString());

    string:RegExp x26 = re `\S+`;
    assertEquality("\\S+", x26.toString());

    string:RegExp x27 = re `bc..[\d]*[\s]*`;
    assertEquality("bc..[\\d]*[\\s]*", x27.toString());

    string:RegExp x28 = re `\?`;
    assertEquality("\\?", x28.toString());

    string:RegExp x29 = re `\?`;
    assertEquality("\\?", x29.toString());

    string:RegExp x30 = re `\*`;
    assertEquality("\\*", x30.toString());

    string:RegExp x31 = re `\\u123`;
    assertEquality("\\\\u123", x31.toString());

    string:RegExp x32 = re `\\3`;
    assertEquality("\\\\3", x32.toString());
}

function testComplexRegExpValue2() {
    int val = 10;
    string:RegExp x1 = re `^ab${val}cd*e$`;
    assertEquality("^ab10cd*e$", x1.toString());

    x1 = re `^ab${(val + 1) * val}cd*e$`;
    assertEquality("^ab110cd*e$", x1.toString());

    string:RegExp x2 = re `$[^a-b\tx-z]+(?i:ab^c[d-f]){12,}`;
    assertEquality("$[^a-b\\tx-z]+(?i:ab^c[d-f]){12,}", x2.toString());

    string:RegExp x3 = re `(x|y)|z|[cde-j]*|(?ms:[^])`;
    assertEquality("(x|y)|z|[cde-j]*|(?ms:[^])", x3.toString());

    string:RegExp x4 = re `[^ab-cdk-l]*${val}+|(?:pqr{1,}[a=cdegf\s-\Seg-ijk-])`;
    assertEquality("[^ab-cdk-l]*10+|(?:pqr{1,}[a=cdegf\\s-\\Seg-ijk-])", x4.toString());

    string:RegExp x5 = re `a\td-*ab[^c-f]+(?m:xj(?i:x|y))`;
    assertEquality("a\\td-*ab[^c-f]+(?m:xj(?i:x|y))", x5.toString());

    string a = "A";
    int b = 20;
    string:RegExp x6 = re `abc${a}(?i:[^a-bcd-e]{1,}a{1,2}b{1}(?i:cd*${b*20}+)${b}+)${a+"C"}*`;
    assertEquality("abcA(?i:[^a-bcd-e]{1,}a{1,2}b{1}(?i:cd*400+)20+)AC*", x6.toString());
}

const constValue = "{";

type Rec record {|
    string a = "*";
    string b;
|};

function testInvalidInsertionsInRegExp() {
    string a = "*";
    string:RegExp|error x1 = trap re `${a}`;
    assertEquality(true, x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina}RegularExpressionParsingError", x1.message());
        assertEquality("Invalid insertion in regular expression: Invalid character '*'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = trap re `abc${a}`;
    assertEquality(true, x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina}RegularExpressionParsingError", x1.message());
        assertEquality("Invalid insertion in regular expression: Invalid character '*'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = trap re `abc(${a})`;
    assertEquality(true, x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina}RegularExpressionParsingError", x1.message());
        assertEquality("Invalid insertion in regular expression: Invalid character '*'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = trap re `abc(?i:${a})`;
    assertEquality(true, x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina}RegularExpressionParsingError", x1.message());
        assertEquality("Invalid insertion in regular expression: Invalid character '*'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = trap re `abc(?i-m:${a})`;
    assertEquality(true, x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina}RegularExpressionParsingError", x1.message());
        assertEquality("Invalid insertion in regular expression: Invalid character '*'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = trap re `${constValue}`;
    assertEquality(true, x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina}RegularExpressionParsingError", x1.message());
        assertEquality("Invalid insertion in regular expression: Invalid character '{'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = trap re `abc${constValue}`;
    assertEquality(true, x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina}RegularExpressionParsingError", x1.message());
        assertEquality("Invalid insertion in regular expression: Invalid character '{'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = trap re `abc(?i:${constValue})`;
    assertEquality(true, x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina}RegularExpressionParsingError", x1.message());
        assertEquality("Invalid insertion in regular expression: Invalid character '{'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = trap re `abc(?i-m:${constValue})`;
    assertEquality(true, x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina}RegularExpressionParsingError", x1.message());
        assertEquality("Invalid insertion in regular expression: Invalid character '{'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = trap getRegExpValue();
    assertEquality(true, x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina}RegularExpressionParsingError", x1.message());
        assertEquality("Invalid insertion in regular expression: Invalid character '+'", <string> checkpanic x1.detail()["message"]);
    }

    Rec rec = {b: "+"};
    x1 = trap re `abc(?i:${rec.b})`;
    assertEquality(true, x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina}RegularExpressionParsingError", x1.message());
        assertEquality("Invalid insertion in regular expression: Invalid character '+'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = trap re `abc(?i:${rec.a})`;
    assertEquality(true, x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina}RegularExpressionParsingError", x1.message());
        assertEquality("Invalid insertion in regular expression: Invalid character '*'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = trap re `abc${getChar()}`;
    assertEquality(true, x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina}RegularExpressionParsingError", x1.message());
        assertEquality("Invalid insertion in regular expression: Invalid character '*'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = trap re `abc(?i:${getChar()})`;
    assertEquality(true, x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina}RegularExpressionParsingError", x1.message());
        assertEquality("Invalid insertion in regular expression: Invalid character '*'", <string> checkpanic x1.detail()["message"]);
    }
}

function getRegExpValue(string val = "+") returns string:RegExp {
    return re `abc(?i-m:${val})`;
}

function getChar() returns string {
    return "*";
}

type UserType string:RegExp;

function testEqualityWithRegExp() {
    string:RegExp x1 = re `a\td-*ab[^c-f]+(?m:xj(?i:x|y))`;
    string:RegExp x2 = re `a\td-*ab[^c-f]+(?m:xj(?i:x|y))`;

    assertEquality(true, x1 == x2);
    assertEquality(true, re `a\td-*ab[^c-f]+(?m:xj(?i:x|y))` == x2);

    UserType x3 = re `a\td-*ab[^c-f]+(?m:xj(?i:x|y))`;

    assertEquality(true, re `a\td-*ab[^c-f]+(?m:xj(?i:x|y))` == x3);
    assertEquality(true, x1 == x3);

    anydata x4 = re `a\td-*ab[^c-f]+(?m:xj(?i:x|y))`;

    assertEquality(true, x1 == x4);
    assertEquality(true, re `a\td-*ab[^c-f]+(?m:xj(?i:x|y))` == x4);
}

function testExactEqualityWithRegExp() {
    string:RegExp x1 = re `a\td-*ab[^c-f]+(?m:xj(?i:x|y))`;
    string:RegExp x2 = re `a\td-*ab[^c-f]+(?m:xj(?i:x|y))`;

    assertEquality(true, x1 === x2);
    assertEquality(true, re `a\td-*ab[^c-f]+(?m:xj(?i:x|y))` === x2);

    UserType x3 = re `a\td-*ab[^c-f]+(?m:xj(?i:x|y))`;

    assertEquality(true, re `a\td-*ab[^c-f]+(?m:xj(?i:x|y))` === x3);
    assertEquality(true, x1 === x3);

    anydata x4 = re `a\td-*ab[^c-f]+(?m:xj(?i:x|y))`;

    assertEquality(true, x1 === x4);
    assertEquality(true, re `a\td-*ab[^c-f]+(?m:xj(?i:x|y))` === x4);
}

public type Constraint record {|
    string:RegExp pattern?;
|};

public annotation Constraint String on type;

@String {
    pattern: re `[^0-9]*`
}
type Number string;

function testFreezeDirectWithRegExp() {
    typedesc<any> numTd = Number;
    assertEquality(true, numTd.@String is Constraint);
    Constraint constraint = <Constraint>numTd.@String;
    assertEquality(false, constraint.pattern is ());
    assertEquality(true, constraint.pattern == re `[^0-9]*`);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
