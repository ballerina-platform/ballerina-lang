function test() {
    transaction {
        doSomething();
        check commit;
    }
}

function doSomething() {

}
