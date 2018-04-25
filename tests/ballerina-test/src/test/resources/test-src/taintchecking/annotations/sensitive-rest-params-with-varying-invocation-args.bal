function main(string... a) {
    testFunctionWithRestParamsOnly("static", a[0]);
    testFunctionWithRequiredParamAndRestParams("static", "static", a[0]);
}

function testFunctionWithRestParamsOnly(string... restParams) {

}

function testFunctionWithRequiredParamAndRestParams(string requiredParam, string... restParams) {

}
