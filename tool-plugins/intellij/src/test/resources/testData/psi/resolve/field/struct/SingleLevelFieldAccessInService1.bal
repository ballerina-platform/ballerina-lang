struct testStruct{
    string /*def*/s;
}

service<http> test {

    resource test () {
        testStruct ts = {/*ref*/s:""};
    }
}
