function foo() {
    // HexIndicator DottedHexNumber
    a = 0x3eA3.4Eb2;
    a = 0x.4Eb2;
    a = 0X3eA3.4Eb2;
    a = 0X.4Eb2;

    // HexIndicator DottedHexNumber HexExponent
    a = 0x3eA3.4Eb2p45;
    a = 0x3eA3.4Eb2P45;
    a = 0x.4Eb2p45;
    a = 0x.4Eb2P45;
    a = 0X3eA3.4Eb2p45;
    a = 0X3eA3.4Eb2P45;
    a = 0X.4Eb2p45;
    a = 0X.4Eb2P45;

    // HexIndicator DottedHexNumber HexExponentWithSign
    a = 0x3eA3.4Eb2p+45;
    a = 0x3eA3.4Eb2P-45;
    a = 0x.4Eb2p+45;
    a = 0x.4Eb2P-45;
    a = 0X3eA3.4Eb2p+45;
    a = 0X3eA3.4Eb2P-45;
    a = 0X.4Eb2p+45;
    a = 0X.4Eb2P-45;
}
