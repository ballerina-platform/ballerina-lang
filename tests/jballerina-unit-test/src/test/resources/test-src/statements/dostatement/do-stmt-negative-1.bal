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
   do {
     str += "Before failure throw";
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
   do {
     str += "Before failure throw";
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

function testReturnWitihinDo() returns string {
  string str = "";
  do {
      str = str + "do statement start";
      return str;
  }
  str = str + "do statemtnt finished";
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
   on fail ErrorTypeA e {
      str += "-> Error caught : ";
      str = str.concat(e.message());
   }
   str += "-> Execution continues...";
   return str;
}
