



function testMatchStmtNegative1 (string | int | boolean i)  returns (string | int | boolean) {

    match i {
        int k => return "int value received: " + k;
        string | boolean j => match j {
                                    string s => return "string value received: " + s;
                              }
    }
}

function testMatchStmtNegative2 (string | int i)  {

     match i {
        typedesc j => j = int;
        int k => k = 10;
        string j => j = "sss";
        boolean b => b = true;
     }
}