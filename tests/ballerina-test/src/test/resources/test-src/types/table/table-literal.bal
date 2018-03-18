import ballerina.io;
import ballerina.data.sql;

struct Person {
    int id;
    int age;
    float salary;
    string name;
    boolean married;
}

struct Company {
    int id;
    string name;
}

struct TypeTest {
    int id;
    json jsonData;
    xml xmlData;
}

struct BlobTypeTest {
    int id;
    blob blobData;
}

struct AnyTypeTest {
    int id;
    any anyData;
}

struct ArraTypeTest {
    int id;
    int[] intArrData;
    float[] floatArrData;
    string[] stringArrData;
    boolean[] booleanArrData;
}

struct ResultCount {
    int COUNTVAL;
}

table<Person> dt1 = {};
table<Company> dt2 = {};

function testEmptyTableCreate () (int count1, int count2) {
    table<Person> dt3 = {};
    table<Person> dt4 = {};
    table<Company> dt5 = {};
    table < Person > dt6;
    table < Company > dt7;
    table dt8;
    count1 = checkTableCount("TABLE_PERSON_%");
    count2 = checkTableCount("TABLE_COMPANY_%");
    return;
}

function checkTableCount(string tablePrefix) (int count) {
    endpoint sql:Client testDB {
        database: sql:DB.H2_MEM,
        host: "",
        port: 0,
        name: "TABLEDB",
        username: "sa",
        password: "",
        options: {maximumPoolSize:1}
    };

    sql:Parameter  p1 = {value:tablePrefix, sqlType:sql:Type.VARCHAR};
    sql:Parameter[] parameters = [p1];

    try {
        table dt = testDB -> select("SELECT count(*) as count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME like ?",
        parameters, typeof ResultCount);
        while (dt.hasNext()) {
            var rs, _ = (ResultCount) dt.getNext();
            count = rs.COUNTVAL;
        }
    } finally {
        testDB -> close();
    }
    return;
}

function testEmptyTableCreateInvalid () {
    table t1 = {};
}

function testAddData () (int count1, int count2, int count3, int[] dt1data, int[] dt2data, int[] ct1data) {
    Person p1 = {id:1, age:30, salary:300.50, name:"jane", married:true};
    Person p2 = {id:2, age:20, salary:200.50, name:"martin", married:true};
    Person p3 = {id:3, age:32, salary:100.50, name:"john", married:false};

    Company c1 = {id:100, name:"ABC"};

    table<Person> dt1 = {};
    table<Person> dt2 = {};
    table<Company> ct1 = {};

    dt1.add(p1);
    dt1.add(p2);

    dt2.add(p3);

    ct1.add(c1);

    count1 = dt1.count();
    dt1data = dt1.map(getPersonId);

    count2 = dt2.count();
    dt2data = dt2.map(getPersonId);

    count3 = ct1.count();
    ct1data = ct1.map(getCompanyId);
    return;
}

function testTableAddInvalid () {
    Company c1 = {id:100, name:"ABC"};
    table<Person> dt1 = {};
    dt1.add(c1);
}

function testMultipleAccess () (int count1, int count2, int[] dtdata1, int[] dtdata2) {
    Person p1 = {id:1, age:30, salary:300.50, name:"jane", married:true};
    Person p2 = {id:2, age:20, salary:200.50, name:"martin", married:true};
    Person p3 = {id:3, age:32, salary:100.50, name:"john", married:false};

    table<Person> dt1 = {};
    dt1.add(p1);
    dt1.add(p2);
    dt1.add(p3);

    count1 = dt1.count();
    dtdata1 = dt1.map(getPersonId);

    count2 = dt1.count();
    dtdata2 = dt1.map(getPersonId);
    return;
}

function testLoopingTable () (string) {
    Person p1 = {id:1, age:30, salary:300.50, name:"jane", married:true};
    Person p2 = {id:2, age:20, salary:200.50, name:"martin", married:true};
    Person p3 = {id:3, age:32, salary:100.50, name:"john", married:false};

    table<Person> dt = {};
    dt.add(p1);
    dt.add(p2);
    dt.add(p3);

    string names = "";

    while (dt.hasNext()) {
        var p, _ = (Person)dt.getNext();
        names = names + p.name + "_";
    }
    return names;
}

