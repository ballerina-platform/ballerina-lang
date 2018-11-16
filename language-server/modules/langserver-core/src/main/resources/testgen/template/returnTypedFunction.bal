@test:Config {
    dataProvider: "${testFunctionName}DataProvider"
}
function ${testFunctionName}(${testFunctionParams}) {
    ${actual}
    test:assertEquals(actual, expected, msg = "Return value should be equal to the expected value");
}

function ${testFunctionName}DataProvider() returns (${dataProviderReturnType}) {
    return ${dataProviderReturnValue};
}
