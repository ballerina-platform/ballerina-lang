function intMod(int a, int b) returns (int) {
    return a % b;
}

function floatMod(float a, float b) returns (float) {
    return a % b;
}

public const A = 10;
public const B = 20;
public const C = 30;
public const D = 40;

type SomeTypes A|B|C|D;

type E 12|13|14;

const float F = 20.25;
const float G = 10.0;

type H F|G;

type I 10.0|30.0;

const decimal J = 4.565;
const decimal K = 10.0;

type L J|K;

function testModWithTypes() {
    SomeTypes a1 = 10;
    int a2 = 20;
    SomeTypes a3 = 30;
    byte a4 = 25;
    int|int:Signed16 a5 = 15;
    E a6 = 12;
    float a7 = 10.0;
    H a8 = 10.0;
    I a9 = 30.0;
    L a10 = 10.0;
    decimal a11 = 20.0;

    assertEqual(a1 % a2, 10);
    assertEqual(a2 % a3, 20);
    assertEqual(a3 % a4, 5);
    assertEqual(a1 % a5, 10);
    assertEqual(a1 % a6, 10);
    assertEqual(a4 % a6, 1);
    assertEqual(a5 % a6, 3);
    assertEqual(a7 % a8, 0.0);
    assertEqual(a7 % a9, 10.0);
    assertEqual(a8 % a9, 10.0);
    assertEqual(a10 % a11, 10d);
}

function testModSingleton() {
    20 a1 = 20;
    int a2 = 2;
    25.0 a3 = 25.0;
    float a4 = 10;
    SomeTypes a5 = 30;
    int|int:Signed16 a6 = 5;
    E a7 = 12;

    assertEqual(a1 % a2, 0);
    assertEqual(a3 % a4, 5.0);
    assertEqual(a1 % a5, 20);
    assertEqual(a1 % a6, 0);
    assertEqual(a1 % a7, 8);
}

type Ints 1|2;
type T1 1|2|()|3;
type T2 1|2|3?;

function testModNullable() {
    int? a1 = 10;
    int? a2 = 3;
    int? a3 = 5;
    int? a4 = ();
    int a5 = 2;
    float? a6 = 10.0;
    float? a7 = 5.0;
    float? a8 = ();
    float a9 = 4.0;

    int? a10 = a1 % a5;
    int? a11 = a3 % a2;
    int? a12 = a3 % a4;
    float? a13 = a6 % a7;
    float? a14 = a7 % a9;
    float? a15 = a8 % a9;

    Ints a16 = 2;
    int? a17 = 1;
    int? a18 = a16 % a17;

    int a19 = 25;
    Ints? a20 = 2;

    T1 a21 = 2;
    T2 a22 = 1;
    ()|int a23 = ();

    int:Unsigned8 a = 1;
    int:Unsigned16 b = 2;
    int:Unsigned32 c = 5;
    int:Signed8 d = 20;
    int:Signed16 e = 10;
    int:Signed32 f = 10;
    byte g = 30;

    assertEqual(a10, 0);
    assertEqual(a11, 2);
    assertEqual(a12, ());
    assertEqual(a13, 0.0);
    assertEqual(a14, 1.0);
    assertEqual(a15, ());
    assertEqual(a18, 0);
    assertEqual(a19 % a20, 1);

    assertEqual(a21 % a21, 0);
    assertEqual(a21 % a22, 0);
    assertEqual(a21 % a23, ());
    assertEqual(a22 % a22, 0);
    assertEqual(a22 % a23, ());
    assertEqual(a23 % a23, ());

    assertEqual(a % a, 0);
    assertEqual(a % b, 1);
    assertEqual(a % c, 1);
    assertEqual(a % d, 1);
    assertEqual(a % e, 1);
    assertEqual(a % f, 1);
    assertEqual(a % g, 1);

    assertEqual(b % c, 2);
    assertEqual(b % d, 2);
    assertEqual(b % e, 2);
    assertEqual(b % f, 2);
    assertEqual(b % g, 2);
    assertEqual(b % b, 0);

    assertEqual(c % c, 0);
    assertEqual(c % d, 5);
    assertEqual(c % e, 5);
    assertEqual(c % f, 5);
    assertEqual(c % g, 5);

    assertEqual(d % d, 0);
    assertEqual(d % e, 0);
    assertEqual(d % f, 0);
    assertEqual(d % g, 20);

    assertEqual(e % e, 0);
    assertEqual(e % f, 0);
    assertEqual(e % g, 10);

    assertEqual(f % f, 0);
    assertEqual(f % g, 10);

    assertEqual(g % g, 0);
}

