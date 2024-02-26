
function testSubModuleWithNoImports() {
   _ = module2:isIntArray(array:size());
}

function testExternalModuleWithNoImports() {
   _ = module2:isIntArray(module1:function1());
}
