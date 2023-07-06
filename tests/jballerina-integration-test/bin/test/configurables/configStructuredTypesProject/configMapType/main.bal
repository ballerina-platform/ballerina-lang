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

import configMapType.type_defs;
import testOrg/configLib.mod1 as configLib;
import testOrg/configLib.util;
import configMapType.imported_maps;
import ballerina/test;

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
configurable table<map<string> & readonly> & readonly readonlyTable = ?;
configurable table<map<string[]>> & readonly stringMapArrTable = ?;
configurable table<map<Engineer>> & readonly engineerMapTable = ?;
configurable table<map<Engineer & readonly> & readonly> & readonly readonlyEngineerMapTable = ?;

// Maps from non-default module
configurable type_defs:IntMap & readonly intMap = ?;
configurable type_defs:StudentMap & readonly studentMap = ?;
configurable map<type_defs:Student & readonly> & readonly readonlyStudentMap = ?;
configurable table<type_defs:StudentMap> & readonly studentMapTable = ?;
configurable table<map<type_defs:Student & readonly>> & readonly readonlyStudentMapTable = ?;

// Maps from imported module
configurable configLib:IntMap & readonly libIntMap = ?;
configurable configLib:ManagerMap & readonly managerMap = ?;
configurable map<configLib:Manager & readonly> & readonly readonlyManagerMap = ?;
configurable table<configLib:ManagerMap> & readonly managerMapTable = ?;
configurable table<map<configLib:Manager & readonly>> & readonly readonlyManagerMapTable = ?;

function testMaps() {
    testSimpleMaps();
    testMapIteration();
    testArrayOfMaps();
    testArrayIteration();
    testTableOfMaps();
    testTableIteration();
}

public function testArrayOfMaps() {
    test:assertEquals(intMapArr.toString(), "[{\"int1\":34,\"int2\":78},{\"int3\":17,\"int4\":92}]");
    test:assertEquals(stringMapArr.toString(), "[{\"arr1\":[\"hhh\",\"ggg\"],\"arr2\":[\"aaa\",\"bbb\"]}," + 
    "{\"arr1\":[\"lll\",\"mmm\"],\"arr2\":[\"nnn\",\"ooo\"]}]");
    test:assertEquals(engineerMapArr.toString(), "[{\"p1\":{\"name\":\"Jack\",\"id\":55}," + 
    "\"p2\":{\"name\":\"Jill\",\"id\":88}},{\"p1\":{\"name\":\"John\",\"id\":66}," + 
    "\"p2\":{\"name\":\"Jim\",\"id\":77}}]");
}

function testArrayIteration() {
    util:testArrayIterator(intMapArr, 2);
    util:testArrayIterator(stringMapArr, 2);
    util:testArrayIterator(engineerMapArr, 2);    
}

public function testTableOfMaps() {
    test:assertEquals(stringMapTable.toString(), "[{\"name\":\"Tom\",\"occupation\":\"Software Engineer\"," + 
    "\"city\":\"Colombo\"},{\"name\":\"Harry\",\"occupation\":\"Student\",\"city\":\"Kandy\"}]");
    test:assertEquals(readonlyTable.toString(), "[{\"name\":\"James\",\"occupation\":\"Software Engineer\"," + 
    "\"city\":\"Colombo\"},{\"name\":\"Trevor\",\"occupation\":\"Student\",\"city\":\"Kandy\"}]");

    test:assertEquals(stringMapArrTable.toString(), "[{\"names\":[\"Tom\",\"Jerry\"],\"subjects\":[\"Maths\"," + 
    "\"Science\",\"Arts\"]},{\"cities\":[\"Colombo\",\"Galle\"],\"fruits\":[\"Apple\",\"Grapes\"]}]");
    test:assertEquals(engineerMapTable.toString(), "[{\"p1\":{\"name\":\"Anna\",\"id\":10}," + 
    "\"p2\":{\"name\":\"Elsa\",\"id\":20}},{\"p1\":{\"name\":\"Diana\",\"id\":30}," + 
    "\"p2\":{\"name\":\"Belle\",\"id\":40}}]");
    test:assertEquals(readonlyEngineerMapTable.toString(), "[{\"p1\":{\"name\":\"Anna\",\"id\":10}," + 
    "\"p2\":{\"name\":\"Elsa\",\"id\":20}},{\"p1\":{\"name\":\"Diana\",\"id\":30}," + 
    "\"p2\":{\"name\":\"Belle\",\"id\":40}}]");

    test:assertEquals(studentMapTable.toString(), "[{\"p1\":{\"name\":\"Anna\",\"id\":10}," + 
    "\"p2\":{\"name\":\"Elsa\",\"id\":20}},{\"p1\":{\"name\":\"Diana\",\"id\":30}," + 
    "\"p2\":{\"name\":\"Belle\",\"id\":40}}]");
    test:assertEquals(readonlyStudentMapTable.toString(), "[{\"p1\":{\"name\":\"Anna\",\"id\":10}," + 
    "\"p2\":{\"name\":\"Elsa\",\"id\":20}},{\"p1\":{\"name\":\"Diana\",\"id\":30}," + 
    "\"p2\":{\"name\":\"Belle\",\"id\":40}}]");

    test:assertEquals(managerMapTable.toString(), "[{\"m1\":{\"name\":\"Jack\",\"id\":55}," + 
    "\"m2\":{\"name\":\"Jill\",\"id\":88}},{\"m1\":{\"name\":\"John\",\"id\":66}," + 
    "\"m2\":{\"name\":\"Jim\",\"id\":77}}]");
    test:assertEquals(readonlyManagerMapTable.toString(), "[{\"m1\":{\"name\":\"Jack\",\"id\":55}," + 
    "\"m2\":{\"name\":\"Jill\",\"id\":88}},{\"m1\":{\"name\":\"John\",\"id\":66}," + 
    "\"m2\":{\"name\":\"Jim\",\"id\":77}}]");
}

function testSimpleMaps() {
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
}

function testMapIteration() {
    util:testMapIterator(user, 3);
    util:testMapIterator(numbers, 4);
    util:testMapIterator(fractions, 3);
    util:testMapIterator(bits, 2);
    util:testMapIterator(numberSet, 3);
    util:testMapIterator(stringSet, 3);
    util:testMapIterator(engineerMap, 3);
    util:testMapIterator(lecturerMap, 2);
    util:testMapIterator(readonlyEngineerMap, 3);
    util:testMapIterator(readonlyLecturerMap, 2);
    util:testMapIterator(subjects, 3);
    util:testMapIterator(departments, 3);
    util:testMapIterator(intMap, 3);
    util:testMapIterator(libIntMap, 3);
    util:testMapIterator(managerMap, 2);
    util:testMapIterator(readonlyManagerMap, 2);
    util:testMapIterator(studentMap, 3);
    util:testMapIterator(readonlyStudentMap, 3);
}

function testTableIteration() {
    util:testTableIterator(stringMapTable);
    util:testTableIterator(readonlyTable);
    util:testTableIterator(stringMapArrTable);
    util:testTableIterator(engineerMapTable);
    util:testTableIterator(readonlyEngineerMapTable);
    util:testTableIterator(studentMapTable);
    util:testTableIterator(readonlyStudentMapTable);
    util:testTableIterator(managerMapTable);
    util:testTableIterator(readonlyManagerMapTable);
}

public function main() {
    testMaps();
    imported_maps:testMaps();
    util:print("Tests passed");
}
