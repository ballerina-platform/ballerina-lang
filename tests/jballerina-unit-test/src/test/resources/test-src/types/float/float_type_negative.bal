function testInvalidHexaDecimalWithFloatType() {
    float a = 0xFFFFFFFFFFFFFFFF;

    a = 0xabc435de769FEAB0;

    a = 0xaaaaaaaaaaaaaaa0;

    a = 0xAAAAAAAAAAAAAAA0;

    float|decimal b = 0xFFFFFFFFFFFFFFFF;

    b = 0xabc435de769FEAB0;

    b = 0xaaaaaaaaaaaaaaa0;

    b = 0xAAAAAAAAAAAAAAA0;
}

float x1 = 0xFFFFFFFFFFFFFFFF;

float x2 = 0xabc435de769FEAB0;

float x3 = 0xaaaaaaaaaaaaaaa0;

float x4 = 0xAAAAAAAAAAAAAAA0;

float|decimal y1 = 0xFFFFFFFFFFFFFFFF;

float|decimal y2 = 0xabc435de769FEAB0;

float|decimal y3 = 0xaaaaaaaaaaaaaaa0;

float|decimal y4 = 0xAAAAAAAAAAAAAAA0;

type FloatType float;

FloatType u1 = 0xFFFFFFFFFFFFFFFF;

type FloatType2 float|decimal;

FloatType2 u2 = 0xFFFFFFFFFFFFFFFF;

function testInvalidHexaDecimalWithFloatType2() {
    FloatType u3 = 0xFFFFFFFFFFFFFFFF;
    FloatType u4 = 0Xffffffffffffffff;
    FloatType2 u5 = 0XFFFFFFFFFFFFFFFF;

    float u6 = 0x;
    u6 = 0X;

    FloatType u7 = 0x;

    float|decimal u8 = 0x;
    u8 = 0X;

    FloatType2 u9 = 0x;
}
