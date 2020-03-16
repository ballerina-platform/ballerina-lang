import ballerina/io;
import ballerina/lang.'int as ints;

public function main() {
    int a = 1;
    // `<<` performs a left shift. The bits shifted in on the right are `0`.
    // The type of the result of a left shift is always `int`.
    int res1 = a << 2;
    io:println("`int` 1 << 2: ", res1);

    ints:Unsigned8 b = 128;
    int res2 = b << 3;
    io:println("`ints:Unsigned8` 128 << 3: ", res2);

    // `>>` performs a signed right shift. The bits shifted in on the left
    // are the same as the most significant bit.
    // If the value to be shifted is of a signed subtype of `int`, the type of
    // the result of the signed right shift is `int`.
    ints:Signed16 c = -32700;
    int res3 = c >> 2;
    io:println("`int:Signed16` -32700 >> 2: ", res3);

    // If the value to be shifted is of an unsigned subtype of `int`, the type of
    // the result of the signed right shift is the same unsigned subtype of `int`.
    ints:Unsigned8 d = 255;
    int e = 4;
    ints:Unsigned8 res4 = d >> e;
    io:println("`ints:Unsigned8` 255 >> 4: ", res4);

    // `>>>` performs an unsigned right shift. The bits shifted in on the left are `0`.
    // If the value to be shifted is of a signed subtype of `int`, the type of
    // the result of the signed right shift is `int`.
    ints:Signed32 f = 123167;
    int res5 = f >>> 3;
    io:println("`int:Signed32` 123167 >>> 3: ", res5);

    // If the value to be shifted is of an unsigned subtype of `int`, the type of
    // the result of the signed right shift is the same unsigned subtype of `int`.
    ints:Unsigned16 g = 32001;
    ints:Unsigned16 res6 = g >> 2;
    io:println("`ints:Unsigned16` 32001 >>> 2: ", res6);
}
