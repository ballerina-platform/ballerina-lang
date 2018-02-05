struct /*def*/testStruct {
    string a;
}

service<http> test {

    resource test (message m) {
        /*ref*/testStruct ts;
    }
}
