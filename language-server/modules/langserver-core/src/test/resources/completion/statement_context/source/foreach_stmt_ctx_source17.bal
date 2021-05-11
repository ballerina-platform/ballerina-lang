public function testFunction() {
    map<MyType> myMap = {
        attr1: {myVar1:"hello1", myVar2:2},
        attr2: {myVar1:"hello2", myVar2:3}
    };
    myMap.
}

type MyType record {|
    string myVar1;
    int myVar2;
|};
