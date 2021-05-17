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
import testOrg/configLib.mod1 as configLib;
import ballerina/test;

public type Doctor record {|
    string name = "";
    int id = 555;
|};

type Department readonly & record {|
    string name;
|};

type Subject map<string>;

configurable Doctor & readonly doctor = ?;
configurable configLib:Manager & readonly manager = ?;
configurable configLib:Teacher & readonly teacher = ?;
configurable configLib:Farmer farmer = ?;
configurable mod1:Student & readonly student = ?;
configurable mod1:Officer officer = ?;
configurable mod1:Employee employee = ?;

configurable table<Doctor> & readonly doctorTable = ?;
configurable table<configLib:Manager> & readonly managerTable = ?;
configurable table<configLib:Teacher> & readonly teacherTable = ?;
configurable table<configLib:Farmer> & readonly farmerTable = ?;
configurable table<mod1:Student> & readonly studentTable = ?;
configurable table<mod1:Officer> & readonly officerTable = ?;
configurable table<mod1:Employee> & readonly employeeTable = ?;

configurable mod1:Employee & readonly employee1 = ?;
configurable table<mod1:Officer & readonly> & readonly officerTable1 = ?;
configurable table<mod1:Employee & readonly> & readonly employeeTable1 = ?;

// Complex records
configurable mod1:Person person = ?;
configurable table<mod1:Person> & readonly personTable = ?;
configurable mod1:Person[] & readonly personArray = ?;
configurable (mod1:Person & readonly)[] & readonly personArray1 = ?;
configurable mod1:PersonArray[] & readonly personArray2 = ?;

// Maps
configurable map<string> & readonly user = ?;
configurable map<int> & readonly numbers = ?;
configurable map<float> & readonly fractions = ?;
configurable map<boolean> & readonly bits = ?;

configurable map<int[]> & readonly numberSet = ?;
configurable map<string[][]> & readonly stringSet = ?;

// Map of records
configurable map<Doctor> & readonly doctorMap = ?;
configurable map<Doctor & readonly> & readonly readonlyDoctorMap = ?;

// Map of map
configurable map<Subject> & readonly subjects = ?;

// Map of table
configurable map<table<Department> key(name)> & readonly departments = ?;

// Array of Maps
configurable map<int>[] & readonly intMapArr = ?;
configurable map<string[]>[] & readonly stringMapArr = ?;
configurable map<Doctor>[] & readonly doctorMapArr = ?; 

// Table of Maps
configurable table<map<string>> & readonly stringMapTable = ?;
configurable table<map<string>& readonly> & readonly readonlyTable = ?;
configurable table<map<string[]>> & readonly stringMapArrTable = ?;
configurable table<map<Doctor>> & readonly doctorMapTable = ?; 
configurable table<map<Doctor & readonly> & readonly> & readonly readonlyDoctorMapTable = ?; 

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

public function testTables() {
    test:assertEquals(doctorTable.toString(), "[{\"name\":\"hinduja\",\"id\":100},{\"name\":\"riyafa\",\"id\":105}]");
    test:assertEquals(studentTable.toString(), "[{\"name\":\"manu\",\"id\":100},{\"name\":\"riyafa\",\"id\":105}]");
    test:assertEquals(employeeTable.toString(), "[{\"name\":\"hinduja\",\"id\":102},{\"name\":\"manu\",\"id\":100}]");
    test:assertEquals(employeeTable1.toString(), "[{\"name\":\"gabilan\",\"id\":2},{\"name\":\"riyafa\",\"id\":3}]");
    test:assertEquals(officerTable.toString(), "[{\"name\":\"hinduja\",\"id\":102},{\"name\":\"manu\",\"id\":100}]");
    test:assertEquals(officerTable1.toString(), "[{\"name\":\"hinduja\",\"id\":7},{\"name\":\"waruna\",\"id\":8}]");
    test:assertEquals(managerTable.toString(), "[{\"name\":\"gabilan\",\"id\":101},{\"name\":\"riyafa\",\"id\":102}]");
    test:assertEquals(teacherTable.toString(), "[{\"name\":\"gabilan\",\"id\":66},{\"name\":\"riyafa\",\"id\":77}]");
    test:assertEquals(farmerTable.toString(), "[{\"name\":\"riyafa\",\"id\":555},{\"name\":\"hinduja\",\"id\":666}]");
    test:assertEquals(personTable.toString(), "[{\"name\":\"gabilan\",\"id\":133," +
        "\"address\":{\"city\":\"Berlin\",\"country\":{\"name\":\"Germany\"}}},{\"name\":\"riyafa\"," +
        "\"id\":144,\"address\":{\"city\":\"Madrid\",\"country\":{\"name\":\"Spain\"}}}]");
}

