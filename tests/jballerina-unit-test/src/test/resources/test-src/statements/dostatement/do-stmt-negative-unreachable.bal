type ErrorTypeA distinct error;

const TYPE_A_ERROR_REASON = "TypeA_Error";

type ErrorTypeB distinct error;

const TYPE_B_ERROR_REASON = "TypeB_Error";

function testUnreachableAfterFail () returns string {
   string str = "";
   do {
     error err = error("custom error", message = "error value");
     str += "Before failure throw";
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
   do {
     str += "Before failure throw";
     fail error ErrorTypeA(TYPE_A_ERROR_REASON, message = "Error Type A");
   }
   on fail ErrorTypeA e {
      str += "-> Error caught ! ";
   }
   str += "-> Execution continues...";
   return str;
}

function testIgnoreReturnInOnFail () returns string {
   string str = "";
   do {
     str += "Before failure throw";
     fail error ErrorTypeA(TYPE_A_ERROR_REASON, message = "Error Type A");
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
   do {
     str += "Before failure throw";
     fail error ErrorTypeA(TYPE_A_ERROR_REASON, message = "Error Type A");
   }
   on fail ErrorTypeA e {
      str += "-> Error caught ! ";
      return str;
      str += "-> After returning string";
   }
   str += "-> Execution continues...";
   return str;
}

function testReturnWitihinDo() returns string {
  string str = "";
  do {
      str = str + "do statement start";
      return str;
  }
  str = str + "do statemtnt finished";
}
