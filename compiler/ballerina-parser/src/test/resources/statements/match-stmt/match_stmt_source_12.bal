function foo () {
    match bar {
        error() => {}
        error identifier() => {}
        error identifier:identifier() => {}
        error typeref(var c, _ ) => {}
        error (namedarg1 = mathchpattren, namedarg2 = error typeref(d), namedarg3 = [a, b, c, {}]) => {}
        error typeref(... var e) => {}
        error ("Bad error", namedarg3 = mathchpattren3) => {}
        error typeref(errorMsg, error typeref(d), namedarg1 = mathchpattren, ... var d) => {}
        error (_, +5, ... var e) => {}
        error (true, 1.25) => {}
        error (0xf5, error typeref(... var e), namedarg1 = mathchpattren, ... var varname) => {}
    }
}
