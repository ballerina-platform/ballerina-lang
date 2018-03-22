
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


function testMatchStatementBasics2() returns (string) {
    return openFile(openFileSuccess);
}

function testMatchStatementBasics3() returns (string) {
    return openFile(openFileFailure);
}

function openFile(function (string) returns(File | error) fp) returns (string) {

    File | error k = fp("/tmp/foo.txt");

    match k {
        File f =>  {
                        int a = 10;
                        return "file open success";
                   }
        error e =>      return "file open error: " + e.message;
    }

}

function openFileFailure(string path) returns (File | error) {
    error e = {message: "file not found: " + path};
    return e;
}

function openFileSuccess(string path) returns (File | error) {
    File f = {};
    return f;
}

struct File {
    string path;
}