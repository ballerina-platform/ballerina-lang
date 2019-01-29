type Name record {
    string firstName;
};
type Person record {
    Name /*def*/name;
};

function test () {
    Name name = {firstName:""};
    Person person = {};
    person./*ref*/name=name;
}
