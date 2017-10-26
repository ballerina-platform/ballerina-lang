struct Person {
    string fname;
    string lname;
    function (string, string) returns (string) getName;
}

function getFullName (string f, string l) returns (string r){
    return l + ", " + f;
}

function test1()(string x, string y){
    Person bob = {fname:"bob", lname:"white"};
    Person tom = {fname:"tom", lname:"smith", getName: getFullName};

    x = bob.getName(bob.fname, bob.lname );
    y = tom.getName(tom.fname, tom );
    return;
}