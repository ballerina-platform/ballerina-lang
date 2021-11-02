function foo() {
    // method-name is DecimalFloatingPoint with only missing digit after dot diagnostic
    var s1 = 12.d();
    var s2 = 12.D();
    var s3 = 12.f();
    var s4 = 12.F();

    var s11 = 12.e8();
    var s12 = 12.e8d();
    var s13 = 12.e8D();
    var s14 = 12.e8f();
    var s15 = 12.e8F();

    var s21 = 12.E8();
    var s22 = 12.E8d();
    var s23 = 12.E8D();
    var s24 = 12.E8f();
    var s25 = 12.E8F();

    var s52 = 12.e+1();
    var s53 = 12.e+1d();
    var s54 = 12.e+1D();
    var s55 = 12.e+1f();
    var s56 = 12.e+1F();

    var s62 = 12.E+1();
    var s63 = 12.E+1d();
    var s64 = 12.E+1D();
    var s65 = 12.E+1f();
    var s66 = 12.E+1F();

    var s72 = 12.e-1();
    var s73 = 12.e-1d();
    var s74 = 12.e-1D();
    var s75 = 12.e-1f();
    var s76 = 12.e-1F();

    var s82 = 12.E-1();
    var s83 = 12.E-1d();
    var s84 = 12.E-1D();
    var s85 = 12.E-1f();
    var s86 = 12.E-1F();

    // method-name is a DecimalFloatingPoint with following diagnostics
    // 1. missing digit after dot
    // 2. missing digit after exponent indicator
    var s31 = 12.E();
    var s32 = 12.Ed();
    var s33 = 12.ED();
    var s34 = 12.Ef();
    var s35 = 12.EF();

    var s41 = 12.E();
    var s42 = 12.Ed();
    var s43 = 12.ED();
    var s44 = 12.Ef();
    var s45 = 12.EF();

    // misc
    var s91 = 12.E+E();
    var s92 = 12.E+E1();
    var s93 = 12.e-e();
    var s94 = 12.e-e1();

    var s101 = 12.e12+e();
    var s102 = 12.e12+e1();
    var s103 = 12.E12-E();
    var s104 = 12.E12-E1();
}
