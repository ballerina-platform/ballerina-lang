

import ballerina/io;

# Struct for represent person's details
#
# + name - string value name of the person
# + id - int value id of the person 
# + age - int value age of the person
type Person record {
    string name;
    int id;
    int age;
};

# Test function to show current package works
#
# + s - string parameter 
# + sd - int parameter 
# + return - return an int
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