const int constInt = 5;

const float constFloat = 20.5;

type MyInt int;

type MyFloat float;

type TWO 2;

type FOUR_POINT_FIVE 4.5;

function testModFloatInt() {
    int a = 2;
    int a1 = 0;
    float b = float:Infinity;
    float c = 4.5e-1;
    float d = 0;
    int e = int:MAX_VALUE;
    int f = int:MIN_VALUE;
    MyInt g = 2;
    MyFloat h = 4.5;
    2 i = 2;
    4.5 j = 4.5;
    constInt k = 5;
    constFloat m = 20.5;
    TWO n = 2;
    FOUR_POINT_FIVE p = 4.5;

    float var4 = b % a;
    assertEqual(var4, float:NaN);
    float var5 = c % a;
    assertEqual(var5, 0.45);
    float var6 = d % a;
    assertEqual(var6, 0.0);
    float var61 = h % a;
    assertEqual(var61, 0.5);
    float var62 = j % a;
    assertEqual(var62, 0.5);
    float var63 = m % a;
    assertEqual(var63, 0.5);
    float var64 = p % a;
    assertEqual(var64, 0.5);

    float var7 = b % a1;
    assertEqual(var7, float:NaN);
    float var8 = c % a1;
    assertEqual(var8, float:NaN);
    float var9 = d % a1;
    assertEqual(var9, float:NaN);
    float var91 = h % a1;
    assertEqual(var91, float:NaN);
    float var92 = j % a1;
    assertEqual(var92, float:NaN);
    float var93 = m % a1;
    assertEqual(var93, float:NaN);
    float var94 = p % a1;
    assertEqual(var94, float:NaN);

    float var10 = b % constInt;
    assertEqual(var10, float:NaN);
    float var11 = c % constInt;
    assertEqual(var11, 0.45);
    float var12 = d % constInt;
    assertEqual(var12, 0.0);
    float var121 = h % constInt;
    assertEqual(var121, 4.5);
    float var122 = j % constInt;
    assertEqual(var122, 4.5);
    float var123 = m % constInt;
    assertEqual(var123, 0.5);
    float var124 = p % constInt;
    assertEqual(var124, 4.5);

    float var13 = constFloat % constInt;
    assertEqual(var13, 0.5);

    float var15 = constFloat % a;
    assertEqual(var15, 0.5);
    float var16 = constFloat % d;
    assertEqual(var16, float:NaN);
    float var161 = constFloat % i;
    assertEqual(var161, 0.5);

    float var17 = c % e;
    assertEqual(var17, 0.45);
    float var18 = c % f;
    assertEqual(var18, 0.45);
    float var19 = c % g;
    assertEqual(var19, 0.45);
    float var191 = c % i;
    assertEqual(var191, 0.45);
    float var192 = c % k;
    assertEqual(var192, 0.45);
    float var193 = c % n;
    assertEqual(var193, 0.45);

    float var20 = h % g;
    assertEqual(var20, 0.5);
    float var201 = h % i;
    assertEqual(var201, 0.5);
    float var202 = h % k;
    assertEqual(var202, 4.5);
    float var203 = h % n;
    assertEqual(var203, 0.5);

    float var21 = j % i;
    assertEqual(var21, 0.5);

    float var22 = m % k;
    assertEqual(var22, 0.5);
    float var23 = p % k;
    assertEqual(var23, 4.5);

    float var24 = m % n;
    assertEqual(var24, 0.5);
    float var25 = p % n;
    assertEqual(var25, 0.5);
}

