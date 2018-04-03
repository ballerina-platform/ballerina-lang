import org.bar;

public function testReadOnlyAccess() returns (int, string) {
    bar:Person p = bar:createPerson();
    int age = p.age;
    transaction {

    }
    p.age = 44;
    return (age, p.name);
}
