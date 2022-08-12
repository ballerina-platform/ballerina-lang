function foo() {
    var b = a->/;

    var b = a->/games;
    var b = a->/[pathSegment];
    b = a->/games/[game]/details;
    _ = a->/[...restSegment];
    b += a->/games/[...restSegment];

    var c = a->/.get;
    _ = a->/();
    b = a->/(arg1, arg2);
    c = a->/.post(arg1, arg2);

    match a->/games {
        _ => {
        }
    }

    match a->/[pathSegment] {
        _ => {
        }
    }

    match a->/.post(arg1, arg2) {
        _ => {
        }
    }

    foreach var item in a->/games {
    }

    foreach var item in a->/[...restSegment] {
    }

    foreach var item in a->/(arg1, arg2) {
    }
}