function testModFloatIntSubTypes() {
    int:Signed8 a = -2;
    int:Signed16 b = 2;
    int:Signed32 c = -4;
    int:Unsigned8 d = 4;
    int:Unsigned16 e = 5;
    int:Unsigned32 f = 10;
    byte g = 25;

    float h = 2.5;

    float var8 = h % a;
    assertEqual(var8, 0.5);
    float var9 = h % b;
    assertEqual(var9, 0.5);
    float var10 = h % c;
    assertEqual(var10, 2.5);
    float var11 = h % d;
    assertEqual(var11, 2.5);
    float var12 = h % e;
    assertEqual(var12, 2.5);
    float var13 = h % f;
    assertEqual(var13, 2.5);
    float var14 = h % g;
    assertEqual(var14, 2.5);
}

function testModFloatIntWithNullableOperands() {
    int a = 2;
    int? b = 4;
    float c = 4.5e-1;
    float? d = -10.5;
    int? e = ();
    float? f = ();
    2? g = 2;
    5.5 h = 5.5;

    float? var2 = d % a;
    assertEqual(var2, -0.5);

    float? var4 = c % b;
    assertEqual(var4, 0.45);

    float? var6 = d % b;
    assertEqual(var6, -2.5);

    float? var8 = d % constInt;
    assertEqual(var8, -0.5);

    float? var9 = constFloat % b;
    assertEqual(var9, 0.5);

    float? var10 = c % e;
    assertEqual(var10, ());

    float? var11 = f % e;
    assertEqual(var11, ());

    float? var12 = f % a;
    assertEqual(var12, ());

    float? var13 = f % constInt;
    assertEqual(var13, ());

    float? var14 = constFloat % e;
    assertEqual(var14, ());

    float? var15 = c % g;
    assertEqual(var15, 0.45);

    float? var16 = h % a;
    assertEqual(var16, 1.5);

    float? var17 = h % g;
    assertEqual(var17, 1.5);
}

function testModFloatIntSubTypeWithNullableOperands() {
    int:Signed8 a = -2;
    int:Signed16 b = 2;
    int:Signed32 c = -5;
    int:Unsigned8 d = 10;
    int:Unsigned16 e = 5;
    int:Unsigned32 f = 10;
    byte g = 4;

    int:Signed8? h = -2;
    int:Signed16? i = ();
    int:Signed32? j = ();
    int:Unsigned8? k = 4;
    int:Unsigned16? m = 5;
    int:Unsigned32? n = 10;
    byte? p = ();

    float q = 2.5;
    float? r = 2.5;

    float? var8 = r % a;
    assertEqual(var8, 0.5);
    float? var9 = r % b;
    assertEqual(var9, 0.5);
    float? var10 = r % c;
    assertEqual(var10, 2.5);
    float? var11 = r % d;
    assertEqual(var11, 2.5);
    float? var12 = r % e;
    assertEqual(var12, 2.5);
    float? var13 = r % f;
    assertEqual(var13, 2.5);
    float? var14 = r % g;
    assertEqual(var14, 2.5);

    float? var22 = q % h;
    assertEqual(var22, 0.5);
    float? var23 = q % i;
    assertEqual(var23, ());
    float? var24 = q % j;
    assertEqual(var24, ());
    float? var25 = q % k;
    assertEqual(var25, 2.5);
    float? var26 = q % m;
    assertEqual(var26, 2.5);
    float? var27 = q % n;
    assertEqual(var27, 2.5);
    float? var28 = q % p;
    assertEqual(var28, ());

    float? var36 = r % h;
    assertEqual(var36, 0.5);
    float? var37 = r % i;
    assertEqual(var37, ());
    float? var38 = r % j;
    assertEqual(var38, ());
    float? var39 = r % k;
    assertEqual(var39, 2.5);
    float? var40 = r % m;
    assertEqual(var40, 2.5);
    float? var41 = r % n;
    assertEqual(var41, 2.5);
    float? var42 = r % p;
    assertEqual(var42, ());
}

