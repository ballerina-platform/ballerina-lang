xmlns "http://sample.com/wso2/a1" as ns0;

function testCompoundAssignmentAddition()(int){
    int x = 5;
    x += 10;
    return x;
}

function testCompoundAssignmentSubtraction()(int){
    int x = 5;
    x -= 10;
    return x;
}

function testCompoundAssignmentMultiplication()(int){
    int x = 5;
    x *= 10;
    return x;
}

function testCompoundAssignmentDivision()(int){
    int x = 100;
    x /= 10;
    return x;
}

function testIncrementOperator()(int){
    int x = 100;
    x++;
    return x;
}

function testDecrementOperator()(int){
    int x = 100;
    x--;
    return x;
}

function testCompoundAssignmentAdditionArrayElement()(int){
    int[] x = [];
    x[0] = 100;
    x[0] += 10;
    return x[0];
}

function testCompoundAssignmentSubtractionArrayElement()(int){
    int[] x = [];
    x[0] = 100;
    x[0] -= 10;
    return x[0];
}

function testCompoundAssignmentMultiplicationArrayElement()(int){
    int[] x = [];
    x[0] = 100;
    x[0] *= 10;
    return x[0];
}

function testCompoundAssignmentDivisionArrayElement()(int){
     int[] x = [];
     x[0] = 100;
     x[0] /= 10;
     return x[0];
}

struct Company {
   int count;
   int count2;
}

function testCompoundAssignmentAdditionStructElement()(int){
    Company ibm = {};
    ibm.count = 100;
    ibm.count += 10;
    return ibm.count;
}

function testCompoundAssignmentSubtractionStructElement()(int){
    Company ibm = {};
    ibm.count = 100;
    ibm.count -= 10;
    return ibm.count;
}

function testCompoundAssignmentMultiplicationStructElement()(int){
    Company ibm = {};
    ibm.count = 100;
    ibm.count *= 10;
    return ibm.count;
}

function testCompoundAssignmentDivisionStructElement()(int){
    Company ibm = {};
    ibm.count = 100;
    ibm.count /= 10;
    return ibm.count;
}

function testIncrementOperatorArrayElement()(int){
    int[] x = [];
    x[0] = 100;
    x[0]++;
    return x[0];
}

function testDecrementOperatorArrayElement()(int){
    int[] x = [];
    x[0] = 100;
    x[0]--;
    return x[0];
}

function testIncrementOperatorStructElement()(int){
    Company ibm = {};
    ibm.count = 888;
    ibm.count++;
    return ibm.count;
}

function testDecrementOperatorStructElement()(int){
    Company ibm = {};
    ibm.count = 888;
    ibm.count--;
    return ibm.count;
}

function testStringIntCompoundAssignmentAddition()(string){
    int x = 5;
    string a = "test";
    a += x;
    return a;
}

function testIncrementOperatorFloat()(float){
    float x = 100;
    x++;
    return x;
}

function testDecrementOperatorFloat()(float){
    float x = 100;
    x--;
    return x;
}

function testIntFloatCompoundAssignmentAddition()(float){
    int x = 5;
    float d = 2.5;
    d += x;
    return d;
}

function testXMLAttributeWithCompoundAssignment()(string){
    var x1, _ = <xml> "<root xmlns:ns3=\"http://sample.com/wso2/f\"></root>";
    x1@[ns0:foo1] = "bar1";
    x1@[ns0:foo1] += "bar2";
    return x1@[ns0:foo1];
}

function testCompoundAssignmentAdditionRecursive()(int){
    int x = 5;
    x += x;
    return x;
}

function testCompoundAssignmentAdditionStructElementRecursive()(int){
    Company ibm = {};
    ibm["count"] = 100;
    ibm["count"] += ibm["count"];
    return ibm["count"];
}

function testCompoundAssignmentAdditionStructElements()(int){
    Company ibm = {};
    ibm["count"] = 100;
    ibm["count2"] = 400;
    ibm["count"] += ibm["count2"];
    return ibm["count"];
}

function testCompoundAssignmentAdditionWithExpression()(int){
    int x = 5;
    x += (2+3+4+5);
    return x;
}

function testCompoundAssignmentAdditionMultiple()(int){
    int x = 5;
    x += 5;
    x += 5;
    x += 5;
    return x;
}

function testCompoundAssignmentAdditionMultipleWithIncrement()(int){
    int x = 5;
    x += 5;
    x++;
    x += 5;
    x++;
    x += 5;
    x--;
    x--;
    x--;
    return x;
}

function testCompoundAssignmentAdditionWithStructAccess()(int){
    Company ibm = {};
    ibm["count"] = 100;
    int[] arr = [];
    arr[0] = 200;
    int x = 5;
    x += (ibm["count"] + arr[0]);
    return x;
}

function testCompoundAssignmentAdditionWithFunctionInvocation()(int){
    int x = 5;
    x += getIncrement();
    return x;
}


function getIncrement()(int) {
   return 200;
}