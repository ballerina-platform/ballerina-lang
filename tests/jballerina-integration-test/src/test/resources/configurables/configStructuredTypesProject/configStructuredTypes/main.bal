// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.


import configStructuredTypes.mod1;
import ballerina/jballerina.java;
import configStructuredTypes.mod2;
import configStructuredTypes.mod3;
import testOrg/configLib.mod1 as configLib;
import ballerina/test;

public type Engineer record {|
    string name = "";
    int id = 555;
|};

type Lecturer record {|
    string name;
    Department department1;
    Department department2?;
    readonly Department department3;
|};

type Department readonly & record {|
    string name;
|};

type Lawyer readonly & record {|
    string name;
    Address address1;
    Address address2?;
    readonly Address address3;
|};

type Address record {|
    string city;
|};

type Subject map<string>;

configurable Engineer & readonly 'engineer = ?;
configurable configLib:Manager & readonly manager = ?;
configurable configLib:Teacher & readonly teacher = ?;
configurable configLib:Farmer farmer = ?;
configurable mod1:Student & readonly student = ?;
configurable mod1:Officer officer = ?;
configurable mod1:Employee employee = ?;

configurable table<Engineer> & readonly engineerTable = ?;
configurable table<configLib:Manager> & readonly managerTable = ?;
configurable table<configLib:Teacher> & readonly teacherTable = ?;
configurable table<configLib:Farmer> & readonly farmerTable = ?;
configurable table<mod1:Student> & readonly studentTable = ?;
configurable table<mod1:Officer> & readonly officerTable = ?;
configurable table<mod1:Employee> & readonly employeeTable = ?;

// Readonly records
configurable mod1:Employee & readonly employee1 = ?;
configurable table<mod1:Officer & readonly> & readonly officerTable1 = ?;
configurable table<mod1:Employee & readonly> & readonly employeeTable1 = ?;

// Complex records
configurable mod1:Person person = ?;
configurable mod1:Person person2 = ?;
configurable readonly & record {
    string name;
    int id;
    mod1:Address address = { city:"Galle" };
} person3 = ?;
configurable table<mod1:Person> & readonly personTable = ?;
configurable mod1:Person[] & readonly personArray = ?;
// Need to enable this after fixing #31161
// configurable (mod1:Person & readonly)[] & readonly personArray1 = ?;

configurable Lecturer & readonly lecturer = ?;
configurable Lawyer lawyer = ?;
configurable mod1:Lecturer & readonly lecturer2 = ?;
configurable mod1:Lawyer lawyer2 = ?;
configurable table<Lecturer> & readonly lecturerTable = ?;
configurable table<Lawyer> & readonly lawyerTable = ?;
configurable table<mod1:Lecturer> & readonly lecturerTable2 = ?;
configurable table<mod1:Lawyer> & readonly lawyerTable2 = ?;

type PersonArray mod1:Person;

configurable PersonArray[] & readonly personArray2 = ?;

// Maps
configurable map<string> & readonly user = ?;
configurable map<int> & readonly numbers = ?;
configurable map<float> & readonly fractions = ?;
configurable map<boolean> & readonly bits = ?;

configurable map<int[]> & readonly numberSet = ?;
configurable map<string[][]> & readonly stringSet = ?;

// Map of records
configurable map<Engineer> & readonly engineerMap = ?;
configurable map<Lecturer> & readonly lecturerMap = ?;
configurable map<Engineer & readonly> & readonly readonlyEngineerMap = ?;
configurable map<Lecturer & readonly> & readonly readonlyLecturerMap = ?;

// Map of map
configurable map<Subject> & readonly subjects = ?;

// Map of table
configurable map<table<Department> key(name)> & readonly departments = ?;

// Array of Maps
configurable map<int>[] & readonly intMapArr = ?;
configurable map<string[]>[] & readonly stringMapArr = ?;
configurable map<Engineer>[] & readonly engineerMapArr = ?; 

// Table of Maps
configurable table<map<string>> & readonly stringMapTable = ?;
configurable table<map<string>& readonly> & readonly readonlyTable = ?;
configurable table<map<string[]>> & readonly stringMapArrTable = ?;
configurable table<map<Engineer>> & readonly engineerMapTable = ?; 
configurable table<map<Engineer & readonly> & readonly> & readonly readonlyEngineerMapTable = ?; 

