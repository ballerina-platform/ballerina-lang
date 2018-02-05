struct Name {
    string firstName;
}
struct Person {
    Name /*def*/name;
}

function test () {
    Name name = {firstName:""};
    Person person = {};
    person./*ref*/name=name;
}
