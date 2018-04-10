import ballerina/io;
import ballerina/sql;

type Person {
    int id,
    int age,
    float salary,
    string name,
    boolean married,
};

type Company {
    int id,
    string name,
};

type TypeTest {
    int id,
    json jsonData,
    xml xmlData,
};

type BlobTypeTest {
    int id,
    blob blobData,
};

type AnyTypeTest {
    int id,
    any anyData,
};

type ArraTypeTest {
    int id,
    int[] intArrData,
    float[] floatArrData,
    string[] stringArrData,
    boolean[] booleanArrData,
};

type ResultCount {
    int COUNTVAL,
};

table<Person> dt1 = table{};
table<Company> dt2 = table{};

function testEmptyTableCreate () returns (int, int) {
    table<Person> dt3 = table{};
    table<Person> dt4 = table{};
    table<Company> dt5 = table{};
    table < Person > dt6;
    table < Company > dt7;
    table dt8;
    int count1 = checkTableCount("TABLE_PERSON_%");
    int count2 = checkTableCount("TABLE_COMPANY_%");
    return (count1, count2);
}

function checkTableCount(string tablePrefix) returns (int) {
    endpoint sql:Client testDB {
        database: sql:DB_H2_MEM,
        host: "",
        port: 0,
        name: "TABLEDB",
        username: "sa",
        password: "",
        options: {maximumPoolSize:1}
    };

    sql:Parameter  p1 = {value:tablePrefix, sqlType:sql:TYPE_VARCHAR};
    sql:Parameter[] parameters = [p1];

    int count;
    try {
        var temp = testDB -> select("SELECT count(*) as count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME like
         ?",
        parameters, typeof ResultCount);
        table dt = check temp;
        while (dt.hasNext()) {
            var rs = check <ResultCount> dt.getNext();
            count = rs.COUNTVAL;
        }
    } finally {
        _ = testDB -> close();
    }
    return count;
}

function testEmptyTableCreateInvalid () {
    table t1 = table{};
}

function testAddData () returns (int, int, int, int[], int[], int[]) {
    Person p1 = {id:1, age:30, salary:300.50, name:"jane", married:true};
    Person p2 = {id:2, age:20, salary:200.50, name:"martin", married:true};
    Person p3 = {id:3, age:32, salary:100.50, name:"john", married:false};

    Company c1 = {id:100, name:"ABC"};

    table<Person> dt1 = table{};
    table<Person> dt2 = table{};
    table<Company> ct1 = table{};

    _ = dt1.add(p1);
    _ = dt1.add(p2);

    _ = dt2.add(p3);

    _ = ct1.add(c1);

    int count1 = dt1.count();
    int[] dt1data;
    int i = 0;
    while (dt1.hasNext()) {
        var p = check <Person> dt1.getNext();
        dt1data[i] = p.id;
        i = i + 1;
    }

    int count2 = dt2.count();
    int[] dt2data;
    i = 0;
    while (dt2.hasNext()) {
        var p = check <Person> dt2.getNext();
        dt2data[i] = p.id;
        i = i + 1;
    }

    int count3 = ct1.count();
    int[] ct1data;
    i = 0;
    while (ct1.hasNext()) {
        var p = check <Company> ct1.getNext();
        ct1data[i] = p.id;
        i = i + 1;
    }
    return (count1, count2, count3, dt1data, dt2data, ct1data);
}

function testTableAddInvalid () {
    Company c1 = {id:100, name:"ABC"};

    table<Person> dt1 = table{};
    _ = dt1.add(c1);
}

function testMultipleAccess () returns (int, int, int[], int[]) {
    Person p1 = {id:1, age:30, salary:300.50, name:"jane", married:true};
    Person p2 = {id:2, age:20, salary:200.50, name:"martin", married:true};
    Person p3 = {id:3, age:32, salary:100.50, name:"john", married:false};

    table<Person> dt1 = table{};
    _ = dt1.add(p1);
    _ = dt1.add(p2);
    _ = dt1.add(p3);

    int count1 = dt1.count();
    int[] dtdata1;
    int i = 0;
    while (dt1.hasNext()) {
        var p = check <Person> dt1.getNext();
        dtdata1[i] = p.id;
        i = i + 1;
    }

    int count2 = dt1.count();
    int[] dtdata2;
    i = 0;
    while (dt1.hasNext()) {
        var p = check <Person> dt1.getNext();
        dtdata2[i] = p.id;
        i = i + 1;
    }
    return (count1, count2, dtdata1, dtdata2);
}