// Maps from non-default module
configurable mod1:IntMap & readonly intMap = ?;
configurable mod1:StudentMap & readonly studentMap = ?;
configurable map<mod1:Student & readonly> & readonly readonlyStudentMap = ?;
configurable table<mod1:StudentMap> & readonly studentMapTable = ?; 
configurable table<map<mod1:Student & readonly>> & readonly readonlyStudentMapTable = ?;

// Maps from imported module
configurable configLib:IntMap & readonly libIntMap = ?;
configurable configLib:ManagerMap & readonly managerMap = ?;
configurable map<configLib:Manager & readonly> & readonly readonlyManagerMap = ?;
configurable table<configLib:ManagerMap> & readonly managerMapTable = ?; 
configurable table<map<configLib:Manager & readonly>> & readonly readonlyManagerMapTable = ?;

public function main() {
    testRecords();
    testTables();
    testArrays();
    mod2:testRecords();
    mod2:testTables();
    mod2:testArrays();
    testComplexRecords();
    testMaps();
    mod2:testMaps();
    mod3:testRecords();
    print("Tests passed");
}

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
    test:assertEquals(lecturerTable.toString(), "[{\"name\":\"manu\",\"department1\":{\"name\":\"IT\"}," +
    "\"department2\":{\"name\":\"Sales\"},\"department3\":{\"name\":\"HR\"}}," +
    "{\"name\":\"waruna\",\"department1\":{\"name\":\"IT\"},\"department3\":{\"name\":\"HR\"}}]");
    test:assertEquals(lawyerTable.toString(), "[{\"name\":\"gabilan\",\"address1\":{\"city\":\"Colombo\"}," +
    "\"address2\":{\"city\":\"Kandy\"},\"address3\":{\"city\":\"Galle\"}}," +
     "{\"name\":\"riyafa\",\"address1\":{\"city\":\"Matara\"},\"address3\":{\"city\":\"Gampaha\"}}]");
    test:assertEquals(lecturer2.toString(), "{\"name\":\"hinduja\",\"department1\":{\"name\":\"IT\"}," +
    "\"department2\":{\"name\":\"Finance\"},\"department3\":{\"name\":\"HR\"}}");
    test:assertEquals(lawyer2.toString(), "{\"name\":\"riyafa\",\"place1\":{\"city\":\"Colombo\"}," +
    "\"place2\":{\"city\":\"Kandy\"},\"place3\":{\"city\":\"Galle\"}}");
    test:assertEquals(lecturerTable2.toString(), "[{\"name\":\"manu\",\"department1\":{\"name\":\"IT\"}," +
    "\"department2\":{\"name\":\"Sales\"},\"department3\":{\"name\":\"HR\"}}," +
    "{\"name\":\"waruna\",\"department1\":{\"name\":\"IT\"},\"department3\":{\"name\":\"HR\"}}]");
    test:assertEquals(lawyerTable2.toString(), "[{\"name\":\"gabilan\",\"place1\":{\"city\":\"Colombo\"}," +
    "\"place2\":{\"city\":\"Kandy\"},\"place3\":{\"city\":\"Galle\"}},{\"name\":\"riyafa\"," +
    "\"place1\":{\"city\":\"Matara\"},\"place3\":{\"city\":\"Jaffna\"}}]");
}

