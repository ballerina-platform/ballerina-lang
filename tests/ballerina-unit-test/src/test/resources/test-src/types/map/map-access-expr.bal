function mapAccessTest(int x, int y) returns (int) {
    map testMap;
    int xx;
    int yy;
    testMap["first"] = x;
    testMap["second"] = y;
    testMap["third"] = x + y;
    testMap["forth"] = x - y;
    xx = check <int> testMap.first;
    yy = check <int> testMap.second;

    return xx + yy;
}

function mapReturnTest(string firstName, string lastName) returns (map) {
    map testMap;
    testMap["fname"] = firstName;
    testMap["lname"] = lastName;
    testMap[ firstName + lastName ] = firstName + lastName;
    return testMap;
}

function testArrayAccessAsIndexOfMapt() returns (string) {
    map namesMap = {fname:"Supun",lname:"Setunga"};
    string[] keys = ["fname","lname"];
    string key;
    key = namesMap[keys[0]] but { () => "", any a => <string> a};
    return key;
}

function testAccessThroughVar() returns (string) {
    map m;
    m["x"] = "a";
    m["y"] = "b";
    m["z"] = "c";
    return constructString(m);
}

function constructString(map m) returns (string) {
    string [] keys = m.keys();
    int len = lengthof keys;
    int i = 0;
    string returnStr = "";
    while (i < len) {
        string key = keys[i];
        string val =  m[key] but { () => "", any a => <string> a};
        returnStr = returnStr + key + ":" + val + ", ";
        i = i + 1;
    }
    return returnStr;
}

function testMapClear() returns (int) {
    map namesMap = {fname:"Supun", lname:"Setunga", sname:"Kevin", tname:"Ratnasekera"};
    namesMap.clear();
    return lengthof namesMap;
}

function testHasKeyPositive() returns (boolean) {
    map namesMap = {fname:"Supun", lname:"Setunga", sname:"Kevin", tname:"Ratnasekera"};
    return namesMap.hasKey("fname");
}

function testHasKeyNegative() returns (boolean) {
    map namesMap = {fname:"Supun", lname:"Setunga", sname:"Kevin", tname:"Ratnasekera"};
    return namesMap.hasKey("fname2");
}

function testGetMapValues () returns (string, string) {
    json j = {"city":"Colombo", "country":"SriLanka"};
    int[] arr = [7,8,9];
    map address = {city:"CA", "country":"USA"};
    map<map>[] addressArray=[{address:{city:"Colombo", "country":"SriLanka"}},
                               {address:{city:"Kandy", "country":"SriLanka"}},
                               {address:{city:"Galle", "country":"SriLanka"}}];
    map m = { name:"Supun",
                  age:25,
                  gpa:2.81,
                  status:true,
                  info:null,
                  address:address,
                  intArray:arr,
                  addressArray:addressArray,
                  finfo:j
                };
    any[] values = m.values();
    var nam = <string> values[0];
    var jsn = check <json> values[8];
    var city = check <string> jsn.city;
    return (nam, city);
}

function testMapRemovePositive() returns (boolean, boolean, boolean) {
    map namesMap = {fname:"Supun", lname:"Setunga", sname:"Kevin", tname:"Ratnasekera"};
    return (namesMap.hasKey("fname"), namesMap.remove("fname"), namesMap.hasKey("fname"));
}

function testMapRemoveNegative() returns (boolean, boolean, boolean) {
    map namesMap = {fname:"Supun", lname:"Setunga", sname:"Kevin", tname:"Ratnasekera"};
    return (namesMap.hasKey("fname2"), namesMap.remove("fname2"), namesMap.hasKey("fname2"));
}

function testMapConcurrentAccess() returns int {
    map<int> intMap;
    int n = 100000;
    processConcurrent(intMap, n);
    return lengthof intMap;
}

function processConcurrent(map<int> intMap, int n) {
    worker w1 {
      int i = 0;
      int j;
      string k;
      while (i < n) {
         intMap["X"] = 100;
         j = intMap.X;
         k = <string> i;
         intMap[k] = i;
         i = intMap[k] ?: 0;  
         _ = intMap.remove(k);
         i += 1;
      }
    }
    worker w2 {
      int i = n;
      int j;
      string k;
      int n2 = n * 2;
      while (i < n2) {
         intMap["X"] = 200;
         j = intMap.X;
         k = <string> i;
         intMap[k] = i;
         i = intMap[k] ?: 0;
         _ = intMap.remove(k);
         i += 1;
      }
    }
}

function testConcurrentMapGetKeys() returns error? {
    map<int> intMap;
    int n = 100000;
    return processConcurrentKeys(intMap, n);
}

function processConcurrentKeys(map<int> intMap, int n) returns error? {
    worker w1 {
        int i = 0;
        int j;
        string k;
        while (i < n) {
            intMap["X"] = 100;
            j = intMap.X;
            k = <string> i;
            intMap[k] = i;
            i = intMap[k] ?: 0;  
            _ = intMap.remove(k);
            i += 1;
        }
    }
    worker w2 {
        int i = n;
        int j;
        string k;
        int n2 = n * 2;
        while (i < n2) {
            intMap["X"] = 200;
            j = intMap.X;
            k = <string> i;
            intMap[k] = i;
            i = intMap[k] ?: 0;
            _ = intMap.remove(k);
            i += 1;
        }
    }
    worker w3 {
        try { 
            int i = 0;
            int length;
            while (i < n) {
                length = lengthof intMap.keys();
                i += 1;
            }
        } catch (error e) {
            return e;
        }
        return ();
    }
}