function testLoopingTable () returns (string) {
    Person p1 = {id:1, age:30, salary:300.50, name:"jane", married:true};
    Person p2 = {id:2, age:20, salary:200.50, name:"martin", married:true};
    Person p3 = {id:3, age:32, salary:100.50, name:"john", married:false};

    table<Person> dt = table{};
    _ = dt.add(p1);
    _ = dt.add(p2);
    _ = dt.add(p3);

    string names = "";

    while (dt.hasNext()) {
        var p = check <Person>dt.getNext();
        names = names + p.name + "_";
    }
    return names;
}

function testToJson () returns (json) {
    Person p1 = {id:1, age:30, salary:300.50, name:"jane", married:true};
    Person p2 = {id:2, age:20, salary:200.50, name:"martin", married:true};
    Person p3 = {id:3, age:32, salary:100.50, name:"john", married:false};

    table<Person> dt = table{};
    _ = dt.add(p1);
    _ = dt.add(p2);
    _ = dt.add(p3);

    var j = check <json>dt;
    return j;
}

function testToXML () returns (xml) {
    Person p1 = {id:1, age:30, salary:300.50, name:"jane", married:true};
    Person p2 = {id:2, age:20, salary:200.50, name:"martin", married:true};
    Person p3 = {id:3, age:32, salary:100.50, name:"john", married:false};

    table<Person> dt = table{};
    _ = dt.add(p1);
    _ = dt.add(p2);
    _ = dt.add(p3);

    var x = check <xml>dt;
    return x;
}

function testPrintData () {
    Person p1 = {id:1, age:30, salary:300.50, name:"jane", married:true};
    Person p2 = {id:2, age:20, salary:200.50, name:"martin", married:true};
    Person p3 = {id:3, age:32, salary:100.50, name:"john", married:false};

    table<Person> dt = table{};
    _ = dt.add(p1);
    _ = dt.add(p2);
    _ = dt.add(p3);

    io:println(dt);
}

function testTableDrop () {
    Person p1 = {id:1, age:30, salary:300.50, name:"jane", married:true};

    table<Person> dt = table{};
    _ = dt.add(p1);
}

function testTableWithAllDataToJson () returns (json) {
    json j1 = {name:"apple", color:"red", price:30.3};
    xml x1 = xml `<book>The Lost World</book>`;
    TypeTest t1 = {id:1, jsonData:j1, xmlData:x1};
    TypeTest t2 = {id:2, jsonData:j1, xmlData:x1};

    table<TypeTest> dt1 = table{};
    _ = dt1.add(t1);
    _ = dt1.add(t2);

    var j = check <json>dt1;
    return j;
}

function testTableWithAllDataToXml () returns (xml) {
    json j1 = {name:"apple", color:"red", price:30.3};
    xml x1 = xml `<book>The Lost World</book>`;
    TypeTest t1 = {id:1, jsonData:j1, xmlData:x1};
    TypeTest t2 = {id:2, jsonData:j1, xmlData:x1};

    table<TypeTest> dt1 = table{};
    _ = dt1.add(t1);
    _ = dt1.add(t2);

    var x = check <xml>dt1;
    return x;
}


function testTableWithAllDataToStruct () returns (json, xml) {
    json j1 = {name:"apple", color:"red", price:30.3};
    xml x1 = xml `<book>The Lost World</book>`;
    TypeTest t1 = {id:1, jsonData:j1, xmlData:x1};

    table<TypeTest> dt1 = table{};
    _ = dt1.add(t1);

    json jData;
    xml xData;
    while (dt1.hasNext()) {
        var x = check <TypeTest>dt1.getNext();
        jData = x.jsonData;
        xData = x.xmlData;
    }
    return (jData, xData);
}

