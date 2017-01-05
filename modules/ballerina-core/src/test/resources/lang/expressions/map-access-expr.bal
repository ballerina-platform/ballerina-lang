import ballerina.lang.system;

function mapAccessTest(int x, int y)(int) {
    map testMap;
    int xx;
    int yy;
    testMap["first"] = x;
    testMap["second"] = y;
    xx = testMap["first"];
    yy = testMap["second"];
    system:println(xx);
    system:println(yy);

    return xx + yy;
}

function mapReturnTest(string firstName, string lastName) (map) {
    map testMap;
    testMap["fname"] = firstName;
    testMap["lname"] = lastName;
    testMap[ firstName + lastName ] = firstName + lastName;
    return testMap;
}


