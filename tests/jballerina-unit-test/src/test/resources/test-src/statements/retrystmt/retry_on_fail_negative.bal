type ErrorTypeA distinct error;

const TYPE_A_ERROR_REASON = "TypeA_Error";

type ErrorTypeB distinct error;

const TYPE_B_ERROR_REASON = "TypeB_Error";

function trxError()  returns error {
    return error("TransactionError");
}

function testUnreachableAfterFail () returns string|error {
     string str = "";
     int count = 0;
     retry (3) {
           count = count+1;
           str += (" attempt " + count.toString() + ":error,");
           fail trxError();
           str += (" attempt "+ count.toString() + ":result returned end.");
     } on fail error e {
           return error("Custom Error");
     }
     return str;
}

function testIncompatibleErrorTypeOnFail () returns string {
   string str = "";
   retry(3) {
     str += "Before failure throw";
     fail ErrorTypeA(TYPE_A_ERROR_REASON, message = "Error Type A");
   } on fail ErrorTypeB e {
      str += "-> Error caught ! ";
   }
   str += "-> Execution continues...";
   return str;
}

function testIgnoreReturnInOnFail () returns string {
   string str = "";
   retry(3) {
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
   retry(3) {
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

function testNestedRetryWithLessOnFails () returns string {
   string str = "";
   retry(3) {
     str += "-> Before error 1 is thrown";
      retry(2) {
          str += " -> Before error 2 is thrown";
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
   retry(3) {
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
