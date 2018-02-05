struct Name {
    string firstName;
}
struct Person {
    Name /*def*/name;
}

function test () {
    Name name = {firstName:""};
    Person person = {/*ref*/name:name};
}
