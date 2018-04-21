import ballerina/reflect;

// Start types
type SimplePerson {
    string name;
    int age = -1;
    boolean married;
};

function testPrimitiveTypes() returns (boolean) {
    SimplePerson sp1 = {name: "Nick", age: 25, married: true};
    SimplePerson sp2 = {age: 25, married: true, name: "Nick"};

    SimplePerson spUnmatchString = {name: "Jack", age: 25, married: true};
    SimplePerson spUnmatchInt = {name: "Nick", age: 20, married: true};
    SimplePerson spUnmatchBoolean = {name: "Nick", age: 25, married: false};

    return reflect:equals(sp1,sp2) &&
           !reflect:equals(sp1,spUnmatchString) &&
           !reflect:equals(sp1,spUnmatchInt) &&
           !reflect:equals(sp1,spUnmatchBoolean) &&
           !reflect:equals(sp1,()) &&
           !reflect:equals((),sp1);
}

public type ArrayedPerson {
    string name;
    int age = -1;
    boolean married;
    string[] address;
};

function testTypesWithArrays() returns (boolean) {
    ArrayedPerson ap1 = {name: "Nick", married: true, address: ["20", "Palm Grove"]};
    ArrayedPerson ap2 = {name: "Nick", married: true, address: ["20", "Palm Grove"]};

    ArrayedPerson apUnmatch = {name: "Jack", married: true, address: ["20", "Duplication Road"]};
    ArrayedPerson apUnordered = {name: "Nick", married: true, address: ["Palm Grove", "20"]};

    return reflect:equals(ap1,ap2) &&
           !reflect:equals(ap1,apUnmatch) &&
           !reflect:equals(ap1,apUnordered) &&
           !reflect:equals(ap1,()) &&
           !reflect:equals((),ap1);
}

public type Wheel {
    int pressure;
};

public type Engine {
    string model;
    float capacity;
};

public type Car {
    string name;
    Wheel[] wheels;
    Engine engine;
};

public function testNestedTypes() returns (boolean) {
    Car c1 = {name: "Charger",
                 engine: {model: "v8", capacity: 2000},
                 wheels: [{pressure: 30}, {pressure: 31}, {pressure: 30}, {pressure: 29}]};
    Car c2 = {engine: {model: "v8", capacity: 2000.00},
                 name: "Charger",
                 wheels: [{pressure: 30}, {pressure: 31}, {pressure: 30}, {pressure: 29}]};
    Car c3Unmatch = {name: "Charger",
                        wheels: [{pressure: 30}, {pressure: 31}, {pressure: 30}, {pressure: 29}],
                        engine: {model: "v8", capacity: 1500}};
    Car c4Unmatch = {name: "Corvette",
                        wheels: [{pressure: 30}, {pressure: 31}, {pressure: 30}, {pressure: 29}],
                        engine: {model: "v8", capacity: 2000}};
    Car c5Unmatch = {name: "Charger",
                        engine: {model: "v8", capacity: 2000},
                        wheels: [{pressure: 29}, {pressure: 31}, {pressure: 28}, {pressure: 29}]};

    return reflect:equals(c1,c2) &&
           !reflect:equals(c1,c3Unmatch) &&
           !reflect:equals(c1,c4Unmatch) &&
           !reflect:equals(c1,c5Unmatch) &&
           !reflect:equals(c1,()) &&
           !reflect:equals((),c1);
}

// End types

// Start Array of Arrays

public function testArraysOfArrays() returns (boolean) {
    int[][] aa1 = [[1, 2, 3], [10, 20, 30], [5, 6, 7]];
    int[][] aa2 = [[1, 2, 3], [10, 20, 30], [5, 6, 7]];

    int[][] aaUnmatch = [[1, 2, 3], [10, 99, 30], [5, 6, 7]];

    return reflect:equals(aa1,aa2) &&
           !reflect:equals(aa1,aaUnmatch) &&
           !reflect:equals(aa1,()) &&
           !reflect:equals((),aa1);
}

// End Array of Arrays

// Start Maps

