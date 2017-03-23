function jsonReturnTest() (any) {
  any abc = jsonReturnFunction();
  return abc;
}

function jsonReturnFunction()(json) {
  json val = `{"PropertyName" : "Value"}`;
  return val;
}


function anyMethodParameter() (any) {
  int i = 9;
  return anyParam(i);
}

function anyParam(any val)(int) {
  int m = (int)val;
  return m;
}


function anyInStructTest()(any) {
  Sample sample = {i:1, val:true, msg:"sampleVal"};
  return sample.val;
}

struct Sample {
  int i;
  any val;
  string msg;
}