function testResultTypeOfModFloatIntByInfering() {
    float a = 2.5;
    int b = 5;

    var c = a % b;
    float var1 = c;
    assertEqual(var1, 2.5);

    var e = a % constInt;
    float var3 = e;
    assertEqual(var3, 2.5);

    var g = constFloat % b;
    float var5 = g;
    assertEqual(var5, 0.5);

    var i = constFloat % constInt;
    float var7 = i;
    assertEqual(var7, 0.5);
}

function testResultTypeOfModFloatIntForNilableOperandsByInfering() {
    float? a = 2.5;
    int? b = 5;

    var c = a % b;
    float? var1 = c;
    assertEqual(var1, 2.5);

    var e = a % constInt;
    float? var3 = e;
    assertEqual(var3, 2.5);

    var g = constFloat % b;
    float? var5 = g;
    assertEqual(var5, 0.5);

    var i = constFloat % constInt;
    float? var7 = i;
    assertEqual(var7, 0.5);
}

const decimal constDecimal = 20.5;

type MyDecimal decimal;

type FOUR_POINT_FIVE_DECIMAL 4.5d;

function testModDecimalInt() {
    int a = 2;
    decimal c = 4.5e-1;
    decimal d = 0;
    int e = int:MAX_VALUE;
    int f = int:MIN_VALUE;
    MyInt g = 2;
    MyDecimal h = 4.5;
    2 i = 2;
    4.5d j = 4.5d;
    constInt k = 5;
    constDecimal m = 20.5;
    TWO n = 2;
    FOUR_POINT_FIVE_DECIMAL p = 4.5;

    decimal var5 = c % a;
    assertEqual(var5, 0.45d);
    decimal var6 = d % a;
    assertEqual(var6, 0d);
    decimal var61 = h % a;
    assertEqual(var61, 0.5d);
    decimal var62 = j % a;
    assertEqual(var62, 0.5d);
    decimal var63 = m % a;
    assertEqual(var63, 0.5d);
    decimal var64 = p % a;
    assertEqual(var64, 0.5d);

    decimal var11 = c % constInt;
    assertEqual(var11, 0.45d);
    decimal var12 = d % constInt;
    assertEqual(var12, 0d);
    decimal var121 = h % constInt;
    assertEqual(var121, 4.5d);
    decimal var122 = j % constInt;
    assertEqual(var122, 4.5d);
    decimal var123 = m % constInt;
    assertEqual(var123, 0.5d);
    decimal var124 = p % constInt;
    assertEqual(var124, 4.5d);

    decimal var13 = constDecimal % constInt;
    assertEqual(var13, 0.5d);

    decimal var15 = constDecimal % a;
    assertEqual(var15, 0.5d);
    decimal var16 = constDecimal % i;
    assertEqual(var16, 0.5d);

    decimal var17 = c % e;
    assertEqual(var17, 0.45d);
    decimal var18 = c % f;
    assertEqual(var18, 0.45d);
    decimal var19 = c % g;
    assertEqual(var19, 0.45d);
    decimal var191 = c % i;
    assertEqual(var191, 0.45d);
    decimal var192 = c % k;
    assertEqual(var192, 0.45d);
    decimal var193 = c % n;
    assertEqual(var193, 0.45d);

    decimal var20 = h % g;
    assertEqual(var20, 0.5d);
    decimal var201 = h % i;
    assertEqual(var201, 0.5d);
    decimal var202 = h % k;
    assertEqual(var202, 4.5d);
    decimal var203 = h % n;
    assertEqual(var203, 0.5d);

    decimal var21 = j % i;
    assertEqual(var21, 0.5d);

    decimal var22 = m % k;
    assertEqual(var22, 0.5d);
    decimal var23 = p % k;
    assertEqual(var23, 4.5d);

    decimal var24 = m % n;
    assertEqual(var24, 0.5d);
    decimal var25 = p % n;
    assertEqual(var25, 0.5d);
}

