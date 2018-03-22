import ballerina/lang.errors;

function testStringAsJsonVal() (json){
    json j = "Supun";
    return j;
}

function testIntAsJsonVal() (json){
    json j = 5;
    return j;
}

function testFloatAsJsonVal() (json){
    json j = 7.65;
    return j;
}

function testBooleanAsJsonVal() (json){
    json j = true;
    return j;
}

function testNullAsJsonVal() (json){
    json j = null;
    return j;
}

function testJsonWithNull() (json, any){
    json j = {"name":null};
    return j, j.name;
}

function testNestedJsonInit() (json) {
    json j = {name:"aaa", age:25, parent:{name:"bbb", age:50}, address:{city:"Colombo", "country":"SriLanka"}, array:[1,5,7]};
    return j;
}

function testJsonArrayInit() (json) {
    json j1 = {name:"supun"};
    json j2 = {name:"thilina"};
    json j3 = {name:"setunga"};
    json j = [j1, j2, j3, 10, "SriLanka", true, null];
    return j;
}

function testGetString() (string, string) {
    json j1 = "Supun";
    json j2 = {name:"Setunga"};
    string j1String;
    string j2String;
    j1String, _ = (string)j1;
    j2String, _ = (string)j2.name;
    return j1String, j2String;
}

function testGetInt() (int, int) {
    json j1 = 25;
    json j2 = {age:43};
    int j1Int;
    int j2Int;
    j1Int, _ = (int)j1;
    j2Int, _ = (int)j2.age;
    return j1Int, j2Int;
}

function testGetFloat() (float) {
    json j = {score:9.73};
    float jFloat;
    jFloat, _ = (float)j.score;
    return jFloat;
}

function testGetBoolean() (boolean) {
    json j = {pass:true};
    boolean jBoolean;
    jBoolean, _ = (boolean)j.pass;
    return jBoolean;
}

function testGetJson() (json) {
    json j = {address:{city:"Colombo", "country":"SriLanka"}};
    return j.address;
}

function testGetNonExistingElement() (any) {
    json j2 = { age:43};
    return j2.name;
}

function testAddString() (json) {
    json j = {fname:"Supun"};
    j.lname = "Setunga";
    return j;
}

function testAddInt() (json) {
    json j = {fname:"Supun"};
    j.age = 25;
    return j;
}

function testAddFloat() (json) {
    json j = {fname:"Supun"};
    j.score = 4.37;
    return j;
}

function testAddBoolean() (json) {
    json j = {fname:"Supun"};
    j.status = true;
    return j;
}

function testAddJson() (json) {
    json j = {fname:"Supun"};
    j.address = {country:"SriLanka"};
    return j;
}

function testUpdateString() (json) {
    json j = {fname:"Supun", lname:"Thilina"};
    j.lname = "Setunga";
    return j;
}

function testUpdateInt() (json) {
    json j = {fname:"Supun", age:30};
    j.age = 25;
    return j;
}

function testUpdateFloat() (json) {
    json j = {fname:"Supun", score:7.65};
    j.score = 4.37;
    return j;
}

function testUpdateBoolean() (json) {
    json j = {fname:"Supun", status: false};
    j.status = true;
    return j;
}

function testUpdateJson() (json) {
    json j = {fname:"Supun", address:{country:"USA"}};
    j.address = {country:"SriLanka"};
    return j;
}

function testUpdateStringInArray() (json) {
    json j = ["a", "b", "c"];
    j[1] = "d";
    return j;
}

function testUpdateIntInArray() (json) {
    json j = ["a", "b", "c"];
    j[1] = 64;
    return j;
}

function testUpdateFloatInArray() (json) {
    json j = ["a", "b", "c"];
    j[1] = 4.72;
    return j;
}

function testUpdateBooleanInArray() (json) {
    json j = ["a", "b", "c"];
    j[1] = true;
    return j;
}

function testUpdateNullInArray() (json) {
    json j = ["a", "b", "c"];
    j[1] = null;
    return j;
}

