function testFunction() returns error? {
    transaction {
        doSomething(1);
        check commit;
    }
}

function doSomething(int a) {

}
