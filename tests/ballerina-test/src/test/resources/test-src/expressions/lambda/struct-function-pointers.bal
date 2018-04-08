type Person {
    string fname;
    string lname;
    function (string, string) returns (string) getName;
    function (Person p) returns (string) getPersonName;
};

function getFullName (string f, string l) returns (string){
    return l + ", " + f;
}

function test1() returns (string, string){
    Person bob = {fname:"bob", lname:"white", getName: (string fname, string lname) => (string){
                                                              return fname + " " + lname;
                                                          }};
    Person tom = {fname:"tom", lname:"smith", getName: getFullName};

    string x = bob.getName(bob.fname, bob.lname );
    string y = tom.getName(tom.fname, tom.lname );
    return (x, y);
}

function test2() returns (string){
    Person bob = {fname:"bob", lname:"white"};

    string x = bob.getName(bob.fname, bob.lname );
    return x;
}

function getPersonNameFullName (Person p) returns (string){
    return p.lname + ", " + p.fname;
}

function test3() returns (string){
    Person bob = {fname:"bob", lname:"white", getPersonName : getPersonNameFullName };
    string x = bob.getPersonName(bob);
    return x;
}
