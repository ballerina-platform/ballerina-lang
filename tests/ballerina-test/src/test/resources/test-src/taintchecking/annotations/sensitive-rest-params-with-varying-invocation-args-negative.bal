function main(string... a) {
    testFunctionWithRestParamsOnly("static", a[0]);
    testFunctionWithRequiredParamAndRestParams("static", "static", a[0]);
}

function testFunctionWithRestParamsOnly(@sensitive string... restParams) {

}

function testFunctionWithRequiredParamAndRestParams(string requiredParam, @sensitive string... restParams) {

}
