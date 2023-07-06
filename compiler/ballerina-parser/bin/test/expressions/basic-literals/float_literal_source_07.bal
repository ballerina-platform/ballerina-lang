function foo() {
    // HexIndicator DottedHexNumber
    a = 0x3eA3.;

    // HexIndicator DottedHexNumber HexExponent
    a = 0x3eA3.p45;
    a = 0x3eA3.P45;
    a = 0x.p45;
    a = 0x.P45;
    a = 0X3eA3.p45;
    a = 0X3eA3.P45;
    a = 0X.p45;
    a = 0X.2P45;

    // HexIndicator DottedHexNumber HexExponentWithSign
    a = 0x3eA3.p+45;
    a = 0x3eA3.P-45;
    a = 0x.p+45;
    a = 0x.P-45;
    a = 0X3eA3.p+45;
    a = 0X3eA3.P-45;
    a = 0X.p+45;
    a = 0X.P-45;

    // Misc
    a = 0x.;
    a = 0X.;
    a = 0x.+0x11;
    a = 0xAB.+0x11;
    a = 0x.max;
}
