
import org.foo;


public function testPrivateFieldAccess() {
    foo:person p = foo:newPerson();

    string name = p.name;
    string ssn = p.ssn;

}

public type personFoo {
    int age;
    string name;
    string ssn;
    int id;
};

public function testCompileTimeStructEq() {
    personFoo pf = {age:10, name:"dd", ssn:"123-44-3333", id:123};
    foo:person p = check <foo:person>pf;

    //string name = p.name;
    //string ssn = p.ssn;

}