function testStringAsJsonVal () returns (json) {
    json j = "Supun";
    return j;
}

function testIntAsJsonVal () returns (json) {
    json j = 5;
    return j;
}

function testFloatAsJsonVal () returns (json) {
    json j = 7.65;
    return j;
}

function testBooleanAsJsonVal () returns (json) {
    json j = true;
    return j;
}

function testNullAsJsonVal () returns (json) {
    json j = null;
    return j;
}

function testJsonWithNull () returns (json, any) {
    json j = {"name":null};
    return (j, j.name);
}

function testNestedJsonInit () returns (json) {
    json j = {name:"aaa", age:25, parent:{name:"bbb", age:50}, address:{city:"Colombo", "country":"SriLanka"}, array:[1, 5, 7]};
    return j;
}

function testJsonArrayInit () returns (json) {
    json j1 = {name:"supun"};
    json j2 = {name:"thilina"};
    json j3 = {name:"setunga"};
    json j = [j1, j2, j3, 10, "SriLanka", true, null];
    return j;
}

function testGetString () returns (string, string) {
    json j1 = "Supun";
    json j2 = {name:"Setunga"};
    string j1String;
    string j2String;
    j1String =check <string>j1;
    j2String =check <string>j2.name;
    return (j1String, j2String);
}

function testGetInt () returns (int, int) {
    json j1 = 25;
    json j2 = {age:43};
    int j1Int;
    int j2Int;
    j1Int =check <int>j1;
    j2Int =check <int>j2.age;
    return (j1Int, j2Int);
}

function testGetFloat () returns (float) {
    json j = {score:9.73};
    float jFloat;
    jFloat =check <float>j.score;
    return jFloat;
}

function testGetBoolean () returns (boolean) {
    json j = {pass:true};
    boolean jBoolean;
    jBoolean =check <boolean>j.pass;
    return jBoolean;
}

function testGetJson () returns (json) {
    json j = {address:{city:"Colombo", "country":"SriLanka"}};
    return j.address;
}

function testGetNonExistingElement () returns (any) {
    json j2 = {age:43};
    return j2.name;
}

function testAddString () returns (json) {
    json j = {fname:"Supun"};
    j.lname = "Setunga";
    return j;
}

function testAddInt () returns (json) {
    json j = {fname:"Supun"};
    j.age = 25;
    return j;
}

function testAddFloat () returns (json) {
    json j = {fname:"Supun"};
    j.score = 4.37;
    return j;
}

function testAddBoolean ()returns (json) {
    json j = {fname:"Supun"};
    j.status = true;
    return j;
}

function testAddJson ()returns (json) {
    json j = {fname:"Supun"};
    j.address = {country:"SriLanka"};
    return j;
}

function testUpdateString ()returns (json) {
    json j = {fname:"Supun", lname:"Thilina"};
    j.lname = "Setunga";
    return j;
}

function testUpdateInt ()returns (json) {
    json j = {fname:"Supun", age:30};
    j.age = 25;
    return j;
}

function testUpdateFloat () returns(json) {
    json j = {fname:"Supun", score:7.65};
    j.score = 4.37;
    return j;
}

function testUpdateBoolean () returns(json) {
    json j = {fname:"Supun", status:false};
    j.status = true;
    return j;
}

function testUpdateJson () returns(json) {
    json j = {fname:"Supun", address:{country:"USA"}};
    j.address = {country:"SriLanka"};
    return j;
}

function testUpdateStringInArray () returns(json) {
    json j = ["a", "b", "c"];
    j[1] = "d";
    return j;
}

function testUpdateIntInArray () returns (json) {
    json j = ["a", "b", "c"];
    j[1] = 64;
    return j;
}

function testUpdateFloatInArray () returns (json) {
    json j = ["a", "b", "c"];
    j[1] = 4.72;
    return j;
}

function testUpdateBooleanInArray () returns (json) {
    json j = ["a", "b", "c"];
    j[1] = true;
    return j;
}

function testUpdateNullInArray () returns (json) {
    json j = ["a", "b", "c"];
    j[1] = null;
    return j;
}

function testUpdateJsonInArray () returns (json) {
    json j = ["a", "b", "c"];
    j[1] = {country:"SriLanka"};
    return j;
}

function testUpdateJsonArrayInArray () returns (json) {
    json j = ["a", "b", "c"];
    j[1] = [1, 2, 3];
    return j;
}

