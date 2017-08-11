struct Person {
    string fname;
    string lname;
    function (string, string) returns (string) getName;
}

function getFullName (string f, string l) returns (string r){
    return l + ", " + f;
}

function test1()(string x, string y){
    Person bob = {fname:"bob", lname:"white", getName: function (string fname, string lname) returns (string result){
                                                              return fname + " " + lname;
                                                          }};
    Person tom = {fname:"tom", lname:"smith", getName: getFullName};

    x = bob.getFullName(bob.fname, bob.lname );
    y = tom.getName(tom.fname, tom.lname );
    return;
}