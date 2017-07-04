struct Name {
    string firstName;
}
struct Person {
    Name /*def*/name;
}
service<http> test {

    resource test (message m) {
        Name name = {firstName:""};
        Person person = {};
        person./*ref*/name=name;
    }
}
