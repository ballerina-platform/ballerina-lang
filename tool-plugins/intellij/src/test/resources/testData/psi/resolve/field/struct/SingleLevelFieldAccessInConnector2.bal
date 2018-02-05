struct testStruct{
    string /*def*/s;
}

connector test () {

    action test () {
        testStruct ts = {};
        ts./*ref*/s:"";
    }
}