function testGetNestedJsonElement () returns (string, string, string, string) {
    json j = {name:"aaa", age:25, parent:{name:"bbb", age:50}, address:{city:"Colombo", "country":"SriLanka"}, array:[1, 5, 7]};

    string addressKey = "address";
    string cityKey = "city";
    string cityString1;
    string cityString2;
    string cityString3;
    string cityString4;
    cityString1 =check <string>j.address.city;
    cityString2 =check <string>j["address"]["city"];
    cityString3 =check <string>j.address["city"];
    cityString4 =check <string>j[addressKey][cityKey];
    return (cityString1, cityString2, cityString3, cityString4);
}

function testJsonExprAsIndex () returns (string) {
    json j = {name:"aaa", address:{city:"Colombo", "area":"city"}};

    string addressKey = "address";
    string cityKey = "city";

    //Moving index expression into another line since with new changes, it is a unsafe cast,
    //which returns a error value if any.
    string key;
    key =check <string>j.address.area;
    string value;
    value =check <string>j.address[key];
    return value;
}

function testSetArrayOutofBoundElement () returns (json) {
    json j = [1, 2, 3];
    j[7] = 8;
    return j;
}

function testSetToNonArrayWithIndex () returns (json, json, json) {
    json j1 = {name:"supun"};
    json j2 = "foo";
    json j3 = true;
    j1[7] = 8;
    j2[7] = 8;
    j3[7] = 8;
    return (j1, j2, j3);
}

function testGetFromNonArrayWithIndex () returns (json, json, json) {
    json j1 = {name:"supun"};
    json j2 = "foo";
    json j3 = true;
    return (j1[7], j2[7], j3[7]);
}

function testSetToNonObjectWithKey () returns (json, json, json) {
    json j1 = [1, 2, 3];
    json j2 = "foo";
    json j3 = true;

    j1["name"] = "Supun";
    j2["name"] = "Supun";
    j3["name"] = "Supun";
    return (j1, j2, j3);
}

function testGetFromNonObjectWithKey () returns (json, json, json) {
    json j1 = [1, 2, 3];
    json j2 = "foo";
    json j3 = true;
    return (j1.name, j2.name, j3.name);
}

function testGetStringInArray () returns (string) {
    json j = ["a", "b", "c"];
    string value;
    value =check <string>j[1];
    return value;
}

function testGetArrayOutofBoundElement () returns (string) {
    json j = [1, 2, 3];
    string value;
    value =check <string>j[5];
    return value;
}

function testGetElementFromPrimitive () returns (json) {
    json j = {name:"Supun"};
    return j.name.fname;
}

function testUpdateNestedElement () returns (json) {
    json j = {details:{fname:"Supun", lname:"Thilina"}};
    j.details.lname = "Setunga";
    return j;
}

function testEmptyStringToJson () returns (json) {
    string s = "";
    return <json>s;
}

function testJsonStringToJson () returns (json) {
    string s = "{\"name\", \"supun\"}";
    return <json>s;
}

function testStringWithEscapedCharsToJson () returns (json) {
    string s = "{\\\"name\\\", \"supun\"}";
    return <json>s;
}

function testJsonLength () returns (int, int) {
    json[] j1 = [[1, 2, 3, 4], [3, 4, 5, 6], [7, 8, 9, 10]];

    json j2 = [[1, 2, 3, 4], [3, 4, 5, 6], [7, 8, 9, 10]];

    return (lengthof j1, lengthof j2);
}

function testJsonArrayToJsonCasting () returns (json) {
    json[][] j1 = [[1, 2, 3], [3, 4, 5], [7, 8, 9]];

    json j2 = <json>j1;
    return j2;
}

function testJsonToJsonArrayCasting () returns (json[], json[][]) {
    json j1 = [[1, 2, 3], [3, 4, 5], [7, 8, 9]];

    var j2 =check <json[]>j1;
    var j3 =check <json[][]>j1;

    return (j2, j3);
}

function testJsonToJsonArrayInvalidCasting () returns (json[][][] | error) {
    json j1 = [[1, 2, 3], [3, 4, 5], [7, 8, 9]];

    var j2 = <json[][][]>j1;

    return j2;
}

function testGetFromNull () returns (string) {
    json j2 = {age:43, name:null};
    string value;
    value =check <string>j2.name.fname;
    return value;
}

function testAddToNull () returns (json) {
    json j = {name:"Supun", address:null};
    j.address.country = "SriLanka";
    return j;
}
