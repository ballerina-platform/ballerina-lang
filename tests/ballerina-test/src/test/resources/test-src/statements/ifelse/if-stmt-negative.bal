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

function testIfStmtWithIncompatibleType1() returns boolean {
    if (false) {
        return false;
    } else if ("foo") {
        return true;
    }
}

function testIfStmtWithIncompatibleType2() {
    if ("foo") {
        int a = 5;
    }
    return;
}