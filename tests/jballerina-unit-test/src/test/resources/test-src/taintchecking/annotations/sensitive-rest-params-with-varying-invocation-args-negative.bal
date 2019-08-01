public function main(string... args) {
    testFunctionWithRestParamsOnly("static", args[0]);
    testFunctionWithRequiredParamAndRestParams("static", "static", args[0]);
    testFunctionWithRequiredDefaultableAndRestParams("static", "optionalStatic", "x", args[0]);
}

function testFunctionWithRestParamsOnly(@untainted string... restParams) {

}

function testFunctionWithRequiredParamAndRestParams(string requiredParam, @untainted string... restParams) {

}

function testFunctionWithRequiredDefaultableAndRestParams(string requiredValue, string? optionalValue,
string defaultableValue = "x", @untainted string... restParams) {

}
