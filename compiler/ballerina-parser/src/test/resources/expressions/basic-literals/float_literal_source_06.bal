function foo() {
    // DottedDecimalNumber
    a = 25.;

    // DottedDecimalNumber Exponent
    a = 25.e1742;
    a = 25.E1742;

    // DottedDecimalNumber ExponentWithSign
    a = 25.e+1742;
    a = 25.E-1742;

    // DottedDecimalNumber FloatingPointTypeSuffix
    a = 25.f;
    a = 25.F;
    a = 25.d;
    a = 25.D;

    // DottedDecimalNumber Exponent FloatingPointTypeSuffix
    a = 25.e1742f;
    a = 25.e1742F;
    a = 25.e1742d;
    a = 25.e1742D;
    a = 25.E1742f;
    a = 25.E1742F;
    a = 25.E1742d;
    a = 25.E1742D;

    // DottedDecimalNumber ExponentWithSign FloatingPointTypeSuffix
    a = 25.e+1742f;
    a = 25.e-1742F;
    a = 25.e+1742d;
    a = 25.e-1742D;
    a = 25.E+1742f;
    a = 25.E-1742F;
    a = 25.E+1742d;
    a = 25.E-1742D;
}