public function testArrays() {
    test:assertEquals(personArray.toString(), "[{\"address\":{\"city\":\"Abu Dhabi\",\"country\":{\"name\":\"UAE\"}}," +
        "\"name\":\"waruna\",\"id\":111},{\"address\":{\"city\":\"Mumbai\",\"country\":{\"name\":\"India\"}}," +
        "\"name\":\"manu\",\"id\":122}]");
    test:assertEquals(personArray1.toString(), "[{\"address\":{\"city\":\"New York\",\"country\":{\"name\":\"USA\"}}," +
        "\"name\":\"manu\",\"id\":800},{\"address\":{\"city\":\"London\",\"country\":{\"name\":\"UK\"}}," +
        "\"name\":\"hinduja\",\"id\":801}]");
    test:assertEquals(personArray2.toString(), "[{\"address\":{\"city\":\"New York\",\"country\":{\"name\":\"USA\"}}," +
        "\"name\":\"riyafa\",\"id\":1000},{\"address\":{\"city\":\"London\",\"country\":{\"name\":\"UK\"}}," +
        "\"name\":\"waruna\",\"id\":1001}]");
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
    test:assertEquals(doctorMap.toString(), "{\"doctor1\":{\"name\":\"Anne\",\"id\":11},"
    + "\"doctor2\":{\"name\":\"Bob\",\"id\":22},\"doctor3\":{\"name\":\"Charles\",\"id\":33}}");
    test:assertEquals(readonlyDoctorMap.toString(), "{\"doctor1\":{\"name\":\"Anne\",\"id\":11},"
    + "\"doctor2\":{\"name\":\"Bob\",\"id\":22},\"doctor3\":{\"name\":\"Charles\",\"id\":33}}");
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

    testArrayOfMaps();
    testTableOfMaps();
}

function testArrayOfMaps() {
    test:assertEquals(intMapArr.toString(), "[{\"int1\":34,\"int2\":78},{\"int3\":17,\"int4\":92}]");
    test:assertEquals(stringMapArr.toString(), "[{\"arr1\":[\"hhh\",\"ggg\"],\"arr2\":[\"aaa\",\"bbb\"]}," +
    "{\"arr1\":[\"lll\",\"mmm\"],\"arr2\":[\"nnn\",\"ooo\"]}]");
    test:assertEquals(doctorMapArr.toString(),"[{\"p1\":{\"name\":\"Jack\",\"id\":55}," + 
    "\"p2\":{\"name\":\"Jill\",\"id\":88}},{\"p1\":{\"name\":\"John\",\"id\":66}," +
    "\"p2\":{\"name\":\"Jim\",\"id\":77}}]");
}

function testTableOfMaps() {
    test:assertEquals(stringMapTable.toString(), "[{\"name\":\"Tom\",\"occupation\":\"Software Engineer\"," +
    "\"city\":\"Colombo\"},{\"name\":\"Harry\",\"occupation\":\"Student\",\"city\":\"Kandy\"}]");
    test:assertEquals(readonlyTable.toString(), "[{\"name\":\"James\",\"occupation\":\"Software Engineer\"," +
    "\"city\":\"Colombo\"},{\"name\":\"Trevor\",\"occupation\":\"Student\",\"city\":\"Kandy\"}]");
    test:assertEquals(stringMapArrTable.toString(),"[{\"names\":[\"Tom\",\"Jerry\"],\"subjects\":[\"Maths\"," +
    "\"Science\",\"Arts\"]},{\"cities\":[\"Colombo\",\"Galle\"],\"fruits\":[\"Apple\",\"Grapes\"]}]");
    test:assertEquals(doctorMapTable.toString(),"[{\"p1\":{\"name\":\"Anna\",\"id\":10}," +
    "\"p2\":{\"name\":\"Elsa\",\"id\":20}},{\"p1\":{\"name\":\"Diana\",\"id\":30}," +
    "\"p2\":{\"name\":\"Belle\",\"id\":40}}]");
    test:assertEquals(readonlyDoctorMapTable.toString(),"[{\"p1\":{\"name\":\"Anna\",\"id\":10}," +
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
}
