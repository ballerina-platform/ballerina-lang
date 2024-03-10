import ballerina/io;

function loop(int times) {
    int count = 1;
    while (count <= times) { count += 1;}
}

function getSign(int number) returns string {
    if number <= 0 {return "+";}
    else {return "-";}
}

function btoi(boolean? b) returns int {
    match b {
        true => {return 1;}
        false => {return 0;}
    }
    return 0;
}

function printFalse() {
    boolean b = false;
    if b {
        io:println(1);
    } else {
        io:println(0);
    }
}

function hello() returns string {return "hello";}
