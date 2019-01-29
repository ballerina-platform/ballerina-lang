type Name record {
    string firstName;
};
type Person record {
    Name /*def*/name;
};

function test () {
    Name name = {firstName:""};
    Person person = {/*ref*/name:name};
}
