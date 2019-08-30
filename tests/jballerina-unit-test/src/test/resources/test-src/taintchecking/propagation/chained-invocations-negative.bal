public function main (string... args) {
    secureFunction(returnString(args[0]).trim(), returnString(args[0]).trim());
    Rec r = { field : "" };
    // <@untainted> does not prevent taintedness propagation out of a parameter
    recordTainter(<@untainted> r, args[0]);
    sensitiveRecUser(r);
}

public function returnString(string data) returns (string) {
    return data;
}

public function secureFunction (@untainted string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}

public type Rec record {
    string field;
};

public function recordTainter(Rec rec, string s) {
    sensitiveRecUser(rec);
    rec.field = s;
}

public function sensitiveRecUser(@untainted Rec rec) {

}