import testorg/foo;

function returnDifferentObectInit() returns foo:Apartment {
    return new (5, 7);
}

function returnDifferentObectInit1() returns foo:Apartment | () {
    return new (5);
}

function returnDifferentObectInit2() {
    foo:Apartment | () person = new (5);
    var person1 = new (5);
    error person2 = new (5);
}


