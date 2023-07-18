type ErrorTypeA distinct error;

const TYPE_A_ERROR_REASON = "TypeA_Error";

type ErrorTypeB distinct error;

const TYPE_B_ERROR_REASON = "TypeB_Error";

function testIncompatibleErrorTypeOnFail () returns string {
   string str = "";
   retry(3) {
     str += "Before failure throw";
     fail error ErrorTypeA(TYPE_A_ERROR_REASON, message = "Error Type A");
   } on fail ErrorTypeB e {
      str += "-> Error caught ! ";
   }
   str += "-> Execution continues...";
   return str;
}

function testOnFailWithUnion () returns string {
   string str = "";
   var getTypeAError = function () returns int|ErrorTypeA{
       ErrorTypeA errorA = error ErrorTypeA(TYPE_A_ERROR_REASON, message = "Error Type A");
       return errorA;
   };
   var getTypeBError = function () returns int|ErrorTypeB{
       ErrorTypeB errorB = error ErrorTypeB(TYPE_B_ERROR_REASON, message = "Error Type B");
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

type SampleErrorData record {|
    int code;
    string reason;
|};

type SampleError error<SampleErrorData>;

type SampleComplexErrorData record {|
    int code;
    int[2] pos;
    record {string moreInfo;} infoDetails;
|};

type SampleComplexError error<SampleComplexErrorData>;

function testOnFailWithMultipleErrors() {
    boolean isPositiveState = false;
    retry(3) {
        if isPositiveState {
            fail error SampleComplexError("Transaction Failure", code = 20, pos = [30, 45], infoDetails = {moreInfo: "deadlock condition"});
        }
        fail error SampleError("Transaction Failure", code = 50, reason = "deadlock condition");
    } on fail var error(msg) {
    }
}
