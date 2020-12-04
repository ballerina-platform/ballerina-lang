function foo() {
    error(a b) = "hello";
    error(a b = "hello";
    error T(a b) = "hello";

    [x error(a b] = "hello";
    [x, error T(a b] = "hello";

    error(a error(b, c d=error(e f))) = "hello";
    error T(a, error(b c), d=T(e, f)) = "hello";
    error(a,) = "simpleError";
    error(a, b,) = "simpleError";
    error(a, = bindingPattern) = "simpleError";
    error(a ...) = "simpleError";

    {x: error(a b} = "hello";
    {x: error T(a, b} = "hello";

    error(error(a,b)) = "simpleError";
    error([a,b,c])  = "simpleError";
    error(a, b = {filedname: BP}, error(a,b)) = "simpleError";
    error(a, ...restBP, b = {filedname: BP}) = "simpleError";
    error({a:b}, error(a,b)) = "simpleError";
    error(simpleBP1, simpleBP2, simpleBP3) = "simpleError";
    error(simpleBP1, simpleBP2, error(a,b)) = "simpleError";

    error(a, ... b, ... c) = "simpleError";
    error(a, ... b, c = d, ... e) = "simpleError";
}
