$$$imports$$$
$$$declarations$$$
@test:BeforeSuite
function beforeTest(){
    log:printInfo("Running Test setup in beforeTest function");
}

$$$content$$$

@test:AfterSuite
function afterTest(){
    log:printInfo("Running Test setup in afterTest function");
}
