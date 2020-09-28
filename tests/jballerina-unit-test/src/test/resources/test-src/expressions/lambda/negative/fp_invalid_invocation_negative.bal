type Person record {
    string fname;
    string lname;
    function (string, string) returns (string) getName;
};

class Employee {
    string fname = "John";
    string lname = "Doe";

    function () returns (string) getLname;

    function init() {
        self.getLname = function () returns (string) {
                            return self.fname;
                        };
    }

    public function getFname() returns (string) {
        return self.fname;
    }
}


function getFullName (string f, string l) returns (string){
    return l + ", " + f;
}

function test1() {
    Person bob = {fname:"bob", lname:"white", getName: function (string fname, string lname) returns (string) {
                                                              return fname + " " + lname;
                                                          }};
    Person tom = {fname:"tom", lname:"smith", getName: getFullName};

    string x = bob.getFullName(bob.fname, bob.lname );
    string y = tom.getName(tom.fname, tom.lname );

    var f1 = foo;
    f1();
    
    var f2 = bob.getName;
    _ = f2("John", "Doe");
    
    Employee e = new;
    var f3 = e.getFname;
    _ = f3();
    
    var f4 = e.getLname;
    _ = f4();
    
    var getLname = e.getLname;
    _ = getLname();
    
    _ = e.getFname();
}

function foo() {

}

// Same function using function pointer invocation
function test2() {
    Person bob = {fname:"bob", lname:"white", getName: function (string fname, string lname) returns (string) {
                                                              return fname + " " + lname;
                                                          }};
    Person tom = {fname:"tom", lname:"smith", getName: getFullName};

    string y = tom.getName(tom.fname, tom.lname );

    var f1 = foo;
    f1();
    
    var f2 = bob.getName;
    _ = f2("John", "Doe");
    
    Employee e = new;
    var f3 = e.getFname;
    _ = f3();
    
    var f4 = e.getLname;
    _ = f4();
    
    _ = e.getLname();
}