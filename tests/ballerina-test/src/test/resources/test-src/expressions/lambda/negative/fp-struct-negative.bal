type Person {
    string fname;
    string lname;
    function (string, string) returns (string) getName;
}

function getFullName (string f, string l) returns (string){
    return l + ", " + f;
}

function test1() returns (string, string){
    Person bob = {fname:"bob", lname:"white", getName: (string fname, string lname) => (string){
                                                              return fname + " " + lname;
                                                          }};
    Person tom = {fname:"tom", lname:"smith", getName: getFullName};

    string x = bob.getFullName(bob.fname, bob.lname );
    string y = tom.getName(tom.fname, tom.lname );
    return (x, y);
}