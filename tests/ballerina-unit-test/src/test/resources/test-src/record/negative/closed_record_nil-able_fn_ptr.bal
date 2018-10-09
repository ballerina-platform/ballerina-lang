type Person record {
    string fname;
    string lname;
    (function (string, string) returns string)|() getName;
    !...
};

function testNilableFuncPtrInvocation() {
    Person bob = {fname:"bob", lname:"white"};
    bob.getName = function (string fname, string lname) returns string {
        return fname + " " + lname;
    };
    string x = bob.getName(bob.fname, bob.lname);
}

function testNilableFuncPtrInvocation2() {
    Person bob = {fname:"bob", lname:"white"};
    string x = bob.getName(bob.fname, bob.lname);
}
