function foo() {
    a = 12.e
    a = 12.E
    a = 12.e+
    a = 12.E+
    a = 12.e+
    a = 12.E-

    a = 12.p
    a = 12.P
    a = 12.p+
    a = 12.P+
    a = 12.p-
    a = 12.P-

    a = 12.p
    a = 12.P
    a = 12.p+
    a = 12.P+
    a = 12.p-
    a = 12.P-

    a = 12.e;
    a = 12.E;

    a = 12.p;
    a = 12.P;

    a = 12.p12;
    a = 12.P12;
    a = 12.p+12;
    a = 12.P+12;
    a = 12.p-12;
    a = 12.P-12;

    a = 012.12;
    a = 0012.123;
    a = 0012.11e12;
    a = 012.11e-12;
    a = 0000012.11e8;

    a = 12.2p12;
    a = 12.2p+12;
    a = 12.2P+12;
    a = 12.2P+12;

    a = 0x;
    a = 0X;

    a = 0X12G23;
    a = 0X12G23.23EGE;

    // HexFloatingPointLiteral without HexIndicator
    a = 3eA3.4Eb2;
    a = .4Eb2;

    a = 3eA3.4Eb2p45;
    a = 3eA3.4Eb2P45;
    a = .4Eb2p45;
    a = .4Eb2P45;

    a = 3eA3.4Eb2p+45;
    a = 3eA3.4Eb2P-45;
    a = .4Eb2p+45;
    a = .4Eb2P-45;
}
