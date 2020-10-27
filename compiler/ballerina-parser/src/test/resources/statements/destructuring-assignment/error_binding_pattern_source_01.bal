function foo() {
    error() = "myErrorValue";
    error(errormessage1) = "sample_error1";
    error typeref(errormessage2) = "sample_error2";
    error(_) = "sample_error3";
    error(_, errorcause1) = "sample_error4";
    error(_, error(errormessage3, errorcause2)) = "sample_error5";
    error(a, b) = "hello1";
    error(a, b = c, ...d) = "hello2";
    error(a, b = c) = "hello3";
    error(b = c) = "hello4";
    error(a, ...d) = "hello5";
    error(...restBindingPattern) = "hello6";
    error(errormessage4, errorcause3, ...d) = "hello7";
    error(a, b = [bindingPattern, error(a)], ...d) = "hello8";
}
