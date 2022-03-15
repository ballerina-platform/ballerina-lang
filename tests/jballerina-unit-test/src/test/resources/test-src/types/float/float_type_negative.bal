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
