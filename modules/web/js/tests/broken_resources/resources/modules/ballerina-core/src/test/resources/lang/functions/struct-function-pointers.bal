struct Person {
    string fname;
    string lname;
    function (string, string) returns (string) getName;
    function (Person p) returns (string) getPersonName;
}

function getFullName (string f, string l) returns (string){
    return l + ", " + f;
}

function test1()(string x, string y){
    Person bob = {fname:"bob", lname:"white", getName: function (string fname, string lname) returns (string result){
                                                              return fname + " " + lname;
                                                          }};
    Person tom = {fname:"tom", lname:"smith", getName: getFullName};

    x = bob.getName(bob.fname, bob.lname );
    y = tom.getName(tom.fname, tom.lname );
    return;
}

function test2()(string x){
    Person bob = {fname:"bob", lname:"white"};

    x = bob.getName(bob.fname, bob.lname );
    return;
}

function getPersonNameFullName (Person p) returns (string){
    return p.lname + ", " + p.fname;
}

function test3()(string x){
    Person bob = {fname:"bob", lname:"white", getPersonName : getPersonNameFullName };
    x = bob.getPersonName(bob);
    return;
}
