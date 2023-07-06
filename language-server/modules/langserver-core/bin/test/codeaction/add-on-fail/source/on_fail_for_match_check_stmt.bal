function testFunction(any t) {
   match t {
        _ => { int i = check checkError(); }
   }
}

function checkError() returns int|error {
    return error("Test Error");
}
