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

function testIfStmtWithIncompatibleType3() {
    if "foo" {
        int a = 5;
    } else if 4 {
        int b = 4;
    }

    if [5, "baz"] {
        //do nothing
    }
    return;
}
