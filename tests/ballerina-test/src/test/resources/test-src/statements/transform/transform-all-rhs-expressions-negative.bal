struct Person {
    string firstname;
    string lastname;
    int age;
    string city;
    string prefix = "Mr.";
    string[] addresses;
}

struct Employee {
    string fname;
    string lname;
    int age;
    string city;
    string prefix = "Mr.";
    string[] addresses;
}

function testTransformWithAllExprs() {
    TestConnector con;
    Person p = {firstname:"John", lastname:"Doe", age:30, city:"London"};
    Employee e1 = <Employee, Foo_1(con)> p;
}

function <Person p1> getPrefixedName_1(Person p2) returns (string) {
    return p2.prefix + p1.firstname;
}

function <Employee e1> getPrefixedName_2(Employee e2) returns (string) {
    return e2.prefix + e1.fname;
}

transformer <Person p, Employee e> Foo_1(TestConnector con) {
    Person p1 = p; // simple var ref
    
    string name = p.getPrefixedName_1(p); // invocation expr

    // ternary expr
    boolean old = (p.age > 60) ? true: false;
    
    // binary expr
    string concatName = p.firstname + p.lastname;
    
    // unary expr
    int l = lengthof p.addresses;
    
    // record lieteral
    map m = {"firstname": p.firstname};
    
    // array literal;
    Person[] ppl = [p];
    
    // index access expr
    string key = "address";
    var address, _ = (string) m[key];
    
    // xml exprs
    xml x = xml `<{{p.firstname}} {{p.city}}="{{p.age}}"><{{p.lastname}}/><!-- {{p.firstname}} -->{{p.firstname}}<?p.firstname {{p.lastname}}?></{{p.firstname}}>`;
    any a = x@;
    string s1 = x@[p.city];
    
    // string template
    string s2 = string `hello {{p.firstname}}`;
        
    Employee e1 = <Employee, Foo_1(con)> p;
}

transformer <Person p, Employee e> Foo_2(TestConnector con) {
    // simple var ref
    Employee e1 = e;
    
    // invocation expr
    string name = e.getPrefixedName_2(e);

    // ternary expr
    boolean old = (e.age > 60) ? true: false;
    
    // binary expr
    string concatName = e.fname + e.lname;
    
    // unary expr
    int l = lengthof e.addresses;
    
    // record lieteral
    map m = {"firstname": e.fname};
    
    // array literal;
    Employee[] ppl = [e];
    
    // index access expr
    string key = "address";
    var address, _ = (string) m[key];
    
    // xml exprs
    xml x = xml `<{{e.fname}} {{e.city}}="{{e.age}}"><p.lname/><!-- {{e.fname}} -->hello {{e.fname}}<?doc {{e.lname}}?></{{e.fname}}>`;
    any a = x@;
    string s1 = x@[e.city];
    
    // string template
    string s2 = string `hello {{e.fname}}`;
}

connector TestConnector() {
    action textAction_1() (string) {
        return "hello";
    }
    
    action textAction_2(TestConnector con) (string) {
        return "hello";
    }
    
    action textAction_3(Employee e) (string) {
        return "hello";
    }
}

struct testEP {
}

function <testEP ep> init(string name, struct {} config) {
}

function <testEP ep> start() {
}

function <testEP ep> stop() {
}

function <testEP ep> register(type t) {
}

function <testEP ep> getConnector() returns (TestConnector) {
    return null;
}