import ballerina/io;
import ballerina/lang.'int;

public function main() {
    int a = 385;
    'int:Unsigned8 b = 128;
    // For a bitwise AND (`&`) expression, if the type of either operand is a subtype of
    // `int:UnsignedK` when K is 8, 16 or 32, then the type of the result is `int:UnsignedN`
    // where N is the smallest such K.
    'int:Unsigned8 res1 = a & b;
    io:println("`int` 385 & `int:Unsigned8` 128: ", res1);

    'int:Signed16 c = -32700;
    int d = 249;
    // Else, if neither operand is of a type that is a subtype of an unsigned `int` type,
    // the type of the result for a bitwise AND expression is `int`.
    int res2 = c & d;
    io:println("`int:Signed16` -32700 & `int` 249: ", res2);

    'int:Unsigned8 e = 254;
    'int:Unsigned16 f = 511;
    // For bitwise OR (`|`) and XOR (`^`) expressions, if the types of both operands are
    // subtypes of `int:UnsignedK` when K is 8, 16 or 32, then the type of the result is
    // `int:UnsignedN` where N is the smallest such K.
    'int:Unsigned8 res3 = e | f;
    io:println("`int:Unsigned8` 254 | `int:Unsigned16` 511: ", res3);
    'int:Unsigned8 res4 = e ^ f;
    io:println("`int:Unsigned8` 254 ^ `int:Unsigned16` 511: ", res4);

    int g = 12345678;
    'int:Signed8 h = -127;
    // Else, if at least one operand's type is not a subtype of an unsigned `int` type,
    // the type of the result for a bitwise OR or XOR expression is `int`.
    int res5 = g | h;
    io:println("`int` 12345678 | `int:Signed8` -127: ", res5);
    int res6 = g ^ h;
    io:println("`int` 12345678 ^ `int:Signed8` -127: ", res6);
}
