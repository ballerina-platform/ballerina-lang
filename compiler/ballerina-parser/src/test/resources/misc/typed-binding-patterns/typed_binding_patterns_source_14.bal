var { } = x;
var { a, b:c, ...d } = x;
var { ...d } = x;

function foo() {
    var { } = x;
    var { a, b:c, ...d } = x;
    var { ...d } = x;
}
