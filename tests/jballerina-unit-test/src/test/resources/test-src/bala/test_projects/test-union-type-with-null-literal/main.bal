import testorg/testType;

function testPositiveAssignment(){
    testType:TestType a = func1(testType:doThis);
}

function func1(function (testType:TestType resp) returns testType:TestType callback) returns testType:TestType{
    testType:TestType a = null;
    testType:TestType b = callback(a);
    return b;
}
