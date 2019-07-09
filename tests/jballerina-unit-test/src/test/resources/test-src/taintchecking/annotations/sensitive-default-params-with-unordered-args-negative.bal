public function main(string... args) {
    testFunctionWithDefaultableParamsOnly(defaultableInput2 = args[0]);
    testFunctionRequiredParamAndDefaultableParams("static", defaultableInput2 = args[0]);
}

function testFunctionWithDefaultableParamsOnly(string defaultableInput1 = "x",
                                               @untainted string defaultableInput2 = "y") {

}


function testFunctionRequiredParamAndDefaultableParams(string requiredParam, string defaultableInput1 = "x",
                                                       @untainted string defaultableInput2 = "y") {

}
