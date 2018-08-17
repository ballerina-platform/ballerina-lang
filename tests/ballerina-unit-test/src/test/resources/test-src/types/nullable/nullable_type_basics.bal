import ballerina/io;

function funcReturnNil() {
}

function testNullableTypeBasics1() returns int? {
    int? i = funcReturnNil();
    i = ();
    i = 5;
    return i;
}

function testNullableTypeBasics2() returns int? {
    int? i = funcReturnNil();
    return i;
}

function testNullableArrayTypes1() returns any {
    float?[] fa = [1.0, 5, 3, ()];
    float? f = fa[0];
    return f;
}

//struct person {
//    int age;
//    address? addr;
//    string[] names;
//}

//struct address {
//    string name = "52 skytop";
//    int no = 10;
//    subject? sbjs;
//}
//
//struct subject {
//    string name;
//    int id;
//}

//function testNullableTypesInStructs() {
//    person? p1;
//    person p2 = {age:12};
//    io:println(p2.addr);


    //person p2 = {age:12, addr: {name:"ddd", sbjs:null}, names:["ss", "d"]};

    //match p {
    //    person k => io:println(k);
    //    any | null => io:println("null here");
    //}

    //io:println(p.addr);
//}