import org.foo.attached_funcs as af;
import org.foo;
import org.foo.bar;

public struct person {
    int age;
    string name;
    string address;
    string zipcode = "95134";
    private:
    string ssn;
    int id;
}

public function <person p> getName () returns (string) {
    return p.name;
}

public function <person p> getAge () returns (int) {
    return p.age;
}

function <person p> getSSN () returns (string) {
    return p.ssn;
}

function <person p> setSSN (string ssn) {
    p.ssn = ssn;
}

public struct employee {
    int age;
    string name;
    string address;
    string zipcode = "95134";
    private:
    string ssn;
    int id;
    int employeeId = 123456;
}

public function <employee e> getName () returns (string) {
    return e.name;
}

public function <employee e> getAge () returns (int) {
    return e.age;
}

function <employee e> getSSN () returns (string) {
    return e.ssn;
}

function <employee e> setSSN (string ssn) {
    e.ssn = ssn;
}

public function <employee e> getEmployeeId () returns (int) {
    return e.employeeId;
}

function testEquivalenceOfStructsInSamePackage () returns (string) {
    employee e = {age:14, name:"rat"};
    e.setSSN("234-56-7890");

    person p = (person)e;

    return p.getSSN();
}


function testEquivalenceOfStructsInSamePackageFromDifferentPackage () returns (string) {
    af:employee e = {age:14, name:"rat"};
    e.setSSN("234-56-7890");

    af:person p = (af:person)e;

    return p.getSSN();
}

function handleUser (foo:user u) (string) {
    return u.name;
}


function testStructEqViewFromThirdPackage () (string) {
    bar:officer ub = {name:"ball"};
    return handleUser((foo:user)ub);
}


public struct userA {
    int age;
    string name;
}

public function <userA ua> getName () returns (string) {
    return ua.name;
}

public function <userA ua> getAge () returns (int) {
    return ua.age;
}

public struct userB {
    int age;
    string name;
    string address;
}

public function <userB ub> getName () returns (string) {
    return ub.name;
}

public function <userB ub> getAge () returns (int) {
    return ub.age;
}


public function testRuntimeStructEq () returns (string) {
    foo:userFoo u = {age:10, name:"ttt", address:"102 Skyhigh street #129, San Jose"};


    // This is a safe cast
    var uA = (userA)u;

    // This is a unsafe cast
    var uB, err = (userB)uA;
    if (err != null) {
        return err.msg;
    }
    return uB.name;
}
