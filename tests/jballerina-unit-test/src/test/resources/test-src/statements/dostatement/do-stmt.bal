type AssertionError error;

const ASSERTION_ERROR_REASON = "AssertionError";

function testOnFailStatement() {
    string testOnFailRes = testOnFail();
    assertEquality("Before failure throw-> Error caught ! -> Execution continues...", testOnFailRes);

    string testReturnWithinOnFailRes = testReturnWithinOnFail();
    assertEquality("Before failure throw-> Error caught !", testReturnWithinOnFailRes);

     string|error testOnFailWithCheckExprRes = testOnFailWithCheckExpr();
     if(testOnFailWithCheckExprRes is string) {
         assertEquality("Before failure throw-> Error caught ! -> Execution continues...", testOnFailWithCheckExprRes);
     } else {
          panic error("Expected error to be caught. Hence, test failed.");
     }

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
