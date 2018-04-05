type Person {
    string fname;
    string lname;
    function (string, string) returns (string) getName;
}

function getFullName (string f, string l) returns (string){
    return l + ", " + f;
}

function test1() returns (string, string){
    Person bob = {fname:"bob", lname:"white"};
    Person tom = {fname:"tom", lname:"smith", getName: getFullName};

    string x = bob.getName(bob.fname, bob.lname );
    string y = tom.getName(tom.fname, tom );
    return (x, y);
}