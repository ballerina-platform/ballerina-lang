struct /*def*/testStruct {
    string a;
}

service test {

    resource test (message m) {
        /*ref*/testStruct ts;
    }
}