function testToJson () (json) {
    Person p1 = {id:1, age:30, salary:300.50, name:"jane", married:true};
    Person p2 = {id:2, age:20, salary:200.50, name:"martin", married:true};
    Person p3 = {id:3, age:32, salary:100.50, name:"john", married:false};

    table<Person> dt = {};
    dt.add(p1);
    dt.add(p2);
    dt.add(p3);

    var j, _ = <json>dt;
    return j;
}

function testToXML () (xml) {
    Person p1 = {id:1, age:30, salary:300.50, name:"jane", married:true};
    Person p2 = {id:2, age:20, salary:200.50, name:"martin", married:true};
    Person p3 = {id:3, age:32, salary:100.50, name:"john", married:false};

    table<Person> dt = {};
    dt.add(p1);
    dt.add(p2);
    dt.add(p3);

    var x, _ = <xml>dt;
    return x;
}

function testPrintData () {
    Person p1 = {id:1, age:30, salary:300.50, name:"jane", married:true};
    Person p2 = {id:2, age:20, salary:200.50, name:"martin", married:true};
    Person p3 = {id:3, age:32, salary:100.50, name:"john", married:false};

    table<Person> dt = {};
    dt.add(p1);
    dt.add(p2);
    dt.add(p3);

    io:println(dt);
}

function testTableDrop () {
    Person p1 = {id:1, age:30, salary:300.50, name:"jane", married:true};

    table<Person> dt = {};
    dt.add(p1);
}

function testTableWithAllDataToJson () (json) {
    json j1 = {name:"apple", color:"red", price:30.3};
    xml x1 = xml `<book>The Lost World</book>`;
    TypeTest t1 = {id:1, jsonData:j1, xmlData:x1};
    TypeTest t2 = {id:2, jsonData:j1, xmlData:x1};

    table<TypeTest> dt1 = {};
    dt1.add(t1);
    dt1.add(t2);

    var j, _ = <json>dt1;
    return j;
}

function testTableWithAllDataToXml () (xml) {
    json j1 = {name:"apple", color:"red", price:30.3};
    xml x1 = xml `<book>The Lost World</book>`;
    TypeTest t1 = {id:1, jsonData:j1, xmlData:x1};
    TypeTest t2 = {id:2, jsonData:j1, xmlData:x1};

    table<TypeTest> dt1 = {};
    dt1.add(t1);
    dt1.add(t2);

    var x, _ = <xml>dt1;
    return x;
}


function testTableWithAllDataToStruct () (json jData, xml xData) {
    json j1 = {name:"apple", color:"red", price:30.3};
    xml x1 = xml `<book>The Lost World</book>`;
    TypeTest t1 = {id:1, jsonData:j1, xmlData:x1};

    table<TypeTest> dt1 = {};
    dt1.add(t1);

    while (dt1.hasNext()) {
        var x, _ = (TypeTest)dt1.getNext();
        jData = x.jsonData;
        xData = x.xmlData;
    }
    return;
}

function testTableWithBlobDataToJson () (json) {
    string text = "Sample Text";
    blob content = text.toBlob("UTF-8");
    BlobTypeTest t1 = {id:1, blobData:content};

    table<BlobTypeTest> dt1 = {};
    dt1.add(t1);

    var j, _ = <json>dt1;
    return j;
}

function testTableWithBlobDataToXml () (xml) {
    string text = "Sample Text";
    blob content = text.toBlob("UTF-8");
    BlobTypeTest t1 = {id:1, blobData:content};

    table<BlobTypeTest> dt1 = {};
    dt1.add(t1);

    var x, _ = <xml>dt1;
    return x;
}

function testTableWithBlobDataToStruct () (blob bData) {
    string text = "Sample Text";
    blob content = text.toBlob("UTF-8");
    BlobTypeTest t1 = {id:1, blobData:content};

    table<BlobTypeTest> dt1 = {};
    dt1.add(t1);

    while (dt1.hasNext()) {
        var x, _ = (BlobTypeTest)dt1.getNext();
        bData = x.blobData;
    }
    return;
}

function testTableWithAnyDataToJson () (json) {
    AnyTypeTest t1 = {id:1, anyData:"Sample Text"};

    table<AnyTypeTest> dt1 = {};
    dt1.add(t1);

    var j, _ = <json>dt1;
    return j;
}

function testStructWithDefaultDataToJson () (json) {
    Person p1 = {id:1};
    table<Person> dt1 = {};
    dt1.add(p1);

    var j, _ = <json>dt1;
    return j;
}

