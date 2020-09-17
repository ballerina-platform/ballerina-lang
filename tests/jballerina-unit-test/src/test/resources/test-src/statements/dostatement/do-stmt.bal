type AssertionError error;

const ASSERTION_ERROR_REASON = "AssertionError";

function testOnFailStatement() {
    string onFailResult = testOnFail();
    assertEquality("Before failure throw-> Error caught ! -> Execution continues...", onFailResult);

    string returnWithinOnFailResult = testReturnWithinOnFail();
    assertEquality("Before failure throw-> Error caught !", returnWithinOnFailResult);

     string|error onFailWithCheckExprResult = testOnFailWithCheckExpr();
     if(onFailWithCheckExprResult is string) {
         assertEquality("Before failure throw-> Error caught ! -> Execution continues...", onFailWithCheckExprResult);
     } else {
          panic error("Expected error to be caught. Hence, test failed.");
     }

    string nestedDoWithOnFailResult = testNestedDoWithOnFail();
    assertEquality("-> Before error 1 is thrown -> Before error 2 is thrown -> error 2 caught ! " +
    "-> error 1 caught !-> Execution continues...", nestedDoWithOnFailResult);

    string nestedDoWithLessOnFailsRestult = testNestedDoWithLessOnFails();
        assertEquality("-> Before error 1 is thrown -> Before error 2 is thrown -> Error caught !" +
        "-> Execution continues...", nestedDoWithLessOnFailsRestult);

    error? errorReturnWithinOnFailResult = testReturnErrorWithinOnFail();
    if(errorReturnWithinOnFailResult is error) {
         assertEquality("custom error", errorReturnWithinOnFailResult.message());
    } else {
          panic error("Expected error to be caught. Hence, test failed.");
    }

    string appendOnFailErrorResult = testAppendOnFailError();
    assertEquality("Before failure throw -> Error caught: custom error -> Execution continues...", appendOnFailErrorResult);

    assertEquality(44, testArrowFunctionInsideOnFail());
}

function testOnFail () returns string {
   string str = "";
   do {
     error err = error("custom error", message = "error value");
     str += "Before failure throw";
     fail err;
   }
   on fail error e {
      str += "-> Error caught ! ";
   }
   str += "-> Execution continues...";
   return str;
}

function testReturnWithinOnFail() returns string  {
   string str = "";
   do {
     error err = error("custom error", message = "error value");
     str += "Before failure throw";
     fail err;
   }
   on fail error e {
      str += "-> Error caught !";
      return str;
   }
   return str;
}

function testOnFailWithCheckExpr () returns string|error {
   string str = "";
   do {
     error err = error("custom error", message = "error value");
     str += "Before failure throw";
     int val = check getError();
   }
   on fail error e {
      str += "-> Error caught ! ";
   }
   str += "-> Execution continues...";
   return str;
}

function getError()  returns int|error {
    error err = error("Custom Error");
    return err;
}


function testNestedDoWithOnFail () returns string {
   string str = "";
   do {
     error err1 = error("custom error 1", message = "error value");
     str += "-> Before error 1 is thrown";
      do {
          error err2 = error("custom error 2", message = "error value");
          str += " -> Before error 2 is thrown";
          fail err2;
      } on fail var e2 {
          str += " -> error 2 caught !";
      }
     fail err1;
   }
   on fail error e1 {
       str += " -> error 1 caught !";
   }
   str += "-> Execution continues...";
   return str;
}

function testNestedDoWithLessOnFails () returns string {
   string str = "";
   do {
     error err1 = error("custom error 1", message = "error value");
     str += "-> Before error 1 is thrown";
      do {
          error err2 = error("custom error 2", message = "error value");
          str += " -> Before error 2 is thrown";
          fail err2;
      }
      fail err1;
   }
   on fail error e1 {
       str += " -> Error caught !";
   }
   str += "-> Execution continues...";
   return str;
}

function testReturnErrorWithinOnFail() returns error?  {
   string str = "";
   do {
     error err = error("custom error", message = "error value");
     str += "Before failure throw";
     fail err;
   }
   on fail error e {
      str += "-> Error caught ! ";
      return e;
   }
}

function testAppendOnFailError () returns string {
   string str = "";
   do {
     error err = error("custom error", message = "error value");
     str += "Before failure throw";
     fail err;
   }
   on fail error e {
      str += " -> Error caught: ";
      str = str.concat(e.message());
   }
   str += " -> Execution continues...";
   return str;
}

public function testArrowFunctionInsideOnFail() returns int {
    int a = 10;
    int b = 11;
    int c = 0;
    do {
      error err = error("custom error", message = "error value");
      c = a + b;
      fail err;
    }
    on fail error e {
       function (int, int) returns int arrow = (x, y) => x + y + a + b + c;
       a = arrow(1, 1);
    }
    return a;
}


function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic AssertionError(ASSERTION_ERROR_REASON,
            message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
