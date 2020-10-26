import ballerina/runtime;

function errorConstructReasonTest() returns [error, error, error, string, any, string] {
    error er1 = error("error1");

    string s = "error2";
    error er2 = error(s);

    map<string> m = { k1: "error3" };
    error er3 = error(m.get("k1"));

    return [er1, er2, er3, er1.message(), er2.message(), er3.message()];
}

function errorConstructDetailTest() returns [error, error, error, any, any, any] {
    error er1 = error("error1", message = "msg1");

    string s = "error2";
    map<anydata> m2 = { message: "msg2" };
    anydata msg2 = m2.get("message");
    error er2 = error(s, message = <string>m2.get("message"));

    map<string> reason = { k1: "error3" };
    map<string> details = { message: "msg3" };
    error er3 = error(reason.get("k1"), message = details.get("message"));

    return [er1, er2, er3, er1.detail(), er2.detail(), er3.detail()];
}

function errorPanicTest(int i) returns string {
    string val = errorPanicCallee(i);
    return val;
}

function errorPanicCallee(int i) returns string {
    if (i > 10) {
        error err = error("largeNumber", message = "large number");
        panic err;
    }
    return "done";
}

function errorTrapTest(int i) returns string|error {
    string|error val = trap errorPanicCallee(i);
    return val;
}

type TrxErrorData record {|
    string message = "";
    error cause?;
    string data = "";
|};

type TrxError error<TrxErrorData>;

type TrxErrorData2 record {|
    string message = "";
    error cause?;
    map<string> data = {};
|};

public function testCustomErrorDetails() returns error {
    TrxError err = TrxError("trxErr", data = "test");
    return err;
}

public function testCustomErrorDetails2() returns string {
    TrxError err = TrxError("trxErr", data = "test");
    TrxErrorData errorData = err.detail();
    return errorData.data;
}

public function testErrorWithErrorConstructor() returns string {
    TrxError e = TrxError("trxErr", data = "test");
    error<TrxErrorData> err = e;
    TrxErrorData errorData = err.detail();
    return errorData.data;
}

function testTrapWithSuccessScenario() returns int {
    var a = trap retIntSuccess();
    if(a is int) {
        return a;
    } else {
        panic a;
    }
}

function retIntSuccess() returns int {
    return 1;
}

//function getCallStackTest() returns runtime:CallStackElement[] {
//    return runtime:getCallStack();
//}

function testConsecutiveTraps() returns [string, string] {
    error? e1 = trap generatePanic();
    error? e2 = trap generatePanic();
    if e1 is error {
        if e2 is error {
            return [e1.message(), e2.message()];
        }
    }
    return ["Failed", "Failed"];
}

function generatePanic() {
    error e = error("Error");
    panic e;
}

function testOneLinePanic() returns string[] {
    error? error1 = trap panicWithReasonOnly();
    error? error2 = trap panicWithReasonAndDetailMap();
    error? error3 = trap panicWithReasonAndDetailRecord();
    string[] results = [];
    if error1 is error {
        results[0] = error1.message();
    }

    if error2 is error {
        results[1] = error2.message();
        var detail = error2.detail();
        results[2] = <string>detail.get("msg");
    }

    if error3 is error {
        results[3] = error3.message();
        var detail = error3.detail();
        results[4] = <string>detail.get("message");
        results[5] = detail.get("statusCode").toString();
    }

    return results;
}

function panicWithReasonOnly() {
    panic error("Error1");
}

function panicWithReasonAndDetailMap() {
    panic error("Error2", msg = "Something Went Wrong");
}

type DetailRec record {
    string message;
    int statusCode;
};

function panicWithReasonAndDetailRecord() {
    panic error("Error3", message = "Something Went Wrong", statusCode = 1);
}

function testGenericErrorWithDetailRecord() returns boolean {
    string reason = "error reason 1";
    string detailMessage = "Something Went Wrong";
    int detailStatusCode = 123;
    error e = error(reason, message = detailMessage, statusCode = detailStatusCode);
    string errReason = e.message();
    map<anydata|readonly> errDetail = e.detail();
    return errReason == reason && <string> errDetail.get("message") == detailMessage &&
            <int> errDetail.get("statusCode") == detailStatusCode;
}

type ErrorReasons "reason one"|"reason two";

const ERROR_REASON_ONE = "reason one";
const ERROR_REASON_TWO = "reason two";

type UserDefErrorOne error;
type UserDefErrorTwo error<TrxErrorData>;

function testErrorConstrWithConstForUserDefinedReasonType() returns error {
    UserDefErrorOne e = error(ERROR_REASON_ONE);
    return e;
}

function testErrorConstrWithLiteralForUserDefinedReasonType() returns error {
    UserDefErrorOne e = error("reason one");
    return e;
}

function testErrorConstrWithConstForConstReason() returns error {
    UserDefErrorTwo e = UserDefErrorTwo(ERROR_REASON_ONE, message = "error detail message");
    return e;
}

function testErrorConstrWithConstLiteralForConstReason() returns error {
    UserDefErrorTwo e = UserDefErrorTwo("reason one", message = "error detail message");
    return e;
}