public function testMaps () returns (boolean) {
int[] a = [900, 2230];
int[] b = [2230, 900];
    map m1 = {line1:"No. 20", line2:"Palm Grove",
                 city:"Colombo 03", country:"Sri Lanka", checkinsTimes: a};
    map m2 = {line1:"No. 20", line2:"Palm Grove",
                 city:"Colombo 03", country:"Sri Lanka", checkinsTimes: a};

    map mUnorder = {line1:"No. 20", city:"Colombo 03", line2:"Palm Grove",
                       country:"Sri Lanka", checkinsTimes: a};

    map mArrayUnorder = {line1:"No. 20", line2:"Palm Grove",
                            city:"Colombo 03", country:"Sri Lanka", checkinsTimes: b};

    map mStringValueMismatch = {line1:"No. 20", line2:"Palm Groveeeeeeee",
                                   city:"Colombo 03", country:"Sri Lanka", checkinsTimes: a};

    map mMissingKeys = {line1:"No. 20",
                           city:"Colombo 03", country:"Sri Lanka", checkinsTimes: a};

    return reflect:equals(m1,m2) &&
            reflect:equals(m1,mUnorder) &&
           !reflect:equals(m1,mArrayUnorder) &&
           !reflect:equals(m1,mStringValueMismatch) &&
           !reflect:equals(m1,mMissingKeys) &&
           !reflect:equals(m1,()) &&
           !reflect:equals((),m1);
}

// End Maps

// Start Any

public function testAnyType() returns (boolean) {
    any a1 = 5;
    any a2 = 5;

    any aUnmatch1 = 10;
    any aUnmatch2 = "Hello";
    any aUnmatch3 = false;

    return reflect:equals(a1,a2) &&
           !reflect:equals(a1,aUnmatch1) &&
           !reflect:equals(a1,aUnmatch2) &&
           !reflect:equals(a1,aUnmatch3) &&
           !reflect:equals(a1,()) &&
           !reflect:equals((),a1);
}

// End Any

// Start JSON

json jString1 = "Apple";
json jString2 = "Apple";
json jStringUnmatch = "Orange";

json jInt1 = 5.36;
json jInt2 = 5.36;
json jIntUnmatch = 10.19;

json jBoolean1 = true;
json jBoolean2 = true;
json jBooleanUnmatch = false;

json jNull1 = ();
json jNull2 = ();

json empty1 = {};
json empty2 = {};

public function testJSONString() returns (boolean) {
    return reflect:equals(jString1,jString2) &&
           !reflect:equals(jString1,jStringUnmatch) &&
           !reflect:equals(jString1,jIntUnmatch) &&
           !reflect:equals(jString1,jBooleanUnmatch) &&
           !reflect:equals(jString1,jNull1) &&
           !reflect:equals(jString1,empty1);
}

public function testJSONInt() returns (boolean) {
    return reflect:equals(jInt1,jInt2) &&
           !reflect:equals(jInt1,jStringUnmatch) &&
           !reflect:equals(jInt1,jIntUnmatch) &&
           !reflect:equals(jInt1,jBooleanUnmatch) &&
           !reflect:equals(jInt1,jNull1) &&
           !reflect:equals(jInt1,empty1);
}

public function testJSONBoolean() returns (boolean) {
    return reflect:equals(jBoolean1,jBoolean2) &&
           !reflect:equals(jBoolean1,jStringUnmatch) &&
           !reflect:equals(jBoolean1,jIntUnmatch) &&
           !reflect:equals(jBoolean1,jBooleanUnmatch) &&
           !reflect:equals(jBoolean1,jNull1) &&
           !reflect:equals(jBoolean1,empty1);
}

public function testJSONNull() returns (boolean) {
    return reflect:equals(jNull1,jNull2) &&
           !reflect:equals(jNull1,jStringUnmatch) &&
           !reflect:equals(jNull1,jIntUnmatch) &&
           !reflect:equals(jNull1,jBooleanUnmatch) &&
           !reflect:equals(jNull1,empty1);
}

public function testJSONEmpty() returns (boolean) {
    return reflect:equals(empty1,empty2) &&
           !reflect:equals(empty1,jStringUnmatch) &&
           !reflect:equals(empty1,jIntUnmatch) &&
           !reflect:equals(empty1,jBooleanUnmatch) &&
           !reflect:equals(empty1,jNull1);
}

public function testJSONObjects() returns (boolean) {
    json jObj1 = {name:"apple", price: 40.50, isNew: true};
    json jObj2 = {name:"apple", price: 40.50, isNew: true};

    json jObjUnordered = {price: 40.50, isNew: true, name:"apple"};
    json jObjUnmatch1 = {price: 40.50, isNew: true, name:"orange"};
    json jObjUnmatch2 = {price: 20.00, isNew: true, name:"apple"};

    return reflect:equals(jObj1,jObj2) &&
            reflect:equals(jObj1,jObjUnordered) &&
           !reflect:equals(jObj1,jObjUnmatch1) &&
           !reflect:equals(jObj1,jObjUnmatch2) &&
           !reflect:equals(jObj1,()) &&
           !reflect:equals((),jObj1);

}

