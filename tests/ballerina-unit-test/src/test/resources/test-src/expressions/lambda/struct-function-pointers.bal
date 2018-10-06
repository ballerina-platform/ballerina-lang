type Person record {
    string fname;
    string lname;
    function (string, string) returns (string) getName;
    function (Person p) returns (string) getPersonName;
};

function getFullName (string f, string l) returns (string){
    return l + ", " + f;
}

function test1() returns (string, string){
    Person bob = {fname:"bob", lname:"white", getName: function (string fname, string lname) returns (string){
                                                              return fname + " " + lname;
                                                          }};
    Person tom = {fname:"tom", lname:"smith", getName: getFullName};

    string x = bob.getName.call(bob.fname, bob.lname );
    string y = tom.getName.call(tom.fname, tom.lname );
    return (x, y);
}

function test2() returns (string){
    Person bob = {fname:"bob", lname:"white"};

    string x = bob.getName.call(bob.fname, bob.lname );
    return x;
}

function getPersonNameFullName (Person p) returns (string){
    return p.lname + ", " + p.fname;
}

function test3() returns (string){
    Person bob = {fname:"bob", lname:"white", getPersonName : getPersonNameFullName };
    string x = bob.getPersonName.call(bob);
    return x;
}
