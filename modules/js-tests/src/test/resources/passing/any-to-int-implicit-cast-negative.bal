function invalidCastingError() {
  any abc = intReturn();
  int sample = abc;
}

function intReturn()(any) {
  int val = 6;
  return val;
}
