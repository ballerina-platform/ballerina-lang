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
      } on fail error e2 {
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

//todo @chiran improve to append error message
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

//todo @chiran check type conversion with return type
//function testReturnErrorWithinOnFail() returns error?  {
//   string str = "";
//   do {
//     error err = error("custom error", message = "error value");
//     str += "Before failure throw";
//     fail err;
//   }
//   on fail error e {
//      str += "-> Error caught ! ";
//      return e;
//   }
//}

//function testOnFail () returns string {
//   string str = "";
//   do {
//     error err = error("custom error", message = "error value");
//     str += "Before failure throw";
//     fail err;
//   }
//   on fail error e {
//      str += "-> Error caught: ";
//      str = str.concat("-> Error caught: ", e.message());
//   }
//   str += "-> Execution continues";
//   io:println(str);
//   return str;
//}


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