function testModDecimalIntSubTypes() {
    int:Signed8 a = -2;
    int:Signed16 b = 2;
    int:Signed32 c = -4;
    int:Unsigned8 d = 4;
    int:Unsigned16 e = 5;
    int:Unsigned32 f = 10;
    byte g = 25;

    decimal h = 2.5;

    decimal var8 = h % a;
    assertEqual(var8, 0.5d);
    decimal var9 = h % b;
    assertEqual(var9, 0.5d);
    decimal var10 = h % c;
    assertEqual(var10, 2.5d);
    decimal var11 = h % d;
    assertEqual(var11, 2.5d);
    decimal var12 = h % e;
    assertEqual(var12, 2.5d);
    decimal var13 = h % f;
    assertEqual(var13, 2.5d);
    decimal var14 = h % g;
    assertEqual(var14, 2.5d);
}

function testModDecimalIntWithNullableOperands() {
    int a = 2;
    int? b = 4;
    decimal c = 4.5e-1;
    decimal? d = -10.5;
    int? e = ();
    decimal? f = ();
    2? g = 2;
    5.5d h = 5.5d;

    decimal? var2 = d % a;
    assertEqual(var2, -0.5d);

    decimal? var4 = c % b;
    assertEqual(var4, 0.45d);

    decimal? var6 = d % b;
    assertEqual(var6, -2.5d);

    decimal? var8 = d % constInt;
    assertEqual(var8, -0.5d);

    decimal? var9 = constDecimal % b;
    assertEqual(var9, 0.5d);

    decimal? var10 = c % e;
    assertEqual(var10, ());

    decimal? var11 = f % e;
    assertEqual(var11, ());

    decimal? var12 = f % a;
    assertEqual(var12, ());

    decimal? var13 = f % constInt;
    assertEqual(var13, ());

    decimal? var14 = constDecimal % e;
    assertEqual(var14, ());

    decimal? var15 = c % g;
    assertEqual(var15, 0.45d);

    decimal? var16 = h % a;
    assertEqual(var16, 1.5d);

    decimal? var17 = h % g;
    assertEqual(var17, 1.5d);
}

function testModDecimalIntSubTypeWithNullableOperands() {
    int:Signed8 a = -2;
    int:Signed16 b = 2;
    int:Signed32 c = -5;
    int:Unsigned8 d = 10;
    int:Unsigned16 e = 5;
    int:Unsigned32 f = 10;
    byte g = 4;

    int:Signed8? h = -2;
    int:Signed16? i = ();
    int:Signed32? j = ();
    int:Unsigned8? k = 4;
    int:Unsigned16? m = 5;
    int:Unsigned32? n = 10;
    byte? p = ();

    decimal q = 2.5;
    decimal? r = 2.5;

    decimal? var8 = r % a;
    assertEqual(var8, 0.5d);
    decimal? var9 = r % b;
    assertEqual(var9, 0.5d);
    decimal? var10 = r % c;
    assertEqual(var10, 2.5d);
    decimal? var11 = r % d;
    assertEqual(var11, 2.5d);
    decimal? var12 = r % e;
    assertEqual(var12, 2.5d);
    decimal? var13 = r % f;
    assertEqual(var13, 2.5d);
    decimal? var14 = r % g;
    assertEqual(var14, 2.5d);

    decimal? var22 = q % h;
    assertEqual(var22, 0.5d);
    decimal? var23 = q % i;
    assertEqual(var23, ());
    decimal? var24 = q % j;
    assertEqual(var24, ());
    decimal? var25 = q % k;
    assertEqual(var25, 2.5d);
    decimal? var26 = q % m;
    assertEqual(var26, 2.5d);
    decimal? var27 = q % n;
    assertEqual(var27, 2.5d);
    decimal? var28 = q % p;
    assertEqual(var28, ());

    decimal? var36 = r % h;
    assertEqual(var36, 0.5d);
    decimal? var37 = r % i;
    assertEqual(var37, ());
    decimal? var38 = r % j;
    assertEqual(var38, ());
    decimal? var39 = r % k;
    assertEqual(var39, 2.5d);
    decimal? var40 = r % m;
    assertEqual(var40, 2.5d);
    decimal? var41 = r % n;
    assertEqual(var41, 2.5d);
    decimal? var42 = r % p;
    assertEqual(var42, ());
}