//type MyError error<string, map<MyError>>;
//
//function testCustomErrorWithMappingOfSelf() returns boolean {
//    MyError e1 = error(ERROR_REASON_ONE);
//    MyError e2 = error(ERROR_REASON_TWO, e1);
//
//    boolean errOneInitSuccesful = e1.message() == ERROR_REASON_ONE && e1.detail().length() == 0;
//    boolean errTwoInitSuccesful = e2.message() == ERROR_REASON_TWO && e2.detail().length() == 1 &&
//                e2.detail().err.message() == ERROR_REASON_ONE && e2.detail().err.detail().length() == 0;
//    return errOneInitSuccesful && errTwoInitSuccesful;
//}

function testUnspecifiedErrorDetailFrozenness() returns boolean {
    error e1 = error("reason 1");
    map<anydata|readonly> m1 = e1.detail();
    error? err = trap addValueToMap(m1, "k", 1);
    return err is error && err.message() == "{ballerina/lang.map}InvalidUpdate";
}

function addValueToMap(map<anydata|readonly> m, string key, anydata|readonly value) {
    m[key] = value;
}

public function testRuntimeFailingWhenAssigningErrorToAny() {
    map<any> m1 = { one: "a", two: "b" };
    error errValOne = error("error reason one");

    insertMemberToMap(m1, "three", errValOne); // panic
}

public function insertMemberToMap(map<any|error> mapVal, string index, any|error member) {
    mapVal[index] = member;
}

const string reasonA = "ErrNo-1";
type UserDefErrorTwoA error<TrxErrorData2>;

const string reasonB = "ErrorNo-2";
type UserDefErrorTwoB error<TrxErrorData>;
public function errorReasonSubType() returns [error, error, error, error] {
    UserDefErrorTwoB er_rA = UserDefErrorTwoB(reasonA);
    UserDefErrorTwoB er_rB = UserDefErrorTwoB(reasonB);
    UserDefErrorTwoB er_aALit = UserDefErrorTwoB("ErrNo-1");
    UserDefErrorTwoB er_aBLit = UserDefErrorTwoB("ErrorNo-2");
    return [er_rA, er_rB, er_aALit, er_aBLit];
}

function testIndirectErrorConstructor() returns [UserDefErrorTwoA, UserDefErrorTwoA, error, error] {
    var e0 = UserDefErrorTwoA("arg");
    UserDefErrorTwoA e1 = UserDefErrorTwoA("arg");
    return [e0, e1, e0, e1];
}

type Detail record {
    int code;
    string message?;
    error cause?;
};

const FOO = "foo";

type FooError error<Detail>;

public function indirectErrorCtor() returns [string, boolean, error] {
    error e = FooError(FOO, code = 3456);
    return [e.message(), e is FooError, e];
}

const F = "Foo";
const G = "Foo";

type E1 error<record { string message?; error cause?;}>;
type E2 error<record { string message?; error cause?;}>;
type E E1|E2;

public function testUnionLhsWithIndirectErrorRhs() returns error {
    E x = E1(F);
    return x;
}

public function testOptionalErrorReturn() returns error? {
    return error("this is broken", message = "too bad");
}

public function testStackTraceInNative() {
    string[] array = ["apple", "orange"];
    _ = array.slice(1, 4);
}

const C1 = "x";
const C2 = "y";

type C1E error<record {| string message?; error cause?; |}>;
type C2E error<record {| string message?; error cause?; int code; |}>;

public function testPanicOnErrorUnion(int i) returns string {
    var res = testFunc(i);
    if (res is string) {
        return res;
    } else {
        panic res;
    }
}

function testFunc(int i) returns string|E1|E2 { // fails even if one of the errors is `error`
    if (i == 0) {
        return "str";
    } else if (i == 1) {
        return C1E(C1);
    }
    return C2E(C2, code=4);
}

public function testIndirectErrorReturn() returns E1|E2|string {
    return E1(F, message = "error msg");
}

const A1 = "a1";
const B1 = "b1";

type A error;
type B error;

type MyError A|B;

public function testErrorUnionPassedToErrorParam() returns string {
    A aErr = error(A1, one = 1);
    MyError eErr = aErr;
    return takeError(eErr);
}

function takeError(error e) returns string {
    return e.message();
}

function testErrorTrapVarReuse() returns [error?, error?] {
    var result = trap panicNow();
    var temp = result;
    result = trap dontPanic();
    return [temp, result];
}

function panicNow() {
    panic error("panic now");
}

function dontPanic() {
}

function bar(){
    bar2();
}

function bar2(){
    bar();
}

function testStackOverFlow() returns [runtime:CallStackElement[], string]? {
    error? e = trap bar();
    if (e is error){
        return [e.stackTrace().callStack, e.message()];
    }
}

function testErrorTypeDescriptionInferring() {
    TrxError e = TrxError("IAmAInferedErr");
    error<*> err = e;
    TrxError errSecondRef = err;
    assertEquality(errSecondRef.detail().toString(), e.detail().toString());
}

function testDefaultErrorTypeDescriptionInferring() {
    error e = error("IAmAInferedDefaultErr");
    error<*> err = e;
    assertEquality(err.detail().toString(), e.detail().toString());
}

function testUnionErrorTypeDescriptionInferring() {
    error|TrxError e = error("IAmAInferedUnionErr");
    error<*> err = e;
    assertEquality(err.detail().toString(), e.detail().toString());
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error actual, any|error expected) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
