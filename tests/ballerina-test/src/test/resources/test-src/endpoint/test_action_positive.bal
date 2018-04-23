
type DummyEndpoint object {
    public function init ({} conf) {
    }

    public function getCallerActions () returns (DummyClient) {
        return new;
    }
};

type DummyClient object {
    public function invoke1 (string a) returns error? {
        error e = {message : "i1"};
        return e;
    }

    public function invoke2 (int a) returns string|int|boolean {
        if(a == 1){
            return "string";
        } else if (a == 2) {
            return 10;
        }
        return false;
    }

    public function invoke3 (int a) returns string? {
        if(a == 1){
            return "string";
        }
        return ();
    }
};

endpoint DummyEndpoint ep {};

function testCheck () returns error? {
    var a = testCheckFunction();
    return a;
}

function testCheckFunction () returns error?{
    check ep -> invoke1("foo");
    return ();
}

string s6;
string s7;
function testBut() returns (string, string, string, string, string, string, string){
    string s1 = ep -> invoke2(1) but { int => "int", boolean => "boolean" };
    string s2 = ep -> invoke2(2) but { int => "int", boolean => "boolean" };
    string s3 = ep -> invoke2(10) but { int => "int", boolean => "boolean" };
    string s4 = ep -> invoke3(1) ?: "elvis";
    string s5 = ep -> invoke3(2) ?: "elvis";
    ep -> invoke3(1) but { string s => assignS6(s)};
    ep -> invoke3(2) but { string s => assignS7(s)};
    return (s1,s2,s3,s4,s5,s6,s7);
}

function assignS6(string s){
    s6 = s + "6";
}
function assignS7(string s){
s7 = s + "6";
}