function invalidByteShiftOperators() {
    byte q = 7 > > 3;
    byte t = 6 < < 9;
    byte u = 5 > >> 2;
    byte v = 5 >> > 2;
    byte p = 5 > > > 2;
    byte r = 5 >    >     > 2;
}