public function testTables() {
    test:assertEquals(engineerTable.toString(), "[{\"name\":\"hinduja\",\"id\":100},{\"name\":\"riyafa\",\"id\":105}]");
    test:assertEquals(studentTable.toString(), "[{\"name\":\"manu\",\"id\":100},{\"name\":\"riyafa\",\"id\":105}]");
    test:assertEquals(employeeTable.toString(), "[{\"name\":\"hinduja\",\"id\":102},{\"name\":\"manu\",\"id\":100}]");
    test:assertEquals(employeeTable1.toString(), "[{\"name\":\"waruna\",\"id\":2},{\"name\":\"manu\",\"id\":7}]");
    test:assertEquals(officerTable.toString(), "[{\"name\":\"hinduja\",\"id\":102},{\"name\":\"manu\",\"id\":100}]");
    test:assertEquals(officerTable1.toString(), "[{\"name\":\"waruna\",\"id\":4},{\"name\":\"gabilan\",\"id\":5}]");
    test:assertEquals(managerTable.toString(), "[{\"name\":\"gabilan\",\"id\":101},{\"name\":\"riyafa\",\"id\":102}]");
    test:assertEquals(teacherTable.toString(), "[{\"name\":\"manu\",\"id\":77},{\"name\":\"riyafa\",\"id\":88}]");
    test:assertEquals(farmerTable.toString(), "[{\"name\":\"waruna\",\"id\":444},{\"name\":\"hinduja\",\"id\":888}]");
    test:assertEquals(personTable.toString(), "[{\"name\":\"riyafa\",\"id\":13," +
        "\"address\":{\"city\":\"Canberra\",\"country\":{\"name\":\"Australia\"}}},{\"name\":\"gabilan\"," +
        "\"id\":14,\"address\":{\"city\":\"Paris\",\"country\":{\"name\":\"France\"}}}]");
}

public function testArrays() {
    test:assertEquals(personArray.toString(), "[{\"name\":\"manu\",\"id\":11,\"address\":{\"city\":\"New York\"," +
    "\"country\":{\"name\":\"USA\"}}},{\"name\":\"hinduja\",\"id\":12,\"address\":{\"city\":\"London\"," +
    "\"country\":{\"name\":\"UK\"}}}]");
    // test:assertEquals(personArray1.toString(), "[{\"address\":{\"city\":\"Abu Dhabi\",\"country\":" +
    // "{\"name\":\"UAE\"}}"+",\"name\":\"waruna\",\"id\":700},{\"address\":{\"city\":\"Mumbai\"," +
    // "\"country\":{\"name\":\"India\"}},\"name\":\"manu\",\"id\":701}]");
    test:assertEquals(personArray2.toString(), "[{\"name\":\"gabilan\",\"id\":900,\"address\":{\"city\":\"Abu Dhabi\"," +
    "\"country\":{\"name\":\"UAE\"}}},{\"name\":\"hinduja\",\"id\":901,\"address\":{\"city\":\"Mumbai\"," +
    "\"country\":{\"name\":\"India\"}}}]");
}

