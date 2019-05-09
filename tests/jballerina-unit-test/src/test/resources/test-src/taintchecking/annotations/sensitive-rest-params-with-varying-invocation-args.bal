public function main(string... args) {
    testFunctionWithRestParamsOnly("static", args[0]);
    testFunctionWithRequiredParamAndRestParams("static", "static", args[0]);
    testFunctionWithRequiredDefaultableAndRestParams("static", "optionalStatic", args[0]);
}

function testFunctionWithRestParamsOnly(string... restParams) {

}

function testFunctionWithRequiredParamAndRestParams(string requiredParam, string... restParams) {

}

function testFunctionWithRequiredDefaultableAndRestParams(string requiredValue, string? optionalValue,
string defaultableValue = "x", string... restParams) {

}
