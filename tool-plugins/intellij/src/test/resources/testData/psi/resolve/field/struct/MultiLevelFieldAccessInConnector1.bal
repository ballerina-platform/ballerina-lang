struct Name {
    string firstName;
}
struct Person {
    Name /*def*/name;
}

connector test() {

    action test () {
        Name name = {firstName:""};
        Person person = {/*ref*/name:name};
    }
}
