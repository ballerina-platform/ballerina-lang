function foo() {
    int a = module: varName;
    int a = module :varName;
    int a = module : varName;
    int a = module:
    varName;
    int a = module:// some comment
    varName;
    int a = module:	varName; // contains tab
    int b = module : func();
    var c = {fieldName: b : c};
}
