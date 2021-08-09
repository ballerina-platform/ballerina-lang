import ballerina/jballerina.java;


type TrxError error<TrxErrorData>;

type TrxErrorData record {|
    string message?;
    error cause?;
    string data = "";
|};

public function main() {
    worker w1 {
        int i = 2;
        TrxError? success = i ->> w2;
        println("w1");
    }

    worker w2 returns boolean|error {
        int j = 25;
        if (0 > 1) {
            return error("trxErr", data = "test");
        }
        j = <- w1;
        println(j);
        return true;
    }
}

public function println(any|error... values) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;
