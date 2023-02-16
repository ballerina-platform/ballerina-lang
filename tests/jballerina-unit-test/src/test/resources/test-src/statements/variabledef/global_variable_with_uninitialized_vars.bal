int x;
int y = foo();

function init() {
    x = 2;
}

function foo() returns int => x;