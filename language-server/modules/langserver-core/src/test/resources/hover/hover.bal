

import ballerina/io;

@Description {value:"Struct for represent person's details"}
@Field{value:"name: string value name of the person"}
@Field{value:"id: int value id of the person"}
@Field{value:"age: int value age of the person"}
type Person {
    string name;
    int id;
    int age;
};

@Description{value:"Test function to show current package works"}
@Param{value:"s: string parameter"}
@Param{value:"sd: int parameter"}
@Return{value:"return an int"}
function test1 (string s, int sd) returns int{
    int a = 0;
    return a;
}

















function main (string[] args) {
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