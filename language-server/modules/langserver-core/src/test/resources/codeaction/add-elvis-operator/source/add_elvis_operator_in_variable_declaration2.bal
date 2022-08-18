function testVarDecl() returns error?{
    int|string myVal = check getInt();
}

function getInt() returns int? | error {

}
