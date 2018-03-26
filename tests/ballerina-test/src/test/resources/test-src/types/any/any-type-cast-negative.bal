function invalidCastingError() {
  any abc = intReturn();
  float val = <float> abc;
}

function intReturn() returns (any) {
  int val = 6;
  return val;
}


function undefinedCasting() {
  any val;
  int i = <int> val;
}

