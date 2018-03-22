

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


struct MyFile {
    string path;
    int bytes;
}

function testMatchStatementBasics4() returns (string) {
    any a = "This is working";

    match a {
        string s => return s;
        int i => return "int";
        any m => return "any";
    }
}

function testMatchStatementBasics5() returns (string) {
    MyFile f = {};
    match f {
        File ff => return "file is matched";
    }
}

function testMatchStatementBasics6() returns (string) {
    File f = {};
    match f {
        MyFile ff => return "file is matched";
        any ff => return "File is matched";
    }
}



function testMatchStatementBasics7() returns (string | int | boolean) {
    any a = "dddd";

    match a {
        int e => return 1;
        string k => return "found string";
        any k => return "found any";
    }
}

function testMatchStatementBasics8() returns (string | int | boolean) {
    any a = null;

    match a {
        int e => return 1;
        string | null  k => return "string | null matched";
        any k => return "found any";
    }
}

function testMatchStatementBasics9(json aa) returns (string | int | boolean) {
    any a = aa;
    match a {
        int | null e => return "json int| null matched";
        string | boolean k => return "json string | boolean matched";
        json  k => return "json matched";
        any k => return "found any";
    }
}

function testMatchStatementBasics11() returns (string | int | boolean) {

    string | int | boolean | null a = 5;

    match a {
        int | null e => return "int| null matched";
        string | boolean k => return "string | boolean matched";
    }
}

function testMatchStatementBasics12() returns (string | int | boolean) {

    string | int | boolean | null a = false;

    match a {
        int | null e => return "int| null matched";
        string | boolean k => return "string | boolean matched";
    }
}

function testMatchStatementBasics13() returns (string | int | boolean) {

    string | int | boolean | null a = null;

    match a {
        int  e => return "int matched";
        string | boolean k => return "string | boolean matched";
        any => return "any matched";
    }
}

function testMatchStatementBasics14(json a) returns (string | int | boolean) {
    match a {
        int | null e => return "int| null matched";
        string | boolean k => return "json string | boolean matched";
        json  k => return "json matched";
    }
}


struct Human {
    string name;
    function (int, string) returns string | null foo;
}

struct Man {
    string name;
    function (int, string) returns string | null foo;
    int age;
}

struct Woman {
    string name;
    function (int, string) returns string | null foo;
    int age;
    string color;
}


function testMatchStatementBasics16() returns (string | int | boolean) {

     Human m = {name:"Piyal"};

    match m {
        Man r => return r.name;
        Human h => return h.name;
    }
}



