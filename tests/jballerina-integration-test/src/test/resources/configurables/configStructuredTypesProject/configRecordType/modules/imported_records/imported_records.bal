import configRecordType.type_defs;
import testOrg/configLib.mod1 as configLib;
import ballerina/test;

public type Doctor record {|
    string name = "";
    int id = 555;
|};

configurable Doctor & readonly doctor = ?;

configurable type_defs:Student & readonly student = ?;
configurable type_defs:Officer officer = ?;
configurable type_defs:Employee employee = ?;
configurable type_defs:Employee & readonly employee1 = ?;
configurable type_defs:Person person = ?;

configurable configLib:Manager & readonly manager = ?;
configurable configLib:Teacher & readonly teacher = ?;
configurable configLib:Farmer farmer = ?;

public function testRecords() {
    test:assertEquals(doctor.name, "waruna");
    test:assertEquals(doctor.id, 555);
    test:assertEquals(student.name, "riyafa");
    test:assertEquals(student.id, 444);
    test:assertEquals(employee.name, "manu");
    test:assertEquals(employee.id, 101);
    test:assertEquals(employee1.name, "waruna");
    test:assertEquals(employee1.id, 404);
    test:assertEquals(officer.name, "gabilan");
    test:assertEquals(officer.id, 101);
    test:assertEquals(manager.name, "hinduja");
    test:assertEquals(manager.id, 107);
    test:assertEquals(teacher.name, "hinduja");
    test:assertEquals(teacher.id, 11);
    test:assertEquals(farmer.name, "manu");
    test:assertEquals(farmer.id, 22);
    test:assertEquals(person.name, "hinduja");
    test:assertEquals(person.id, 100);
    test:assertEquals(person.address.city, "Kandy");
    test:assertEquals(person.address.country.name, "Sri Lanka");
}
