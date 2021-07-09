
import configRecordType.type_defs;
import testOrg/configLib.mod1 as configLib;
import ballerina/test;

configurable Engineer & readonly 'engineer = ?;
configurable Lecturer & readonly lecturer = ?;
configurable Lawyer lawyer = ?;
configurable readonly & record {
    string name;
    int id;
    type_defs:Address address = {city: "Galle"};
} person3 = ?;

//From non-default modules
configurable type_defs:Student & readonly student = ?;
configurable type_defs:Officer officer = ?;
configurable type_defs:Employee employee = ?;
configurable type_defs:Employee & readonly employee1 = ?;
configurable type_defs:Lecturer & readonly lecturer2 = ?;
configurable type_defs:Lawyer lawyer2 = ?;

// Complex records
configurable type_defs:Person person = ?;
configurable type_defs:Person person2 = ?;

//From non-default packages
configurable configLib:Manager & readonly manager = ?;
configurable configLib:Teacher & readonly teacher = ?;
configurable configLib:Farmer farmer = ?;

public function testRecords() {
    test:assertEquals(engineer.name, "waruna");
    test:assertEquals(engineer.id, 555);
    test:assertEquals(student.name, "riyafa");
    test:assertEquals(student.id, 444);
    test:assertEquals(employee.name, "manu");
    test:assertEquals(employee.id, 101);
    test:assertEquals(officer.name, "gabilan");
    test:assertEquals(officer.id, 101);
    test:assertEquals(manager.name, "hinduja");
    test:assertEquals(manager.id, 107);
    test:assertEquals(teacher.name, "gabilan");
    test:assertEquals(teacher.id, 888);
    test:assertEquals(farmer.name, "waruna");
    test:assertEquals(farmer.id, 999);
    test:assertEquals(person.name, "waruna");
    test:assertEquals(person.id, 10);
    test:assertEquals(person.address.city, "San Francisco");
    test:assertEquals(person.address.country.name, "USA");
    test:assertEquals(person2.name, "manu");
    test:assertEquals(person2.id, 11);
    test:assertEquals(person2.address.city, "Nugegoda");
    test:assertEquals(person2.address.country.name, "SL");
    test:assertEquals(person3.name, "riyafa");
    test:assertEquals(person3.id, 12);
    test:assertEquals(person3.address.city, "Galle");
    test:assertEquals(person3.address.country.name, "SL");
}

public function testComplexRecords() {
    test:assertEquals(lecturer.toString(), "{\"name\":\"hinduja\",\"department1\":{\"name\":\"IT\"}," + 
    "\"department2\":{\"name\":\"Finance\"},\"department3\":{\"name\":\"HR\"}}");
    test:assertEquals(lawyer.toString(), "{\"name\":\"riyafa\",\"address1\":{\"city\":\"Colombo\"}," + 
    "\"address2\":{\"city\":\"Kandy\"},\"address3\":{\"city\":\"Galle\"}}");
    test:assertEquals(lecturer2.toString(), "{\"name\":\"hinduja\",\"department1\":{\"name\":\"IT\"}," + 
    "\"department2\":{\"name\":\"Finance\"},\"department3\":{\"name\":\"HR\"}}");
    test:assertEquals(lawyer2.toString(), "{\"name\":\"riyafa\",\"place1\":{\"city\":\"Colombo\"}," + 
    "\"place2\":{\"city\":\"Kandy\"},\"place3\":{\"city\":\"Galle\"}}");
 }