public function testJSONArray() returns (boolean) {
    json jArr1 = {primeNumebers: [2, 3, 5, 7, 11, 13]};
    json jArr2 = {primeNumebers: [2, 3, 5, 7, 11, 13]};

    json jArrUnordered = {primeNumebers: [2, 3, 5, 11, 7, 13]};
    json jArrMissing = {primeNumebers: [2, 3, 5, 11, 13]};
    json jArrUnmatch = {primeNumebers: ["test"]};
    json jArrNull = [null, {primeNumebers: ["test"]}];

    return reflect:equals(jArr1,jArr2) &&
            reflect:equals(jArrNull, jArrNull) &&
           !reflect:equals(jArr1,jArrUnordered) &&
           !reflect:equals(jArr1,jArrMissing) &&
           !reflect:equals(jArr1,jArrUnmatch);

}

public function testJSONNested() returns (boolean) {
    json jObj1 = {   name:"Target",
                     location:{
                                  address1:"19, sample road",
                                  postalCode: 6789
                              },
                     products:[{price: 40.50, isNew: true, name:"apple"},
                               {name:"orange", price: 30.50}],
                     manager: ()
                 };
    json jObj2 = {   name:"Target",
                     location:{
                                  address1:"19, sample road",
                                  postalCode: 6789
                              },
                     products:[{price: 40.50, isNew: true, name:"apple"},
                               {name:"orange", price: 30.50}],
                     manager: ()
                 };

    json jObjUnordered = {name:"Target",
                             products:[{price:40.50, isNew:true, name:"apple"},
                                       {name:"orange", price:30.50}],
                             location:{
                                          address1:"19, sample road",
                                          postalCode: 6789
                                      },
                             manager:()
                         };

    json jObjUnmatch1 = {name:"Cubs",
                            products:[{price:40.50, isNew:true, name:"apple"},
                                      {name:"orange", price:30.50}],
                            location:{
                                         address1:"19, sample road",
                                         postalCode: 6789
                                     },
                            manager:()
                        };

    json jObjUnmatch2 = {name:"Target",
                            products:[{name:"orange", price:30.50},
                                      {price:40.50, isNew:true, name:"apple"}
                                     ],
                            location:{
                                         address1:"19, sample road",
                                         postalCode: 6789
                                     },
                            manager:()
                        };

    json jObjUnmatch3 = {name:"Target",
                            products:[{name:"orange", price:30.50},
                                      {price:40.50, isNew:true, name:"apple"}
                                     ],
                            location:{
                                         address1:"70, sample road",
                                         postalCode: 6789
                                     },
                            manager:()
                        };

    json jObjUnmatch4 = {name:"Target",
                            products:[{name:"orange", price:30.50},
                                      {price:40.50, isNew:false, name:"apple"}
                                     ],
                            location:{
                                         address1:"19, sample road",
                                         postalCode: 6789
                                     },
                            manager:()
                        };

    json jObjUnmatch5 = {   name:"Target",
                            location:{
                                         address1:"19, sample road",
                                         postalCode: 6789
                                     },
                            products:[{price: 88.99, isNew: true, name:"apple"},
                                      {name:"orange", price: 30.50}],
                            manager: ()
                        };

    json jObjUnmatch6 = {   name:"Target",
                            location:{
                                         address1:"19, sample road",
                                         postalCode: 6789
                                     },
                            products:[{price: 40.50, isNew: true, name:"apple"},
                                      {name:"orange", price: 30.50}],
                            manager: {name: "Larry Ben"}
                        };

    json jObjUnmatch7 = {   name:"Target",
                            location:{
                                         address1:"19, sample road",
                                         postalCode: 6789
                                     },
                            products:[{price: 40.50, old: true, name:"apple"},
                                      {name:"orange", price: 30.50}],
                            manager: ()
                        };

    json jObjUnmatch8 = {};

    return reflect:equals(jObj1,jObj2) &&
            reflect:equals(jObj1,jObjUnordered) &&
           !reflect:equals(jObj1,jObjUnmatch1) &&
           !reflect:equals(jObj1,jObjUnmatch2) &&
           !reflect:equals(jObj1,jObjUnmatch3) &&
           !reflect:equals(jObj1,jObjUnmatch4) &&
           !reflect:equals(jObj1,jObjUnmatch5) &&
           !reflect:equals(jObj1,jObjUnmatch6) &&
           !reflect:equals(jObj1,jObjUnmatch7) &&
           !reflect:equals(jObj1,jObjUnmatch8);
}


// End JSON