type AssertionError error;

const ASSERTION_ERROR_REASON = "AssertionError";

type ErrorTypeA distinct error;

const TYPE_A_ERROR_REASON = "TypeA_Error";

type ErrorTypeB distinct error;

const TYPE_B_ERROR_REASON = "TypeB_Error";

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

    assertEquality(44, testLambdaFunctionWithOnFail());
    assertEquality(44, testArrowFunctionInsideOnFail());

    string testOnFailWithUnionRes = testOnFailWithUnion();
    assertEquality("Before failure throw-> Error caught : TypeA_Error-> Execution continues...", testOnFailWithUnionRes);

    string failWithinOnFail1Res = testFailWithinOnFail1();
    assertEquality("-> Within do statement-> Error caught in inner on fail-> Error caught in outer on fail" +
    "-> Execution completed.", failWithinOnFail1Res);

    string|error failWithinOnFail2Res = testFailWithinOnFail2();
    assertEquality("custom error", (<error>failWithinOnFail2Res).message());

    string failWithinOnFail3Res = testFailWithinOnFail3();
    assertEquality("-> Within 1st do statement-> Error caught in 1st on-fail-> Within 2nd do statement"
    + "-> Error caught in 2nd on-fail-> Error caught in outer on-fail-> Execution completed.",
    failWithinOnFail3Res);

    string failWithinOnFail4Res = testFailWithinOnFail4();
    assertEquality("-> Within do statement-> Error caught in inner on fail-> Error caught in outer on fail" +
    "-> Execution completed.", failWithinOnFail4Res);
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

public function testLambdaFunctionWithOnFail() returns int {
    var lambdaFunc = function () returns int {
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
    };
    return lambdaFunc();
}

function testOnFailWithUnion () returns string {
   string str = "";
   var getTypeAError = function () returns int|ErrorTypeA{
       ErrorTypeA errorA = ErrorTypeA(TYPE_A_ERROR_REASON, message = "Error Type A");
       return errorA;
   };
   var getTypeBError = function () returns int|ErrorTypeB{
       ErrorTypeB errorB = ErrorTypeB(TYPE_B_ERROR_REASON, message = "Error Type B");
       return errorB;
   };
   do {
     str += "Before failure throw";
     int resA = check getTypeAError();
     int resB = check getTypeBError();
   }
   on fail ErrorTypeA|ErrorTypeB e {
      str += "-> Error caught : ";
      str = str.concat(e.message());
   }
   str += "-> Execution continues...";
   return str;
}

function testFailWithinOnFail1() returns string {
    int i = 0;
    string str = "";
    while (i < 2) {
      do {
          str += "-> Within do statement";
          i = i + 1;
          fail error("custom error", message = "error value");
      } on fail error e {
         str += "-> Error caught in inner on fail";
         fail e;
      }
      str += "-> After do statement";
    } on fail error ee {
       str += "-> Error caught in outer on fail";
    }
    str += "-> Execution completed.";
    return str;
}

function testFailWithinOnFail2() returns string|error {
    int i = 0;
    string str = "";
    while (i < 2) {
      do {
          str += "-> Within do statement";
          i = i + 1;
          fail error("custom error", message = "error value");
      } on fail error e {
         str += "-> Error caught in inner on fail";
         fail e;
      }
      str += "-> After do statement";
    }
    str += "-> Execution completed.";
    return str;
}

function testFailWithinOnFail3() returns string {
    int i = 0;
    string str = "";
    while (i < 2) {
      do {
          str += "-> Within 1st do statement";
          i = i + 1;
          fail error("custom error", message = "error value");
      } on fail error e1 {
          str += "-> Error caught in 1st on-fail";
          do {
               str += "-> Within 2nd do statement";
               i = i + 1;
               fail error("custom error", message = "error value");
          } on fail error e2 {
              str += "-> Error caught in 2nd on-fail";
              fail e2;
          }
      }
      str += "-> After do statement";
    } on fail error e3 {
        str += "-> Error caught in outer on-fail";
    }
    str += "-> Execution completed.";
    return str;
}

function testFailWithinOnFail4() returns string {
    int i = 0;
    string str = "";
    while (i < 2) {
      do {
          str += "-> Within do statement";
          i = i + 1;
          int val1 = check getError();
      } on fail error e {
         str += "-> Error caught in inner on fail";
         int val2 = check getError();
      }
      str += "-> After do statement";
    } on fail error ee {
       str += "-> Error caught in outer on fail";
    }
    str += "-> Execution completed.";
    return str;
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
