transformer <string s, any a> testImplicit() {
    a = s;}

transformer <any a, boolean b> testUnsafeCast() {
    b,_ = (boolean)a;}

transformer <float f, int i> testSafeConversion() {
    i = <int>f;}

transformer <string s, float f> testUnsafeConversion() {
    f,_ = <float>s;}