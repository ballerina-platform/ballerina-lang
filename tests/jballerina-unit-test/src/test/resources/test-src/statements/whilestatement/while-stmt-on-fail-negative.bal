type ErrorTypeA distinct error;

const TYPE_A_ERROR_REASON = "TypeA_Error";

type ErrorTypeB distinct error;

const TYPE_B_ERROR_REASON = "TypeB_Error";

function testUnreachableAfterFail (int i) returns string {
   string str = "";
   int count = i;
   while (count < 5) {
     count += 1;
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

function testIncompatibleErrorTypeOnFail (int i) returns string {
   string str = "";
   int count = i;
   while (count < 5) {
     count += 1;
     str += "Before failure throw";
     fail ErrorTypeA(TYPE_A_ERROR_REASON, message = "Error Type A");
   }
   on fail ErrorTypeB e {
      str += "-> Error caught ! ";
   }
   str += "-> Execution continues...";
   return str;
}

function testIgnoreReturnInOnFail (int i) returns string {
   string str = "";
   int count = i;
   while (count < 5) {
     count += 1;
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

function testUnreachableInOnFail (int i) returns string {
   string str = "";
   int count = i;
   while (count < 5) {
     count += 1;
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

function testReturnWitihinDo(int i) returns string {
  string str = "";
  int count = i;
  while (count < 5) {
      count += 1;
      str = str + "do statement start";
      return str;
  }
  str = str + "do statemtnt finished";
}

function testOnFailWithUnion (int i) returns string {
   string str = "";
   var getTypeAError = function () returns int|ErrorTypeA{
       ErrorTypeA errorA = ErrorTypeA(TYPE_A_ERROR_REASON, message = "Error Type A");
       return errorA;
   };
   var getTypeBError = function () returns int|ErrorTypeB{
       ErrorTypeB errorB = ErrorTypeB(TYPE_B_ERROR_REASON, message = "Error Type B");
       return errorB;
   };
   int count = i;
   while (count < 5) {
     count += 1;
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

function testErrroDuplication() returns error? {
    string str = "";
    int i = 3;
    while (i > 2) {
        i -= 1;
        fail error("Custom Error");
        str += "-> unreachable";
    }
    str += "-> unreachable";
}