function testTableWithBlobDataToJson () returns (json) {
    string text = "Sample Text";
    blob content = text.toBlob("UTF-8");
    BlobTypeTest t1 = {id:1, blobData:content};

    table<BlobTypeTest> dt1 = table{};
    _ = dt1.add(t1);

    var j = check <json>dt1;
    return j;
}

function testTableWithBlobDataToXml () returns (xml) {
    string text = "Sample Text";
    blob content = text.toBlob("UTF-8");
    BlobTypeTest t1 = {id:1, blobData:content};

    table<BlobTypeTest> dt1 = table{};
    _ = dt1.add(t1);

    var x = check <xml>dt1;
    return x;
}

function testTableWithBlobDataToStruct () returns (blob) {
    string text = "Sample Text";
    blob content = text.toBlob("UTF-8");
    BlobTypeTest t1 = {id:1, blobData:content};

    table<BlobTypeTest> dt1 = table{};
    _ = dt1.add(t1);

    blob bData;
    while (dt1.hasNext()) {
        var x = check <BlobTypeTest>dt1.getNext();
        bData = x.blobData;
    }
    return bData;
}

function testTableWithAnyDataToJson () returns (json) {
    AnyTypeTest t1 = {id:1, anyData:"Sample Text"};

    table<AnyTypeTest> dt1 = table{};
    _ = dt1.add(t1);

    var j = check <json>dt1;
    return j;
}

function testStructWithDefaultDataToJson () returns (json) {
    Person p1 = {id:1};

    table<Person> dt1 = table{};
    _ = dt1.add(p1);

    var j = check <json>dt1;
    return j;
}

function testStructWithDefaultDataToXml () returns (xml) {
    Person p1 = {id:1};

    table<Person> dt1 = table{};
    _ = dt1.add(p1);

    var x = check <xml>dt1;
    return x;
}


function testStructWithDefaultDataToStruct () returns (int, float, string, boolean) {
    Person p1 = {id:1};

    table<Person> dt1 = table{};
    _ = dt1.add(p1);

    int iData;
    float fData;
    string sData;
    boolean bData;

    while (dt1.hasNext()) {
        var x = check <Person>dt1.getNext();
        iData = x.age;
        fData = x.salary;
        sData = x.name;
        bData = x.married;
    }
    return (iData, fData, sData, bData);
}

function testTableWithArrayDataToJson () returns (json) {
    int[] intArray = [1, 2, 3];
    float[] floatArray = [11.1, 22.2, 33.3];
    string[] stringArray = ["Hello", "World"];
    boolean[] boolArray = [true, false, true];
    ArraTypeTest t1 = {id:1, intArrData:intArray, floatArrData:floatArray, stringArrData:stringArray,
                          booleanArrData:boolArray};

    int[] intArray2 = [10, 20, 30];
    float[] floatArray2 = [111.1, 222.2, 333.3];
    string[] stringArray2 = ["Hello", "World", "test"];
    boolean[] boolArray2 = [false, false, true];
    ArraTypeTest t2 = {id:2, intArrData:intArray2, floatArrData:floatArray2, stringArrData:stringArray2,
                          booleanArrData:boolArray2};

    table<ArraTypeTest> dt1 = table{};
    _ = dt1.add(t1);
    _ = dt1.add(t2);

    var j = check <json>dt1;
    return j;
}

function testTableWithArrayDataToXml () returns (xml) {
    int[] intArray = [1, 2, 3];
    float[] floatArray = [11.1, 22.2, 33.3];
    string[] stringArray = ["Hello", "World"];
    boolean[] boolArray = [true, false, true];
    ArraTypeTest t1 = {id:1, intArrData:intArray, floatArrData:floatArray, stringArrData:stringArray,
                          booleanArrData:boolArray};

    int[] intArray2 = [10, 20, 30];
    float[] floatArray2 = [111.1, 222.2, 333.3];
    string[] stringArray2 = ["Hello", "World", "test"];
    boolean[] boolArray2 = [false, false, true];
    ArraTypeTest t2 = {id:2, intArrData:intArray2, floatArrData:floatArray2, stringArrData:stringArray2,
                          booleanArrData:boolArray2};

    table<ArraTypeTest> dt1 = table{};
    _ = dt1.add(t1);
    _ = dt1.add(t2);

    var x = check <xml>dt1;
    return x;
}

