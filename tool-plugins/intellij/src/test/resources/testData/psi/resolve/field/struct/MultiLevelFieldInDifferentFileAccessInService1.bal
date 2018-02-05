service<http> test {

    resource test (message m) {
        Name name = {firstName:""};
        Person person = {/*ref*/name:name};
    }
}