public function testMaps() {
    test:assertEquals(user.toString(), "{\"name\":\"Hinduja\",\"occupation\":\"Software Engineer\","
    + "\"city\":\"Colombo\"}");
    test:assertEquals(numbers.toString(), "{\"one\":1,\"two\":2,\"three\":3,\"four\":4}");
    test:assertEquals(fractions.toString(), "{\"quarter\":0.25,\"half\":0.5,\"three_quarter\":0.75}");
    test:assertEquals(bits.toString(), "{\"on\":true,\"off\":false}");
    test:assertEquals(numberSet.toString(), "{\"set1\":[1,11,111],\"set2\":[2,22,222],\"set3\":[3,33,333]}");
    test:assertEquals(stringSet.toString(), "{\"set1\":[[\"a\",\"aa\"],[\"b\",\"bb\"]],\"set2\":[[\"c\",\"cc\"],[\"d\","
    + "\"dd\"]],\"set3\":[[\"e\",\"ee\"],[\"f\",\"ff\"]]}");
    test:assertEquals(engineerMap.toString(), "{\"engineer1\":{\"name\":\"Anne\",\"id\":11},"
    + "\"engineer2\":{\"name\":\"Bob\",\"id\":22},\"engineer3\":{\"name\":\"Charles\",\"id\":33}}");
    test:assertEquals(lecturerMap.toString(), "{\"lecturer1\":{\"name\":\"Richard Feynman\",\"department1\":" +
    "{\"name\":\"Physics\"},\"department3\":{\"name\":\"Science\"}}," +
    "\"lecturer2\":{\"name\":\"Michael Sandel\",\"department1\":{\"name\":\"Justice\"}," +
    "\"department2\":{\"name\":\"Ethics\"},\"department3\":{\"name\":\"Law\"}}}");
    test:assertEquals(readonlyEngineerMap.toString(), "{\"engineer1\":{\"name\":\"Anne\",\"id\":11},"
    + "\"engineer2\":{\"name\":\"Bob\",\"id\":22},\"engineer3\":{\"name\":\"Charles\",\"id\":33}}");
    test:assertEquals(readonlyLecturerMap.toString(), "{\"lecturer1\":{\"name\":\"Richard Feynman\",\"department1\":" +
    "{\"name\":\"Physics\"},\"department3\":{\"name\":\"Science\"}}," +
    "\"lecturer2\":{\"name\":\"Michael Sandel\",\"department1\":{\"name\":\"Justice\"}," +
    "\"department2\":{\"name\":\"Ethics\"},\"department3\":{\"name\":\"Law\"}}}");
    test:assertEquals(subjects.toString(), "{\"Maths\":{\"name\":\"Mathematics\",\"grade\":\"grade 8\","
    + "\"instructor\":\"Jane Doe\"},\"Science\":{\"name\":\"Science & Technology\",\"grade\":\"grade 9\","
    + "\"instructor\":\"John Doe\"},\"English\":{\"name\":\"English Language\",\"grade\":\"grade 11\","
    + "\"instructor\":\"Jane Doe\"}}");
    test:assertEquals(departments.toString(), "{\"department1\":[{\"name\":\"Civil Engineering\"}],"
    + "\"department2\":[{\"name\":\"Computer Science\"}],\"department3\":[{\"name\":\"Electronical Engineering\"}]}");
    
    test:assertEquals(intMap.toString(), "{\"ten\":10,\"eleven\":11,\"twelve\":12}");
    test:assertEquals(studentMap.toString(), "{\"student1\":{\"name\":\"Anne\",\"id\":11}," +
    "\"student2\":{\"name\":\"Bob\",\"id\":22},\"student3\":{\"name\":\"Charles\",\"id\":33}}");
    test:assertEquals(readonlyStudentMap.toString(), "{\"student1\":{\"name\":\"Anne\",\"id\":11}," + 
    "\"student2\":{\"name\":\"Bob\",\"id\":22},\"student3\":{\"name\":\"Charles\",\"id\":33}}");

    test:assertEquals(libIntMap.toString(), "{\"fourteen\":14,\"fifteen\":15,\"sixteen\":16}");
    test:assertEquals(managerMap.toString(), "{\"m1\":{\"name\":\"hinduja\",\"id\":111},\"m2\":{\"name\":" +
    "\"waruna\",\"id\":222}}");
    test:assertEquals(readonlyManagerMap.toString(), "{\"m1\":{\"name\":\"manu\",\"id\":333},\"m2\":{\"name\":" +
    "\"riyafa\",\"id\":444}}");

    mod3:testMapIterator(user, 3);
    mod3:testMapIterator(numbers, 4);
    mod3:testMapIterator(fractions, 3);
    mod3:testMapIterator(bits, 2);
    mod3:testMapIterator(numberSet, 3);
    mod3:testMapIterator(stringSet, 3);
    mod3:testMapIterator(engineerMap, 3);
    mod3:testMapIterator(lecturerMap, 2);
    mod3:testMapIterator(readonlyEngineerMap, 3);
    mod3:testMapIterator(readonlyLecturerMap, 2);
    mod3:testMapIterator(subjects, 3);
    mod3:testMapIterator(departments, 3);
    mod3:testMapIterator(intMap, 3);
    mod3:testMapIterator(libIntMap, 3);
    mod3:testMapIterator(managerMap, 2);
    mod3:testMapIterator(readonlyManagerMap, 2);
    // These lines should be enabled after fixing #30566
    // mod3:testMapIterator(studentMap, 3);
    // mod3:testMapIterator(readonlyStudentMap, 3);

    testArrayOfMaps();
    testTableOfMaps();

    
}

public function testArrayOfMaps() {
    test:assertEquals(intMapArr.toString(), "[{\"int1\":34,\"int2\":78},{\"int3\":17,\"int4\":92}]");
    test:assertEquals(stringMapArr.toString(), "[{\"arr1\":[\"hhh\",\"ggg\"],\"arr2\":[\"aaa\",\"bbb\"]}," +
    "{\"arr1\":[\"lll\",\"mmm\"],\"arr2\":[\"nnn\",\"ooo\"]}]");
    test:assertEquals(engineerMapArr.toString(),"[{\"p1\":{\"name\":\"Jack\",\"id\":55}," + 
    "\"p2\":{\"name\":\"Jill\",\"id\":88}},{\"p1\":{\"name\":\"John\",\"id\":66}," +
    "\"p2\":{\"name\":\"Jim\",\"id\":77}}]");
}

