import project.module2;

function testExternalModuleWithOneImport() {
    _ = module2:isIntArray(module1:function1());
}
