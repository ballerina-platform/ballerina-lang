function foo() {
    // method-name is HexFloatingPoint with only missing hex digit after dot diagnostic
    var s1 = 0x12.p8();
    var s2 = 0x12.P8();
    var s3 = 0X12.p8();
    var s4 = 0X12.P8();

    var s11 = 0x12.p+8();
    var s12 = 0x12.P-8();
    var s13 = 0X12.p+8();
    var s14 = 0X12.P-8();

    // method-name is a HexFloatingPoint with missing digit after exponent indicator diagnostic
    var s51 = 0x12.Ep();
    var s52 = 0x12.EP();
    var s53 = 0X12.Ep();
    var s54 = 0X12.EP();

    var s61 = 0x12.EA3p();
    var s62 = 0x12.EA3P();
    var s63 = 0X12.EA3p();
    var s64 = 0X12.EA3P();

    // method-name is a HexFloatingPoint with following diagnostics
    // 1. missing hex digit after dot
    // 2. missing digit after exponent indicator
    var s21 = 0x12.p();
    var s22 = 0x12.P();
    var s23 = 0X12.p();
    var s24 = 0X12.P();

    // misc
    var s31 = 0x12.P+P();
    var s32 = 0X12.P+P1();
    var s33 = 0X12.p-p();
    var s34 = 0x12.p-p1();

    var s41 = 0X12.p12+p();
    var s42 = 0x12.p12+p1();
    var s43 = 0x12.P12-P();
    var s44 = 0X12.P12-P1();
}
