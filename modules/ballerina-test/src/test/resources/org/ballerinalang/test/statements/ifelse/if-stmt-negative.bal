function invalidIfElse(string[] args) {
  if (5) {
    // nothing
  }
  
  if (false) {
    int a;
  }
}

function foo() returns (string) {
  if (true) {
    return "returning from if";
  } else {
    return "returning from else";
  }
}
