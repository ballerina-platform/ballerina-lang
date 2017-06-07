struct Name {
    string firstName;
}
struct Person {
    Name /*def*/name;
}

connector test() {

    action test () {
        Name name = {firstName:""};
        Person person = {};
        person./*ref*/name=name;
    }
}
