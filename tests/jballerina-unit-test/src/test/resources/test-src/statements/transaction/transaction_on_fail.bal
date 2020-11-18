type AssertionError error;

const ASSERTION_ERROR_REASON = "AssertionError";

function testOnFailStatement() {
    string onFailResult = testTrxReturnVal();
    assertEquality("start -> within transaction1 -> within transaction2 -> error handled", onFailResult);

    assertEquality(44,testLambdaFunctionWithOnFail());

    string nestedTrxWithLessOnFailsRestult = testNestedTrxWithLessOnFails();
    assertEquality("-> Before error 1 is thrown -> Before error 2 is thrown -> Error caught !" +
    "-> Execution continues...", nestedTrxWithLessOnFailsRestult);

    string appendOnFailErrorResult = testAppendOnFailError();
    assertEquality("Before failure throw -> Error caught: custom error -> Execution continues...", appendOnFailErrorResult);
}

function testTrxReturnVal() returns string {
    string str = "start";
    transaction {
        str = str + " -> within transaction1";
        var ii = commit;
        error err = error("custom error", message = "error value");
        transaction {
            str = str + " -> within transaction2";
            var commitRes = commit;
        }
        int res2 = check getError();
    } on fail error e {
        str += " -> error handled";
    }
    return str;
}

public function testLambdaFunctionWithOnFail() returns int {
    var lambdaFunc = function () returns int {
          int a = 10;
          int b = 11;
          int c = 0;
          transaction {
              error err = error("custom error", message = "error value");
              c = a + b;
              check commit;
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

function testNestedTrxWithLessOnFails () returns string {
   string str = "";
   transaction {
      str += "-> Before error 1 is thrown";
      transaction {
          str += " -> Before error 2 is thrown";
          var resCommit2 = commit;
          int res2 =  check getError();
      }
      var resCommit1 = commit;
   }
   on fail error e1 {
       str += " -> Error caught !";
   }
   str += "-> Execution continues...";
   return str;
}

function getError() returns int|error {
  error err = error("custom error", message = "error value");
  return err;
}

function testAppendOnFailError () returns string {
   string str = "";
   transaction {
     error err = error("custom error", message = "error value");
     str += "Before failure throw";
     check commit;
     fail err;
   }
   on fail error e {
      str += " -> Error caught: ";
      str = str.concat(e.message());
   }
   str += " -> Execution continues...";
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
