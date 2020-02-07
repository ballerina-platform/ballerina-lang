function invalidCastingError() {
  any abc = intReturn();
  int sample = abc;
}

function intReturn() returns (any) {
  int val = 6;
  return val;
}
