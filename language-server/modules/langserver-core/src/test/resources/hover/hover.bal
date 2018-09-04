

import ballerina/io;

documentation {
    Struct for represent person's details
    F{{name}} string value name of the person
    F{{id}} int value id of the person
    F{{age}} int value age of the person
}
type Person record {
    string name;
    int id;
    int age;
};

documentation {
    Test function to show current package works
    P{{s}} string parameter
    P{{sd}} int parameter
    R{{}} return an int
}
function test1 (string s, int sd) returns int{
    int a = 0;
    return a;
}

















public function main (string... args) {
    string s = "mars";
    io:println(s);
    var df = s.contains("mar");
    var x = test1("s",0);
    Person p = {
                   id:1,
                   age: 21,
                   name:"mike"
               };
    string name = p.name;
    if(p.name == "mike"){

    }
}