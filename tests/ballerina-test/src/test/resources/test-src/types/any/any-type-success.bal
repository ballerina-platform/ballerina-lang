function jsonReturnTest() returns (any) {
  any abc = jsonReturnFunction();
  return abc;
}

function jsonReturnFunction() returns (json) {
  json val = {"PropertyName" : "Value"};
  return val;
}

function tableReturnTestAsAny() returns (any) {
    any abc = tableReturnFunction();
    return abc;
}

function inputAnyAsTableTest() returns (json) | error {
    table t = check anyToTableCastFunction(tableReturnFunction());
    return <json> t;
}

function anyToTableCastFunction (any aTable) returns (table) | error {
    var result = <table> aTable;
    match result {
        table casted => return casted;
        error e => return e;
    }
}

function tableReturnFunction () returns (table) {
    table <Employee> tb = table{};
    Employee e1 = {id:1, name:"Jane"};
    Employee e2 = {id:2, name:"Anne"};
    _ = tb.add(e1);
    _ = tb.add(e2);

    return tb;
}

type Employee {
    int id;
    string name;
};


function anyMethodParameter() returns (any) {
  int i = 9;
  return anyParam(i);
}

function anyParam(any val) returns (int) {
  int m;
  m = check <int>val;
  return m;
}


function anyInStructTest() returns (any) {
  Sample sample = {i:1, val:true, msg:"sampleVal"};
  return sample.val;
}

type Sample {
  int i;
  any val;
  string msg;
};


function successfulIntCasting() returns (int) {
  any abc = floatReturn();
  float floatVal;
  floatVal = check <float>abc;
  //Int to float is a conversion now
  int intVal;
  intVal = <int>floatVal;
  return intVal;
}

function floatReturn() returns (float) {
  float val = 5.6;
  return val;
}


function anyToAnyExplicitCasting() returns (any) {
  any abc = jsonReturnFunction();
  any val = abc;
  return val;
}


function multipleReturnWithAny() returns (any, int) {
  any abc = jsonReturnFunction();
  return (abc, 7);
}


function multipleParamWithAny() returns (int) {
  any abc = jsonReturnFunction();
  int val = multipleParam(abc, 5);
  return val;
}

function multipleParam(any val, int sam) returns (int) {
  return sam;
}

function variableDefTest() returns (any) {
    any val = 5;
    return val;
}

function assignmentTest() returns (any) {
    any val;
    val = 44.3;
    return val;
}

function anyArrayWithMapArray() returns (any[]) {
    map[] ma = [];
    any[] a = ma;
    return a;
}

