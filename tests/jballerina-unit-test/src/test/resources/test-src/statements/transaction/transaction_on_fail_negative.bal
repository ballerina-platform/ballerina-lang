type ErrorTypeA distinct error;

const TYPE_A_ERROR_REASON = "TypeA_Error";

type ErrorTypeB distinct error;

const TYPE_B_ERROR_REASON = "TypeB_Error";

function testUnreachableAfterFail () returns string {
   string str = "";
   transaction {
     error err = error("custom error", message = "error value");
     str += "Before failure throw";
     var res = commit;
     fail err;
     str += "After failure throw";
   }
   on fail error e {
      str += "-> Error caught ! ";
   }
   str += "-> Execution continues...";
   return str;
}

function testIncompatibleErrorTypeOnFail () returns string {
   string str = "";
   transaction {
     str += "Before failure throw";
     var res = commit;
     fail ErrorTypeA(TYPE_A_ERROR_REASON, message = "Error Type A");
   }
   on fail ErrorTypeB e {
      str += "-> Error caught ! ";
   }
   str += "-> Execution continues...";
   return str;
}

function testIgnoreReturnInOnFail () returns string {
   string str = "";
   transaction {
     str += "Before failure throw";
     var res = commit;
     fail ErrorTypeA(TYPE_A_ERROR_REASON, message = "Error Type A");
   }
   on fail ErrorTypeA e {
      str += "-> Error caught ! ";
      return str;
   }
   str += "-> Execution continues...";
   return str;
}

function testUnreachableInOnFail () returns string {
   string str = "";
   transaction {
     str += "Before failure throw";
     var res = commit;
     fail ErrorTypeA(TYPE_A_ERROR_REASON, message = "Error Type A");
   }
   on fail ErrorTypeA e {
      str += "-> Error caught ! ";
      return str;
      str += "-> After returning string";
   }
   str += "-> Execution continues...";
   return str;
}

function testNestedTrxWithLessOnFails () returns string {
   string str = "";
   transaction {
     str += "-> Before error 1 is thrown";
     check commit;
      transaction {
          str += " -> Before error 2 is thrown";
          check commit;
          fail ErrorTypeA(TYPE_A_ERROR_REASON, message = "Error Type A");
      }
      fail ErrorTypeA(TYPE_A_ERROR_REASON, message = "Error Type A");
   }
   on fail error e1 {
       str += " -> Error caught !";
   }
   str += "-> Execution continues...";
   return str;
}