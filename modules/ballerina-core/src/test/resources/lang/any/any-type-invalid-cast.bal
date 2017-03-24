function invalidCastingError() {
  any abc = intReturn();
  float val = (float)abc;
}

function intReturn()(any) {
  int val = 6;
  return val;
}




