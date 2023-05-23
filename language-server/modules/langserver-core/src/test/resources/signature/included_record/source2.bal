
public type KeyValues record {|
    never msg?;
    never 'error?;
    never stackTrace?;
    Value...;
|};

public isolated function printInfo(string msg, error? 'error = (), error:StackFrame[]? stackTrace = (), *KeyValues keyValues) {

}

public type Value anydata|Valuer;

public type Valuer isolated function () returns anydata;

public function main() {
    printInfo();
}
