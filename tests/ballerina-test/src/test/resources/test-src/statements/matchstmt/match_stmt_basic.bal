

function testMatchStatementBasics1() returns (string | int | boolean) {
    string | int | boolean a1 =  bar(true);
    string | int | boolean a2 =  bar(1234);
    string | int | boolean a3 =  bar("man this is working!!!");
    return a1;
}

function bar (string | int | boolean i)  returns (string | int | boolean){
    match i {
        int k => return "int value received: " + k;
        string | boolean j => match j {
                                    string s => return "string value received: " + s;
                                    boolean b => return "boolean value received: " + b;
                              }
    }
}