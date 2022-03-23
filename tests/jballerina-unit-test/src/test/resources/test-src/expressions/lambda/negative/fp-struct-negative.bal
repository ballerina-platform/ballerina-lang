type Person record {
    string fname;
    string lname;
    function (string, string) returns (string) getName;
};

function getFullName (string f, string l) returns (string){
    return l + ", " + f;
}

function test1() returns [string, string]{
    Person bob = {fname:"bob", lname:"white", getName: function (string fname, string lname) returns (string) {
                                                              return fname + " " + lname;
                                                          }};
    Person tom = {fname:"tom", lname:"smith", getName: getFullName};

    function (string, string) returns (string) fp = bob.getFullName;
    string x = fp(bob.fname, bob.lname );
    string y = tom.getName(tom.fname, tom.lname );
    return [x, y];
}