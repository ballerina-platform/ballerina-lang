transformer <string s, any a> testImplicit() {}

transformer <any a, boolean b> testUnsafeCast() {}

transformer <float f, int i> testSafeConversion() {}

transformer <string s, float f> testUnsafeConversion() {}