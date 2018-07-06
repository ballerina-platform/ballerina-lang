
import p.q;

function testTransformer() returns (string, int, string) {
    q:Person p = {fname:"Jane", lname:"Doe", age:25, city:"Paris"};
    q:Employee e = <q:Employee, q:Foo()> p;
    return (e.name, e.age, e.address);
}
