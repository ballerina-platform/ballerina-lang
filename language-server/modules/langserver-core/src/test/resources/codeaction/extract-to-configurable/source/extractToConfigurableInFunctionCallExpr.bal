function doSomething(string arg1, string arg2, string arg3, string... arg4) {

}

function call() {
    doSomething("arg1", "arg2", "arg3", "arg4");
    doSomething("arg1", arg2 = "arg2", arg3 = "arg3");
}
