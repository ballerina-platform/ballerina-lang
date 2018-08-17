function main(string... args) {
    testFunctionWithDefaultableParamsOnly(defaultableInput2 = args[0]);
    testFunctionRequiredParamAndDefaultableParams("static", defaultableInput2 = args[0]);
}

function testFunctionWithDefaultableParamsOnly(@sensitive string defaultableInput1 = "x",
                                               string defaultableInput2 = "y") {

}


function testFunctionRequiredParamAndDefaultableParams(string requiredParam, @sensitive string defaultableInput1 = "x",
                                                       string defaultableInput2 = "y") {

}
