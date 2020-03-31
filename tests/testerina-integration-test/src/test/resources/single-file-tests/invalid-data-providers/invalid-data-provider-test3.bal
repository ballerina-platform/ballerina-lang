import ballerina/test;

// tests an invalid tuple data provider
@test:Config{
    dataProvider:"invalidDataGen3"
}
function testInvalidTupleDataProvider ([string, string, [string, string]] result) {

   // int|error fErr = trap int.constructFrom(result[0]);
   // int|error sErr = trap int.constructFrom(result[1]);
   // int|error resultErr = trap int.constructFrom(result);
   test:assertTrue(true);
}

function invalidDataGen3() returns ([string, int, [int, int]][]) {
    return [["hi", 20, [30, 30]]];
}
