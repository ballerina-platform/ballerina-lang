function invalidCast () {
    string a = "exception can't be cast";
    exception e2 = {};
    string cast;
    cast =  (string) e2;
}