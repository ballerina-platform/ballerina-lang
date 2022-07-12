function intDivide(int a, int b) returns (int) {
    return a / b;
}

function floatDivide(float a, float b) returns (float) {
    return a / b;
}

public function overflowByDivision() {
 int val = -1;
 int val1 = getPowerof(-2, 63);
 int k = val1/val;
}

function getPowerof(int num, int power) returns int {
    int i = 0;
    int res = 1;
    while (i < power) {
        res = res * num;
        i = i + 1;
    }
    return res;
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

type I 10.5|10.0;

const decimal J = 4.0;
const decimal K = 5.0;

type L J|K;

type IntType1 -2|-1|0|1|2;
type IntType2 int:Unsigned8|int:Signed32;
type IntType3 IntType1|IntType2;
type IntType4 IntType1|byte;

const float AA = 1.25;
const float BB = 2.5;

type FloatType1 -2.0f|-1.0f|0.0f|1.0f|2.0f;
type FloatType2 FloatType1;
type FloatType3 AA|BB;

const decimal CC = 1.2;
const decimal DD = 3.0;

type DecimalType1 CC|DD;
type DecimalType2 1d|2d|-1d|2d;
type DecimalType3 DecimalType1|DecimalType2;

function testDivisionWithTypes() {
    SomeTypes a1 = 10;
    int a2 = 20;
    SomeTypes a3 = 30;
    byte a4 = 25;
    int|int:Signed16 a5 = 15;
    E a6 = 12;
    float a7 = 10.5;
    H a8 = 10.0;
    I a9 = 10.0;
    L a10 = 5.0;
    decimal a11 = 10.0;

    assertEqual(a1 / a2, 0);
    assertEqual(a2 / a3, 0);
    assertEqual(a3 / a4, 1);
    assertEqual(a1 / a5, 0);
    assertEqual(a1 / a6, 0);
    assertEqual(a4 / a6, 2);
    assertEqual(a5 / a6, 1);
    assertEqual(a7 / a8, 1.05);
    assertEqual(a7 / a9, 1.05);
    assertEqual(a8 / a9, 1.0);
    assertEqual(a11 / a10, 2d);

    IntType3 a21 = 1;
    int|IntType3 a22 = 2;
    IntType4|int a23 = 3;

    assertEqual(a21 / a21, 1);
    assertEqual(a21 / a22, 0);
    assertEqual(a21 / a23, 0);
    assertEqual(a22 / a23, 0);
    assertEqual(a23 / a23, 1);

    FloatType2 a24 = -2;
    FloatType2 a25 = 1;
    float|FloatType3 a26 = 1.25;
    FloatType3|float a27 = 2.5;

    assertEqual(a24 / a24, 1.0);
    assertEqual(a24 / a25, -2.0);
    assertEqual(a24 / a26, -1.6);
    assertEqual(a24 / a27, -0.8);
    assertEqual(a25 / a25, 1.0);
    assertEqual(a25 / a26, 0.8);
    assertEqual(a25 / a27, 0.4);
    assertEqual(a26 / a26, 1.0);
    assertEqual(a26 / a27, 0.5);
    assertEqual(a27 / a27, 1.0);

    DecimalType1 a28 = 1.2;
    DecimalType3|decimal a29 = 2;
    decimal|DecimalType3 a30 = 3;

    assertEqual(a28 / a28, 1d);
    assertEqual(a28 / a29, 0.6d);
    assertEqual(a28 / a30, 0.4d);
    assertEqual(a29 / a29, 1d);
    assertEqual(a29 / a30, 0.6666666666666666666666666666666667d);
    assertEqual(a30 / a30, 1d);
}

function testDivisionSingleton() {
    20 a1 = 20;
    int a2 = 2;
    20.5 a3 = 20.5;
    float a4 = 10;
    SomeTypes a5 = 30;
    int|int:Signed16 a6 = 5;
    E a7 = 12;

    assertEqual(a1 / a2, 10);
    assertEqual(a3 / a4, 2.05);
    assertEqual(a1 / a5, 0);
    assertEqual(a1 / a6, 4);
    assertEqual(a1 / a7, 1);
}

function testContextuallyExpectedTypeOfNumericLiteralInDivision() {
    float a1 = 10.0 / 2.0;
    float a2 = (10 / 5) / 2.0;
    decimal a3 = 30.0 / 15;
    decimal a4 = 9.0 / 3.0;
    float? a5 = 10 / 2;
    decimal a6 = 20 / 2.0;

    assertEqual(a1, 5.0);
    assertEqual(a2, 1.0);
    assertEqual(a3, 2.0d);
    assertEqual(a4, 3.0d);
    assertEqual(a5, 5.0);
    assertEqual(a6, 10.0d);
}

type Ints 1|2;
type T1 1|2|()|3;
type T2 1|2|3?;
type Decimals 1d|2d;

function testDivisionNullable() {
    int? a1 = 10;
    int? a2 = 2;
    int? a3 = 1;
    int? a4 = ();
    int a5 = 5;
    float? a6 = 30.0;
    float? a7 = 10.0;
    float? a8 = ();
    float a9 = 5.0;

    int? a10 = (a1 / a2) / a5;
    int? a11 = a5 / a3;
    int? a12 = a4 / a1;
    float? a13 = a6 / a7;
    float? a14 = a6 / a9;
    float? a15 = a6 / a8;

    Ints a16 = 2;
    int? a17 = 1;
    int? a18 = a16 / a17;

    int a19 = 30;
    Ints? a20 = 2;

    T1 a21 = 2;
    T2? a22 = 1;
    ()|int a23 = ();
    T2? a24 = 1;

    Decimals? a25 = 1;
    Decimals? a26 = 2;

    int:Unsigned32 a = 1000;
    int:Unsigned16 b = 500;
    int:Unsigned8 c = 200;
    int:Signed8 d = 100;
    int:Signed16 e = 50;
    int:Signed32 f = 10;
    byte g = 5;

    assertEqual(a10, 1);
    assertEqual(a11, 5);
    assertEqual(a12, ());
    assertEqual(a13, 3.0);
    assertEqual(a14, 6.0);
    assertEqual(a15, ());
    assertEqual(a16, 2);
    assertEqual(a19 / a20, 15);

    assertEqual(a21 / a21, 1);
    assertEqual(a21 / a22, 2);
    assertEqual(a21 / a23, ());
    assertEqual(a22 / a22, 1);
    assertEqual(a22 / a23, ());
    assertEqual(a23 / a23, ());
    assertEqual(a21 / a24, 2);
    assertEqual(a26 / a25, 2d);

    assertEqual(a / a, 1);
    assertEqual(a / b, 2);
    assertEqual(a / c, 5);
    assertEqual(a / d, 10);
    assertEqual(a / e, 20);
    assertEqual(a / f, 100);
    assertEqual(a / g, 200);

    assertEqual(b / c, 2);
    assertEqual(b / d, 5);
    assertEqual(b / e, 10);
    assertEqual(b / f, 50);
    assertEqual(b / g, 100);
    assertEqual(b / b, 1);

    assertEqual(c / c, 1);
    assertEqual(c / d, 2);
    assertEqual(c / e, 4);
    assertEqual(c / f, 20);
    assertEqual(c / g, 40);

    assertEqual(d / d, 1);
    assertEqual(d / e, 2);
    assertEqual(d / f, 10);
    assertEqual(d / g, 20);

    assertEqual(e / e, 1);
    assertEqual(e / f, 5);
    assertEqual(e / g, 10);

    assertEqual(f / f, 1);
    assertEqual(f / g, 2);

    assertEqual(g / g, 1);

    IntType3? a27 = 1;
    IntType3? a28 = 2;
    IntType4? a29 = 3;

    assertEqual(a27 / a27, 1);
    assertEqual(a27 / a28, 0);
    assertEqual(a27 / a29, 0);
    assertEqual(a28 / a29, 0);
    assertEqual(a29 / a29, 1);

    FloatType2? a30 = -2;
    FloatType2? a31 = 1;
    FloatType3? a32 = 1.25;
    FloatType3? a33 = 2.5;

    assertEqual(a30 / a30, 1.0);
    assertEqual(a30 / a31, -2.0);
    assertEqual(a30 / a32, -1.6);
    assertEqual(a30 / a33, -0.8);
    assertEqual(a31 / a31, 1.0);
    assertEqual(a31 / a32, 0.8);
    assertEqual(a31 / a33, 0.4);
    assertEqual(a32 / a32, 1.0);
    assertEqual(a32 / a33, 0.5);
    assertEqual(a33 / a33, 1.0);

    DecimalType1? a34 = 1.2;
    DecimalType3? a35 = 2;
    DecimalType3? a36 = 3;

    assertEqual(a34 / a34, 1d);
    assertEqual(a34 / a35, 0.6d);
    assertEqual(a34 / a36, 0.4d);
    assertEqual(a35 / a35, 1d);
    assertEqual(a35 / a36, 0.6666666666666666666666666666666667d);
    assertEqual(a36 / a36, 1d);
}

const int constInt = 5;

const float constFloat = 20.5;

type MyInt int;

type MyFloat float;

type TWO 2;

type FOUR_POINT_FIVE 4.5;

function testDivisionFloatInt() {
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

    float var4 = b / a;
    assertEqual(var4, float:Infinity);
    float var5 = c / a;
    assertEqual(var5, 0.225);
    float var6 = d / a;
    assertEqual(var6, 0.0);
    float var61 = h / a;
    assertEqual(var61, 2.25);
    float var62 = j / a;
    assertEqual(var62, 2.25);
    float var63 = m / a;
    assertEqual(var63, 10.25);
    float var64 = p / a;
    assertEqual(var64, 2.25);

    float var7 = b / a1;
    assertEqual(var7, float:Infinity);
    float var8 = c / a1;
    assertEqual(var8, float:Infinity);
    float var9 = d / a1;
    assertEqual(var9, float:NaN);
    float var91 = h / a1;
    assertEqual(var91, float:Infinity);
    float var92 = j / a1;
    assertEqual(var92, float:Infinity);
    float var93 = m / a1;
    assertEqual(var93, float:Infinity);
    float var94 = p / a1;
    assertEqual(var94, float:Infinity);

    float var10 = b / constInt;
    assertEqual(var10, float:Infinity);
    float var11 = c / constInt;
    assertEqual(var11, 0.09);
    float var12 = d / constInt;
    assertEqual(var12, 0.0);
    float var121 = h / constInt;
    assertEqual(var121, 0.9);
    float var122 = j / constInt;
    assertEqual(var122, 0.9);
    float var123 = m / constInt;
    assertEqual(var123, 4.1);
    float var124 = p / constInt;
    assertEqual(var124, 0.9);

    float var13 = constFloat / constInt;
    assertEqual(var13, 4.1);

    float var15 = constFloat / a;
    assertEqual(var15, 10.25);
    float var16 = constFloat / d;
    assertEqual(var16, float:Infinity);
    float var161 = constFloat / i;
    assertEqual(var161, 10.25);

    float var17 = c / e;
    assertEqual(var17, 4.87890977618477E-20);
    float var18 = c / f;
    assertEqual(var18, -4.87890977618477E-20);
    float var19 = c / g;
    assertEqual(var19, 0.225);
    float var191 = c / i;
    assertEqual(var191, 0.225);
    float var192 = c / k;
    assertEqual(var192, 0.09);
    float var193 = c / n;
    assertEqual(var193, 0.225);

    float var20 = h / g;
    assertEqual(var20, 2.25);
    float var201 = h / i;
    assertEqual(var201, 2.25);
    float var202 = h / k;
    assertEqual(var202, 0.9);
    float var203 = h / n;
    assertEqual(var203, 2.25);

    float var21 = j / i;
    assertEqual(var21, 2.25);

    float var22 = m / k;
    assertEqual(var22, 4.1);
    float var23 = p / k;
    assertEqual(var23, 0.9);

    float var24 = m / n;
    assertEqual(var24, 10.25);
    float var25 = p / n;
    assertEqual(var25, 2.25);
}

function testDivisionFloatIntSubTypes() {
    int:Signed8 a = -2;
    int:Signed16 b = 2;
    int:Signed32 c = -4;
    int:Unsigned8 d = 4;
    int:Unsigned16 e = 5;
    int:Unsigned32 f = 10;
    byte g = 25;

    float h = 2.5;

    float var8 = h / a;
    assertEqual(var8, -1.25);
    float var9 = h / b;
    assertEqual(var9, 1.25);
    float var10 = h / c;
    assertEqual(var10, -0.625);
    float var11 = h / d;
    assertEqual(var11, 0.625);
    float var12 = h / e;
    assertEqual(var12, 0.5);
    float var13 = h / f;
    assertEqual(var13, 0.25);
    float var14 = h / g;
    assertEqual(var14, 0.1);
}

function testDivisionFloatIntWithNullableOperands() {
    int a = 2;
    int? b = 4;
    float c = 4.5e-1;
    float? d = -10.5;
    int? e = ();
    float? f = ();
    2? g = 2;
    5.5 h = 5.5;

    float? var2 = d / a;
    assertEqual(var2, -5.25);

    float? var4 = c / b;
    assertEqual(var4, 0.1125);

    float? var6 = d / b;
    assertEqual(var6, -2.625);

    float? var8 = d / constInt;
    assertEqual(var8, -2.1);

    float? var9 = constFloat / b;
    assertEqual(var9, 5.125);

    float? var10 = c / e;
    assertEqual(var10, ());

    float? var11 = f / e;
    assertEqual(var11, ());

    float? var12 = f / a;
    assertEqual(var12, ());

    float? var13 = f / constInt;
    assertEqual(var13, ());

    float? var14 = constFloat / e;
    assertEqual(var14, ());

    float? var15 = c / g;
    assertEqual(var15, 0.225);

    float? var16 = h / a;
    assertEqual(var16, 2.75);

    float? var17 = h / g;
    assertEqual(var17, 2.75);
}

function testDivisionFloatIntSubTypeWithNullableOperands() {
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

    float? var8 = r / a;
    assertEqual(var8, -1.25);
    float? var9 = r / b;
    assertEqual(var9, 1.25);
    float? var10 = r / c;
    assertEqual(var10, -0.5);
    float? var11 = r / d;
    assertEqual(var11, 0.25);
    float? var12 = r / e;
    assertEqual(var12, 0.5);
    float? var13 = r / f;
    assertEqual(var13, 0.25);
    float? var14 = r / g;
    assertEqual(var14, 0.625);

    float? var22 = q / h;
    assertEqual(var22, -1.25);
    float? var23 = q / i;
    assertEqual(var23, ());
    float? var24 = q / j;
    assertEqual(var24, ());
    float? var25 = q / k;
    assertEqual(var25, 0.625);
    float? var26 = q / m;
    assertEqual(var26, 0.5);
    float? var27 = q / n;
    assertEqual(var27, 0.25);
    float? var28 = q / p;
    assertEqual(var28, ());

    float? var36 = r / h;
    assertEqual(var36, -1.25);
    float? var37 = r / i;
    assertEqual(var37, ());
    float? var38 = r / j;
    assertEqual(var38, ());
    float? var39 = r / k;
    assertEqual(var39, 0.625);
    float? var40 = r / m;
    assertEqual(var40, 0.5);
    float? var41 = r / n;
    assertEqual(var41, 0.25);
    float? var42 = r / p;
    assertEqual(var42, ());
}

function testResultTypeOfDivisionFloatIntByInfering() {
    float a = 2.5;
    int b = 5;

    var c = a / b;
    float var1 = c;
    assertEqual(var1, 0.5);

    var e = a / constInt;
    float var3 = e;
    assertEqual(var3, 0.5);

    var g = constFloat / b;
    float var5 = g;
    assertEqual(var5, 4.1);

    var i = constFloat / constInt;
    float var7 = i;
    assertEqual(var7, 4.1);
}

function testResultTypeOfDivisionFloatIntForNilableOperandsByInfering() {
    float? a = 2.5;
    int? b = 5;

    var c = a / b;
    float? var1 = c;
    assertEqual(var1, 0.5);

    var e = a / constInt;
    float? var3 = e;
    assertEqual(var3, 0.5);

    var g = constFloat / b;
    float? var5 = g;
    assertEqual(var5, 4.1);

    var i = constFloat / constInt;
    float? var7 = i;
    assertEqual(var7, 4.1);
}

function testDivisionFloatIntToInfinityAndNaN() {
    float a = 8388608333e+298;
    int b = 20;
    float c = float:Infinity;
    float d = float:NaN;
    int e = 0;

    float var4 = c / b;
    assertEqual(var4, float:Infinity);

    float var5 = d / b;
    assertEqual(var5, float:NaN);

    float var7 = a / e;
    assertEqual(var7, float:Infinity);

    float var8 = c / e;
    assertEqual(var8, float:Infinity);

    float var9 = d / e;
    assertEqual(var9, float:NaN);

}

const decimal constDecimal = 20.5;

type MyDecimal decimal;

type FOUR_POINT_FIVE_DECIMAL 4.5d;

function testDivisionDecimalInt() {
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

    decimal var5 = c / a;
    assertEqual(var5, 0.225d);
    decimal var6 = d / a;
    assertEqual(var6, 0.0d);
    decimal var61 = h / a;
    assertEqual(var61, 2.25d);
    decimal var62 = j / a;
    assertEqual(var62, 2.25d);
    decimal var63 = m / a;
    assertEqual(var63, 10.25d);
    decimal var64 = p / a;
    assertEqual(var64, 2.25d);

    decimal var11 = c / constInt;
    assertEqual(var11, 0.09d);
    decimal var12 = d / constInt;
    assertEqual(var12, 0d);
    decimal var121 = h / constInt;
    assertEqual(var121, 0.9d);
    decimal var122 = j / constInt;
    assertEqual(var122, 0.9d);
    decimal var123 = m / constInt;
    assertEqual(var123, 4.1d);
    decimal var124 = p / constInt;
    assertEqual(var124, 0.9d);

    decimal var13 = constDecimal / constInt;
    assertEqual(var13, 4.1d);

    decimal var15 = constDecimal / a;
    assertEqual(var15, 10.25d);
    decimal var16 = constDecimal / i;
    assertEqual(var16, 10.25d);

    decimal var17 = c / e;
    assertEqual(var17, 4.878909776184769953562510061784767E-20d);
    decimal var18 = c / f;
    assertEqual(var18, -4.878909776184769953033537603914738E-20d);
    decimal var19 = c / g;
    assertEqual(var19, 0.225d);
    decimal var191 = c / i;
    assertEqual(var191, 0.225d);
    decimal var192 = c / k;
    assertEqual(var192, 0.09d);
    decimal var193 = c / n;
    assertEqual(var193, 0.225d);

    decimal var20 = h / g;
    assertEqual(var20, 2.25d);
    decimal var201 = h / i;
    assertEqual(var201, 2.25d);
    decimal var202 = h / k;
    assertEqual(var202, 0.9d);
    decimal var203 = h / n;
    assertEqual(var203, 2.25d);

    decimal var21 = j / i;
    assertEqual(var21, 2.25d);

    decimal var22 = m / k;
    assertEqual(var22, 4.1d);
    decimal var23 = p / k;
    assertEqual(var23, 0.9d);

    decimal var24 = m / n;
    assertEqual(var24, 10.25d);
    decimal var25 = p / n;
    assertEqual(var25, 2.25d);
}

function testDivisionDecimalIntSubTypes() {
    int:Signed8 a = -2;
    int:Signed16 b = 2;
    int:Signed32 c = -4;
    int:Unsigned8 d = 4;
    int:Unsigned16 e = 5;
    int:Unsigned32 f = 10;
    byte g = 25;

    decimal h = 2.5;

    decimal var8 = h / a;
    assertEqual(var8, -1.25d);
    decimal var9 = h / b;
    assertEqual(var9, 1.25d);
    decimal var10 = h / c;
    assertEqual(var10, -0.625d);
    decimal var11 = h / d;
    assertEqual(var11, 0.625d);
    decimal var12 = h / e;
    assertEqual(var12, 0.5d);
    decimal var13 = h / f;
    assertEqual(var13, 0.25d);
    decimal var14 = h / g;
    assertEqual(var14, 0.1d);
}

function testDivisionDecimalIntWithNullableOperands() {
    int a = 2;
    int? b = 4;
    decimal c = 4.5e-1;
    decimal? d = -10.5;
    int? e = ();
    decimal? f = ();
    2? g = 2;
    5.5d h = 5.5d;

    decimal? var2 = d / a;
    assertEqual(var2, -5.25d);

    decimal? var4 = c / b;
    assertEqual(var4, 0.1125d);

    decimal? var6 = d / b;
    assertEqual(var6, -2.625d);

    decimal? var8 = d / constInt;
    assertEqual(var8, -2.1d);

    decimal? var9 = constDecimal / b;
    assertEqual(var9, 5.125d);

    decimal? var10 = c / e;
    assertEqual(var10, ());

    decimal? var11 = f / e;
    assertEqual(var11, ());

    decimal? var12 = f / a;
    assertEqual(var12, ());

    decimal? var13 = f / constInt;
    assertEqual(var13, ());

    decimal? var14 = constDecimal / e;
    assertEqual(var14, ());

    decimal? var15 = c / g;
    assertEqual(var15, 0.225d);

    decimal? var16 = h / a;
    assertEqual(var16, 2.75d);

    decimal? var17 = h / g;
    assertEqual(var17, 2.75d);
}

function testDivisionDecimalIntSubTypeWithNullableOperands() {
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

    decimal? var8 = r / a;
    assertEqual(var8, -1.25d);
    decimal? var9 = r / b;
    assertEqual(var9, 1.25d);
    decimal? var10 = r / c;
    assertEqual(var10, -0.5d);
    decimal? var11 = r / d;
    assertEqual(var11, 0.25d);
    decimal? var12 = r / e;
    assertEqual(var12, 0.5d);
    decimal? var13 = r / f;
    assertEqual(var13, 0.25d);
    decimal? var14 = r / g;
    assertEqual(var14, 0.625d);

    decimal? var22 = q / h;
    assertEqual(var22, -1.25d);
    decimal? var23 = q / i;
    assertEqual(var23, ());
    decimal? var24 = q / j;
    assertEqual(var24, ());
    decimal? var25 = q / k;
    assertEqual(var25, 0.625d);
    decimal? var26 = q / m;
    assertEqual(var26, 0.5d);
    decimal? var27 = q / n;
    assertEqual(var27, 0.25d);
    decimal? var28 = q / p;
    assertEqual(var28, ());

    decimal? var36 = r / h;
    assertEqual(var36, -1.25d);
    decimal? var37 = r / i;
    assertEqual(var37, ());
    decimal? var38 = r / j;
    assertEqual(var38, ());
    decimal? var39 = r / k;
    assertEqual(var39, 0.625d);
    decimal? var40 = r / m;
    assertEqual(var40, 0.5d);
    decimal? var41 = r / n;
    assertEqual(var41, 0.25d);
    decimal? var42 = r / p;
    assertEqual(var42, ());
}

function testResultTypeOfDivisionDecimalIntByInfering() {
    decimal a = 2.5;
    int b = 5;

    var c = a / b;
    decimal var1 = c;
    assertEqual(var1, 0.5d);

    var e = a / constInt;
    decimal var3 = e;
    assertEqual(var3, 0.5d);

    var g = constDecimal / b;
    decimal var5 = g;
    assertEqual(var5, 4.1d);

    var i = constDecimal / constInt;
    decimal var7 = i;
    assertEqual(var7, 4.1d);
}

function testResultTypeOfDivisionDecimalIntForNilableOperandsByInfering() {
    decimal? a = 2.5;
    int? b = 5;

    var c = a / b;
    decimal? var1 = c;
    assertEqual(var1, 0.5d);

    var e = a / constInt;
    decimal? var3 = e;
    assertEqual(var3, 0.5d);

    var g = constDecimal / b;
    decimal? var5 = g;
    assertEqual(var5, 4.1d);

    var i = constDecimal / constInt;
    decimal? var7 = i;
    assertEqual(var7, 4.1d);
}

int intVal = 10;

function testNoShortCircuitingInDivisionWithNullable() {
    int? result = foo() / bar();
    assertEqual(result, ());
    assertEqual(intVal, 18);

    result = foo() / 12;
    assertEqual(result, ());
    assertEqual(intVal, 20);

    result = 12 / bar();
    assertEqual(result, ());
    assertEqual(intVal, 26);

    int? x = 20;
    result = foo() / x;
    assertEqual(result, ());
    assertEqual(intVal, 28);

    result = x / bar();
    assertEqual(result, ());
    assertEqual(intVal, 34);

    result = x / bam();
    assertEqual(result, 4);
    assertEqual(intVal, 44);

    result = bam() / x;
    assertEqual(result, 0);
    assertEqual(intVal, 54);

    result = foo() / bam();
    assertEqual(result, ());
    assertEqual(intVal, 66);

    result = bam() / bar();
    assertEqual(result, ());
    assertEqual(intVal, 82);
}

function testNoShortCircuitingInDivisionWithNonNullable() {
    intVal = 10;
    int x = 10;

    int result = x / bam();
    assertEqual(result, 2);
    assertEqual(intVal, 20);

    result = bam() / 2;
    assertEqual(result, 2);
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