function testTableWithArrayDataToStruct () returns (int[], float[], string[], boolean[]) {
    int[] intArray = [1, 2, 3];
    float[] floatArray = [11.1, 22.2, 33.3];
    string[] stringArray = ["Hello", "World"];
    boolean[] boolArray = [true, false, true];
    ArraTypeTest t1 = {id:1, intArrData:intArray, floatArrData:floatArray, stringArrData:stringArray,
                          booleanArrData:boolArray};

    table<ArraTypeTest> dt1 = table{};
    _ = dt1.add(t1);

    int[] intArr;
    float[] floatArr;
    string[] stringArr;
    boolean[] boolArr;

    while (dt1.hasNext()) {
        var x = check <ArraTypeTest>dt1.getNext();
        intArr = x.intArrData;
        floatArr = x.floatArrData;
        stringArr = x.stringArrData;
        boolArr = x.booleanArrData;
    }
    return (intArr, floatArr, stringArr, boolArr);
}

function testTableRemoveSuccess () returns (int, json) {
    Person p1 = {id:1, age:35, salary:300.50, name:"jane", married:true};
    Person p2 = {id:2, age:20, salary:200.50, name:"martin", married:true};
    Person p3 = {id:3, age:32, salary:100.50, name:"john", married:false};

    table<Person> dt = table{};
    _ = dt.add(p1);
    _ = dt.add(p2);
    _ = dt.add(p3);

    int count = check dt.remove(isBellow35);
    var j = check <json>dt;

    return(count, j);
}

function testTableRemoveSuccessMultipleMatch () returns (int, json) {
    Person p1 = {id:1, age:35, salary:300.50, name:"john", married:true};
    Person p2 = {id:2, age:20, salary:200.50, name:"martin", married:true};
    Person p3 = {id:3, age:32, salary:100.50, name:"john", married:false};

    table<Person> dt = table{};
    _ = dt.add(p1);
    _ = dt.add(p2);
    _ = dt.add(p3);

    int count = check dt.remove(isJohn);
    var j = check <json>dt;

    return (count, j);
}

function testTableRemoveFailed () returns (int, json) {
    Person p1 = {id:1, age:35, salary:300.50, name:"jane", married:true};
    Person p2 = {id:2, age:40, salary:200.50, name:"martin", married:true};
    Person p3 = {id:3, age:42, salary:100.50, name:"john", married:false};


    table<Person> dt = table{};
    _ = dt.add(p1);
    _ = dt.add(p2);
    _ = dt.add(p3);

    int count = check dt.remove(isBellow35);
    var j = check <json>dt;

    return (count, j);
}

function testTableAddAndAccess () returns (string, string) {
    Person p1 = {id:1, age:35, salary:300.50, name:"jane", married:true};
    Person p2 = {id:2, age:40, salary:200.50, name:"martin", married:true};
    Person p3 = {id:3, age:42, salary:100.50, name:"john", married:false};


    table<Person> dt = table{};
    _ = dt.add(p1);
    _ = dt.add(p2);

    var j1 = check <json>dt;
    string s1 = j1.toString() but { () => "" };

    _ = dt.add(p3);
    var j2 = check <json>dt;
    string s2 = j2.toString() but { () => "" };

    return (s1, s2);
}

function getPersonId (Person p) returns (int) {
    return p.id;
}

function getCompanyId (Company p) returns (int) {
    return p.id;
}

function isBellow35 (Person p) returns (boolean) {
    return p.age < 35;
}

function isJohn (Person p) returns (boolean) {
    return p.name == "john";
}