public function testTableOfMaps() {
    test:assertEquals(stringMapTable.toString(), "[{\"name\":\"Tom\",\"occupation\":\"Software Engineer\"," +
    "\"city\":\"Colombo\"},{\"name\":\"Harry\",\"occupation\":\"Student\",\"city\":\"Kandy\"}]");
    test:assertEquals(readonlyTable.toString(), "[{\"name\":\"James\",\"occupation\":\"Software Engineer\"," +
    "\"city\":\"Colombo\"},{\"name\":\"Trevor\",\"occupation\":\"Student\",\"city\":\"Kandy\"}]");

    test:assertEquals(stringMapArrTable.toString(),"[{\"names\":[\"Tom\",\"Jerry\"],\"subjects\":[\"Maths\"," +
    "\"Science\",\"Arts\"]},{\"cities\":[\"Colombo\",\"Galle\"],\"fruits\":[\"Apple\",\"Grapes\"]}]");
    test:assertEquals(engineerMapTable.toString(),"[{\"p1\":{\"name\":\"Anna\",\"id\":10}," +
    "\"p2\":{\"name\":\"Elsa\",\"id\":20}},{\"p1\":{\"name\":\"Diana\",\"id\":30}," +
    "\"p2\":{\"name\":\"Belle\",\"id\":40}}]");
    test:assertEquals(readonlyEngineerMapTable.toString(),"[{\"p1\":{\"name\":\"Anna\",\"id\":10}," +
    "\"p2\":{\"name\":\"Elsa\",\"id\":20}},{\"p1\":{\"name\":\"Diana\",\"id\":30}," +
    "\"p2\":{\"name\":\"Belle\",\"id\":40}}]");

    test:assertEquals(studentMapTable.toString(),"[{\"p1\":{\"name\":\"Anna\",\"id\":10}," +
    "\"p2\":{\"name\":\"Elsa\",\"id\":20}},{\"p1\":{\"name\":\"Diana\",\"id\":30}," +
    "\"p2\":{\"name\":\"Belle\",\"id\":40}}]");
    test:assertEquals(readonlyStudentMapTable.toString(),"[{\"p1\":{\"name\":\"Anna\",\"id\":10}," +
    "\"p2\":{\"name\":\"Elsa\",\"id\":20}},{\"p1\":{\"name\":\"Diana\",\"id\":30}," +
    "\"p2\":{\"name\":\"Belle\",\"id\":40}}]");

    test:assertEquals(managerMapTable.toString(), "[{\"m1\":{\"name\":\"Jack\",\"id\":55}," +
    "\"m2\":{\"name\":\"Jill\",\"id\":88}},{\"m1\":{\"name\":\"John\",\"id\":66}," +
    "\"m2\":{\"name\":\"Jim\",\"id\":77}}]");
    test:assertEquals(readonlyManagerMapTable.toString(), "[{\"m1\":{\"name\":\"Jack\",\"id\":55}," +
    "\"m2\":{\"name\":\"Jill\",\"id\":88}},{\"m1\":{\"name\":\"John\",\"id\":66}," +
    "\"m2\":{\"name\":\"Jim\",\"id\":77}}]");

    mod3:testTableIterator(stringMapTable);
    mod3:testTableIterator(readonlyTable);
    mod3:testTableIterator(stringMapArrTable);
    mod3:testTableIterator(engineerMapTable);
    mod3:testTableIterator(readonlyEngineerMapTable);
    mod3:testTableIterator(studentMapTable);
    mod3:testTableIterator(readonlyStudentMapTable);
    mod3:testTableIterator(managerMapTable);
    mod3:testTableIterator(readonlyManagerMapTable);
}

function print(string value) {
    handle strValue = java:fromString(value);
    handle stdout1 = stdout();
    printInternal(stdout1, strValue);
}

public function stdout() returns handle = @java:FieldGet {
    name: "out",
    'class: "java/lang/System"
} external;

public function printInternal(handle receiver, handle strValue) = @java:Method {
    name: "println",
    'class: "java/io/PrintStream",
    paramTypes: ["java.lang.String"]
} external;