function testUpdateJsonInArray() (json) {
    json j = ["a", "b", "c"];
    j[1] = {country:"SriLanka"};
    return j;
}

function testUpdateJsonArrayInArray() (json) {
    json j = ["a", "b", "c"];
    j[1] = [1,2,3];
    return j;
}

function testGetNestedJsonElement() (string, string, string, string) {
    json j = {name:"aaa", age:25, parent:{name:"bbb", age:50}, address:{city:"Colombo", "country":"SriLanka"}, array:[1,5,7]};
    
    string addressKey = "address";
    string cityKey = "city";
    string cityString1;
    string cityString2;
    string cityString3;
    string cityString4;
    cityString1, _ = (string)j.address.city;
    cityString2, _ = (string)j["address"]["city"];
    cityString3, _ = (string)j.address["city"];
    cityString4, _ = (string)j[addressKey][cityKey];
    return cityString1, cityString2, cityString3, cityString4;
}

function testJsonExprAsIndex() (string) {
    json j = {name:"aaa", address:{city:"Colombo", "area":"city"}};
    
    string addressKey = "address";
    string cityKey = "city";

    //Moving index expression into another line since with new changes, it is a unsafe cast,
    //which returns a error value if any.
    string key;
    key, _ = (string)j.address.area;
    string value;
    value, _ = (string)j.address[key];
    return value;
}

function testSetArrayOutofBoundElement() (json) {
    json j = [1,2,3];
    j[7] = 8;
    return j;
}

function testSetToNonArrayWithIndex() (json) {
    json j = {name:"supun"};
    j[7] = 8;
    return j;
}

function testGetFromNonArrayWithIndex() (string) {
    json j = {name:"supun"};
    string name;
    name, _ = (string)j[7];
    return name;
}

function testSetToNonObjectWithKey() (json) {
    json j = [1,2,3];
    j["name"] = "Supun";
    return j;
}

function testGetFromNonObjectWithKey() (any) {
    json j = [1,2,3];
    return j["name"];
}

function testGetStringInArray() (string) {
    json j = ["a", "b", "c"];
    string value;
    value, _ = (string)j[1];
    return value;
}

function testGetArrayOutofBoundElement() (string) {
    json j = [1,2,3];
    string value;
    value, _ = (string)j[5];
    return value;
}

function testGetStringFromPrimitive() (string) {
    json j = {name:"Supun"};
    string name;
    name, _ = (string)j.name.fname;
    return name;
}

function testUpdateNestedElement() (json) {
    json j = {details: {fname:"Supun", lname:"Thilina"}};
    j.details.lname = "Setunga";
    return j;
}

function testEmptyStringToJson() (json) {
    string s = "";
    return (json) s;
}

function testJsonStringToJson() (json) {
    string s = "{\"name\", \"supun\"}";
    return (json) s;
}

function testStringWithEscapedCharsToJson() (json) {
    string s = "{\\\"name\\\", \"supun\"}";
    return (json) s;
}


function testJsonArrayToJsonCasting() (json){
    json[][] j1 = [[1, 2, 3], [3, 4, 5], [7, 8, 9]];
    
    var j2,_ = (json) j1;
    return j2;
}

function testJsonToJsonArrayCasting() (json[], json[][], errors:TypeCastError){
    json j1 = [[1, 2, 3], [3, 4, 5], [7, 8, 9]];
    
    var j2, e = (json[]) j1;
    var j3, e = (json[][]) j1;
    
    return j2, j3, e;
}

function testJsonToJsonArrayInvalidCasting() (json[][][], errors:TypeCastError){
    json j1 = [[1, 2, 3], [3, 4, 5], [7, 8, 9]];
    
    var j2, e = (json[][][]) j1;
    
    return j2, e;
}

function testJsonLength() (int, int, int){
    json[] j1 = [[1, 2, 3, 4], [3, 4, 5, 6], [7, 8, 9, 10]];
    
    json j2 = [[1, 2, 3, 4], [3, 4, 5, 6], [7, 8, 9, 10]];
    
    return lengthof j1, lengthof j2, j1.length;
}