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

function inputAnyAsTableTest() returns (table<any>?) {
    table<any>? t = anyToTableFunction(tableReturnFunction());
    return t;
}

function anyToTableFunction (any aTable) returns (table<any>?) {
    if (aTable is table<any>) {
        return aTable;
    }
    return ();
}

function tableReturnFunction () returns (table<any>) {
    table <Employee> tb = table{};
    Employee e1 = {id:1, name:"Jane"};
    Employee e2 = {id:2, name:"Anne"};
    checkpanic tb.add(e1);
    checkpanic tb.add(e2);

    return tb;
}

type Employee record {
    int id;
    string name;
};


function anyMethodParameter() returns (any|error) {
  int i = 9;
  return anyParam(i);
}

function anyParam(any val) returns int|error {
  int|error m = <int> val;
  return m;
}


function anyInStructTest() returns (any) {
  Sample sample = {i:1, val:true, msg:"sampleVal"};
  return sample.val;
}

type Sample record {
  int i;
  any val;
  string msg;
};


function successfulIntCasting() returns int|error {
  any abc = floatReturn();
  float floatVal = <float> abc;
  // Float to int type conversion
  int intVal;
  intVal = <int>floatVal;
  return intVal;
}

function floatReturn() returns (float) {
  float val = 5.4;
  return val;
}


function anyToAnyExplicitCasting() returns (any) {
  any abc = jsonReturnFunction();
  any val = abc;
  return val;
}


function multipleReturnWithAny() returns [any, int] {
  any abc = jsonReturnFunction();
  return [abc, 7];
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
    map<any>[] ma = [];
    any[] a = ma;
    return a;
}

