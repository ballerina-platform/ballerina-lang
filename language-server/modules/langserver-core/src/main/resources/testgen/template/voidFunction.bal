@test:Config {
    dataProvider: "${testFunctionName}DataProvider"
}
function ${testFunctionName}(${testFunctionParams}) {
${actual}
}

function ${testFunctionName}DataProvider() returns (${dataProviderReturnType}) {
    return ${dataProviderReturnValue};
}
