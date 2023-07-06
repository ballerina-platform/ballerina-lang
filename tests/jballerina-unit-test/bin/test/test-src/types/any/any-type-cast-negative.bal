function invalidCastingError() returns error|() {
  any abc = stringReturn();
  float val = check trap <float> abc;
  return ();
}

function stringReturn() returns (any) {
  string val = "a";
  return val;
}
