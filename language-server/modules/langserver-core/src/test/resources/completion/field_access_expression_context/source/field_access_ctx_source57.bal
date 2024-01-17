import ballerina/lang.regexp;

type RegExpType regexp:RegExp;

public function testUserDefinedRegExpFieldAccess() {
    RegExpType reg = re ``;
    reg.
}
