import org.bar;
import ballerina/http;

public function testReadOnlyFieldAccessInDifferentPackage() returns (int, string) {
    bar:Person p = bar:createPerson();
    int age = p.age;
    float salary = p.salary;
    p.salary = 150.5;
    p.age = 44;
    return (age, p.name);
}

public function testReadOnlyGlobalVarAccessInDifferentPackage() returns (int, int, int) {
    int v1 = bar:globalInt;
    bar:globalInt = 44;
    int v2 = bar:globalInt;
    return (v1, v2, bar:globalInt);
}

@readonly function finalFunction() {
    int i = 0;
}

@readonly service<http:Service> FooService {

}



