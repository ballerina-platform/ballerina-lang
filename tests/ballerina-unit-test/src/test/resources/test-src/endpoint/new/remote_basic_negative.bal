// remote modifier not allowed in non-object attached functions
remote function test1(string value) {
}

// remote modifier not allowed in non-object attached functions
public remote function test2(string value) {
}

// remote modifier not allowed in non-object attached functions
public remote extern function test3(string value) returns int;


type Foo object {

    function abc (string value) returns int;

    remote function pqr(string value) returns boolean {
        return false;
    }

    remote function xyz (string value) returns float;
};

// attempt to refer non-remote function as remote
remote function Foo::abc (string value) returns int {
    return 10;
}

// remote modifier required here
function Foo::xyz (string value) returns float {
    return 10.0;
}

function testFunc1() {
    Foo x = new;
    // invalid remote function invocation syntax, use '->' operator
    var y = x.pqr("test");
    // undefined remote function 'abc' in endpoint Foo
    var z = x->abc("test");
}


function testFunc2() {
    // Testing other type invocation.
    XXX x = new;
    // invalid remote function invocation, expected an endpoint
    var y = x->foo();

    map m = {};
    // invalid remote function invocation, expected an endpoint
    _ = m->keys();

    Bar b = new;
    // invalid remote function invocation, expected an endpoint
    _ = b->action1("value");
}

type Bar object {
   function action1 (string x) returns int {
       return 10;
   }
};

function testFunc3() {
    Bar b = new;
    int i = 10;
    if(i > 5) {
        // endpoint declaration not allowed here, declare at the top of a function or at module level
        Foo x = new;
        var y = x->pqr("test");
    }
}

function testFunc4() {
    worker w1{
        // endpoint declaration not allowed here, declare at the top of a function or at module level
        Foo x = new;
        var y = x->pqr("test");
    }
    worker w2 {
        int i = 10;
    }
}

// valid.
Foo gep = new;

function testFunc5() {
    var y = gep->pqr("test");

    // invalid remote function invocation syntax, use '->' operator
    var z = gep.pqr("test");
}


function testFunc6(Foo ep, string b) {
    var y = ep->pqr("test");

    // invalid remote function invocation syntax, use '->' operator
    var z = ep.pqr("test");
}

function testFunc7 (string s) {
    string a = "abc";
    // endpoint declaration not allowed here, declare at the top of a function or at module level
    Foo ep = new;
    var y = ep->pqr("test");
}

function testFunc8 (string s) {
    Foo ep;
    string a = "abc";
    // endpoint declaration not allowed here, declare at the top of a function or at module level
    Foo ep2 = new;
    var y = ep2->pqr("test");
}

function testFunc9 (string s) {
    Foo ep;
    string a = "abc";
    // Uninitilized variable
    var y = ep->pqr("test");
}
