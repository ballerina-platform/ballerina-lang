function foo () {
    match bar {
        [a, b] => {}
        [a, b, a:b, ...var c] => {}
        [...var c] => {}
        [var a, var _, "this is string", var [a, b, ...c], ...var c] => {}
        [a, _, [a, b, ...var c], ...var c] => {}
        [a, _, [a, [b], ...var c], ...var c] => {}
        [true, (), a, +5, "dulmina"] => {}
    }
}
