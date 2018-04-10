xmlns "http://sample.com/wso2/a1" as ns0;

function testCompoundAssignmentAddition() returns (int){
    int x = 5;
    x += 10;
    return x;
}

function testCompoundAssignmentSubtraction()  returns (int){
    int x = 5;
    x -= 10;
    return x;
}

function testCompoundAssignmentMultiplication() returns (int){
    int x = 5;
    x *= 10;
    return x;
}

function testCompoundAssignmentDivision() returns (int){
    int x = 100;
    x /= 10;
    return x;
}

function testIncrementOperator() returns (int){
    int x = 100;
    x++;
    return x;
}

function testDecrementOperator() returns (int){
    int x = 100;
    x--;
    return x;
}

function testCompoundAssignmentAdditionArrayElement() returns (int){
    int[] x = [];
    x[0] = 100;
    x[0] += 10;
    return x[0];
}

function testCompoundAssignmentSubtractionArrayElement() returns (int){
    int[] x = [];
    x[0] = 100;
    x[0] -= 10;
    return x[0];
}

function testCompoundAssignmentMultiplicationArrayElement() returns (int){
    int[] x = [];
    x[0] = 100;
    x[0] *= 10;
    return x[0];
}

function testCompoundAssignmentDivisionArrayElement() returns (int){
     int[] x = [];
     x[0] = 100;
     x[0] /= 10;
     return x[0];
}

type Company {
   int count;
   int count2;
};

function testCompoundAssignmentAdditionStructElement() returns (int){
    Company ibm = {};
    ibm.count = 100;
    ibm.count += 10;
    return ibm.count;
}

function testCompoundAssignmentSubtractionStructElement() returns (int){
    Company ibm = {};
    ibm.count = 100;
    ibm.count -= 10;
    return ibm.count;
}

function testCompoundAssignmentMultiplicationStructElement() returns (int){
    Company ibm = {};
    ibm.count = 100;
    ibm.count *= 10;
    return ibm.count;
}

function testCompoundAssignmentDivisionStructElement() returns (int){
    Company ibm = {};
    ibm.count = 100;
    ibm.count /= 10;
    return ibm.count;
}

function testIncrementOperatorArrayElement() returns (int){
    int[] x = [];
    x[0] = 100;
    x[0]++;
    return x[0];
}

function testDecrementOperatorArrayElement() returns (int){
    int[] x = [];
    x[0] = 100;
    x[0]--;
    return x[0];
}

function testIncrementOperatorStructElement() returns (int){
    Company ibm = {};
    ibm.count = 888;
    ibm.count++;
    return ibm.count;
}

function testDecrementOperatorStructElement() returns (int){
    Company ibm = {};
    ibm.count = 888;
    ibm.count--;
    return ibm.count;
}

function testStringIntCompoundAssignmentAddition() returns (string){
    int x = 5;
    string a = "test";
    a += x;
    return a;
}

function testIncrementOperatorFloat() returns (float){
    float x = 100;
    x++;
    return x;
}

function testDecrementOperatorFloat() returns (float){
    float x = 100;
    x--;
    return x;
}

function testIntFloatCompoundAssignmentAddition() returns (float){
    int x = 5;
    float d = 2.5;
    d += x;
    return d;
}

function testXMLAttributeWithCompoundAssignment() returns (string){
    xml x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f"></root>`;
    x1@[ns0:foo1] = "bar1";
    x1@[ns0:foo1] += "bar2";
    return x1@[ns0:foo1];
}

function testCompoundAssignmentAdditionRecursive() returns (int){
    int x = 5;
    x += x;
    return x;
}

function testCompoundAssignmentAdditionStructElementRecursive() returns (int){
    Company ibm = {};
    ibm["count"] = 100;
    ibm["count"] += ibm["count"];
    return ibm["count"];
}

function testCompoundAssignmentAdditionStructElements() returns (int){
    Company ibm = {};
    ibm["count"] = 100;
    ibm["count2"] = 400;
    ibm["count"] += ibm["count2"];
    return ibm["count"];
}

function testCompoundAssignmentAdditionWithExpression() returns (int){
    int x = 5;
    x += (2+3+4+5);
    return x;
}

function testCompoundAssignmentAdditionMultiple() returns (int){
    int x = 5;
    x += 5;
    x += 5;
    x += 5;
    return x;
}

function testCompoundAssignmentAdditionMultipleWithIncrement() returns (int){
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

function testCompoundAssignmentAdditionWithStructAccess() returns (int){
    Company ibm = {};
    ibm["count"] = 100;
    int[] arr = [];
    arr[0] = 200;
    int x = 5;
    x += (ibm["count"] + arr[0]);
    return x;
}

function testCompoundAssignmentAdditionWithFunctionInvocation() returns (int){
    int x = 5;
    x += getIncrement();
    return x;
}


function getIncrement() returns (int) {
   return 200;
}
