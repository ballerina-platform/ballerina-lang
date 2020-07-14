function foo () {
    match bar {
        error() => {}
        identifier() => {}
        identifier:identifier() => {}
        typeref([a, b], {a:b, c: var a}, var c, _ ) => {}
        typeref(namedarg1 = mathchpattren, namedarg2 = typeref({a:b, c: var a}, d), namedarg3 = [a, b, c, {}]) => {}
        typeref(... var e) => {}
        error({a:[], c:{}, ... var e}) => {}
        typeref(positionalarg, {}, namedarg1 = mathchpattren, namedarg2 = typeref([a, b], {a:b, c: var a}, d)) => {}
        typeref:identifier(a, b, [a1, _ ], ... var e) => {}
        error(namedarg1 = mathchpattren, namedarg2 = typeref({a:b, c: var a}, d), ... var varname) => {}
        typeref(a, [b], _, namedarg1 = mathchpattren, namedarg2 = error({a:b, c: var a}, d), ... var varname) => {}
    }
}
