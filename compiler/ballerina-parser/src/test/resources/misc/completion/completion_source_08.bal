function foo1() {
    wait
}

function foo2() {
    wait {a: }
    wait {a, }
    wait b |
}

function foo3() {
    start
}

function foo4() {
    a ->
}

function foo5() {
    a ->>
}

function foo6() {
    <-
}

function foo7() {
    <- {
        a:     }
    }

function foo8() {
    flush
}

function foo9() {
    from
     }

function bar() returns int {
    return 12;
}
