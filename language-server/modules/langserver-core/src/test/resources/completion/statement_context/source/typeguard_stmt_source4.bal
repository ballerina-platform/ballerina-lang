public function testFunction(){
    myFunc(5).
}

function myFunc(int arg) returns int|string {
    if arg > 0 {
        return arg;
    } else {
        return "invalid";
    }
}
