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