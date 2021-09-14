type ErrorDesc record {|
    int code;
    string stack;
|}

type Error1 error<ErrorDesc>;

public function func1() {
    Error1 err1 = error Error1("error message", code = 10,)
}
//Create named arg context. 
//Crete an abstract context for invokable contexts. 
