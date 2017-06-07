struct testStruct{
    string /*def*/s;
}

service test {

    resource test () {
        testStruct ts = {/*ref*/s:""};
    }
}