function testResultTypeOfModDecimalIntByInfering() {
    decimal a = 2.5;
    int b = 5;

    var c = a % b;
    decimal var1 = c;
    assertEqual(var1, 2.5d);

    var e = a % constInt;
    decimal var3 = e;
    assertEqual(var3, 2.5d);

    var g = constDecimal % b;
    decimal var5 = g;
    assertEqual(var5, 0.5d);

    var i = constDecimal % constInt;
    decimal var7 = i;
    assertEqual(var7, 0.5d);
}

function testResultTypeOfModDecimalIntForNilableOperandsByInfering() {
    decimal? a = 2.5;
    int? b = 5;

    var c = a % b;
    decimal? var1 = c;
    assertEqual(var1, 2.5d);

    var e = a % constInt;
    decimal? var3 = e;
    assertEqual(var3, 2.5d);

    var g = constDecimal % b;
    decimal? var5 = g;
    assertEqual(var5, 0.5d);

    var i = constDecimal % constInt;
    decimal? var7 = i;
    assertEqual(var7, 0.5d);
}

int intVal = 10;

function testNoShortCircuitingInModWithNullable() {
    int? result = foo() % bar();
    assertEqual(result, ());
    assertEqual(intVal, 18);

    result = foo() % 12;
    assertEqual(result, ());
    assertEqual(intVal, 20);

    result = 12 % bar();
    assertEqual(result, ());
    assertEqual(intVal, 26);

    int? x = 12;
    result = foo() % x;
    assertEqual(result, ());
    assertEqual(intVal, 28);

    result = x % bar();
    assertEqual(result, ());
    assertEqual(intVal, 34);

    result = x % bam();
    assertEqual(result, 2);
    assertEqual(intVal, 44);

    result = bam() % x;
    assertEqual(result, 5);
    assertEqual(intVal, 54);

    result = foo() % bam();
    assertEqual(result, ());
    assertEqual(intVal, 66);

    result = bam() % bar();
    assertEqual(result, ());
    assertEqual(intVal, 82);
}

function testNoShortCircuitingInModWithNonNullable() {
    intVal = 10;
    int x = 10;

    int result = x % bam();
    assertEqual(result, 0);
    assertEqual(intVal, 20);

    result = bam() % 12;
    assertEqual(result, 5);
    assertEqual(intVal, 30);
}

function foo() returns int? {
    intVal += 2;
    return ();
}

function bar() returns int? {
    intVal += 6;
    return ();
}

function bam() returns int {
    intVal += 10;
    return 5;
}

function assertEqual(any actual, any expected) {
    if actual is anydata && expected is anydata && actual == expected {
        return;
    }

    if actual === expected {
        return;
    }

    string actualValAsString = actual.toString();
    string expectedValAsString = expected.toString();
    panic error(string `Assertion error: expected ${expectedValAsString} found ${actualValAsString}`);
}