function testStructWithDefaultDataToXml () (xml) {
    Person p1 = {id:1};
    table<Person> dt1 = {};
    dt1.add(p1);

    var x, _ = <xml>dt1;
    return x;
}


function testStructWithDefaultDataToStruct () (int iData, float fData, string sData, boolean bData) {
    Person p1 = {id:1};
    table<Person> dt1 = {};
    dt1.add(p1);

    while (dt1.hasNext()) {
        var x, _ = (Person)dt1.getNext();
        iData = x.age;
        fData = x.salary;
        sData = x.name;
        bData = x.married;
    }
    return;
}

function testTableWithArrayDataToJson () (json) {
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

    table<ArraTypeTest> dt1 = {};
    dt1.add(t1);
    dt1.add(t2);

    var j, _ = <json>dt1;
    return j;
}

function testTableWithArrayDataToXml () (xml) {
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

    table<ArraTypeTest> dt1 = {};
    dt1.add(t1);
    dt1.add(t2);

    var x, _ = <xml>dt1;
    return x;
}

function testTableWithArrayDataToStruct () (int[] intArr, float[] floatArr, string[] stringArr, boolean[] boolArr) {
    int[] intArray = [1, 2, 3];
    float[] floatArray = [11.1, 22.2, 33.3];
    string[] stringArray = ["Hello", "World"];
    boolean[] boolArray = [true, false, true];
    ArraTypeTest t1 = {id:1, intArrData:intArray, floatArrData:floatArray, stringArrData:stringArray,
                          booleanArrData:boolArray};

    table<ArraTypeTest> dt1 = {};
    dt1.add(t1);

    while (dt1.hasNext()) {
        var x, _ = (ArraTypeTest)dt1.getNext();
        intArr = x.intArrData;
        floatArr = x.floatArrData;
        stringArr = x.stringArrData;
        boolArr = x.booleanArrData;
    }
    return;
}

function testTableRemoveSuccess () (int count, json j) {
    Person p1 = {id:1, age:35, salary:300.50, name:"jane", married:true};
    Person p2 = {id:2, age:20, salary:200.50, name:"martin", married:true};
    Person p3 = {id:3, age:32, salary:100.50, name:"john", married:false};

    table<Person> dt = {};
    dt.add(p1);
    dt.add(p2);
    dt.add(p3);

    count = dt.remove(isBellow35);
    j, _ = <json>dt;
    return;
}

function testTableRemoveSuccessMultipleMatch () (int count, json j) {
    Person p1 = {id:1, age:35, salary:300.50, name:"john", married:true};
    Person p2 = {id:2, age:20, salary:200.50, name:"martin", married:true};
    Person p3 = {id:3, age:32, salary:100.50, name:"john", married:false};

    table<Person> dt = {};
    dt.add(p1);
    dt.add(p2);
    dt.add(p3);

    count = dt.remove(isJohn);
    j, _ = <json>dt;
    return;
}

function testTableRemoveFailed () (int count, json j) {
    Person p1 = {id:1, age:35, salary:300.50, name:"jane", married:true};
    Person p2 = {id:2, age:40, salary:200.50, name:"martin", married:true};
    Person p3 = {id:3, age:42, salary:100.50, name:"john", married:false};

    table<Person> dt = {};
    dt.add(p1);
    dt.add(p2);
    dt.add(p3);

    count = dt.remove(isBellow35);
    j, _ = <json>dt;
    return;
}

function testTableAddAndAccess () (string s1, string s2) {
    Person p1 = {id:1, age:35, salary:300.50, name:"jane", married:true};
    Person p2 = {id:2, age:40, salary:200.50, name:"martin", married:true};
    Person p3 = {id:3, age:42, salary:100.50, name:"john", married:false};

    table<Person> dt = {};
    dt.add(p1);
    dt.add(p2);

    var j1, _ = <json>dt;
    s1 = j1.toString();

    dt.add(p3);
    var j2, _ = <json>dt;
    s2 = j2.toString();
    return;
}

function getPersonId (Person p) (int) {
    return p.id;
}

function getCompanyId (Company p) (int) {
    return p.id;
}

function isBellow35 (Person p) (boolean) {
    return p.age < 35;
}

function isJohn (Person p) (boolean) {
    return p.name == "john";
}
