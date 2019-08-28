
type TrxErrorData record {|
    string message = "";
    error cause?;
    string data = "";
|};

type TrxErrorData2 record {|
    string message = "";
    error cause?;
    map<string> data = {};
|};

const string reasonA = "ErrNo-1";
type UserDefErrorTwoA error<reasonA, TrxErrorData2>;

const string reasonB = "ErrorNo-2";
type UserDefErrorTwoB error<reasonA|reasonB, TrxErrorData>;

public function testErrorUnion() {
    UserDefErrorTwoB er_aALit = error("ErrNo-1");

    UserDefErrorTwoB|UserDefErrorTwoA errUnion = er_aALit;
    errUnion.
}