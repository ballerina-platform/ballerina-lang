import ballerina.reflect;

// Start Structs
struct SimplePerson {
    string name;
    int age = -1;
    boolean married;
}

function testPrimitiveStructs() (boolean) {
    SimplePerson sp1 = {name: "Nick", age: 25, married: true};
    SimplePerson sp2 = {age: 25, married: true, name: "Nick"};

    SimplePerson spUnmatchString = {name: "Jack", age: 25, married: true};
    SimplePerson spUnmatchInt = {name: "Nick", age: 20, married: true};
    SimplePerson spUnmatchBoolean = {name: "Nick", age: 25, married: false};

    return reflect:deepEquals(sp1,sp2) &&
           !reflect:deepEquals(sp1,spUnmatchString) &&
           !reflect:deepEquals(sp1,spUnmatchInt) &&
           !reflect:deepEquals(sp1,spUnmatchBoolean) &&
           !reflect:deepEquals(sp1,null) &&
           !reflect:deepEquals(null,sp1);
}

public struct ArrayedPerson {
    string name;
    int age = -1;
    boolean married;
    string[] address;
}

function testStructsWithArrays() (boolean) {
    ArrayedPerson ap1 = {name: "Nick", married: true, address: ["20", "Palm Grove"]};
    ArrayedPerson ap2 = {name: "Nick", married: true, address: ["20", "Palm Grove"]};

    ArrayedPerson apUnmatch = {name: "Jack", married: true, address: ["20", "Duplication Road"]};
    ArrayedPerson apUnordered = {name: "Nick", married: true, address: ["Palm Grove", "20"]};

    return reflect:deepEquals(ap1,ap2) &&
           !reflect:deepEquals(ap1,apUnmatch) &&
           !reflect:deepEquals(ap1,apUnordered) &&
           !reflect:deepEquals(ap1,null) &&
           !reflect:deepEquals(null,ap1);
}

public struct Wheel {
    int pressure;
}

public struct Engine {
    string model;
    float capacity;
}

public struct Car {
    string name;
    Wheel[] wheels;
    Engine engine;
}

public function testNestedStructs() (boolean) {
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

    return reflect:deepEquals(c1,c2) &&
           !reflect:deepEquals(c1,c3Unmatch) &&
           !reflect:deepEquals(c1,c4Unmatch) &&
           !reflect:deepEquals(c1,c5Unmatch) &&
           !reflect:deepEquals(c1,null) &&
           !reflect:deepEquals(null,c1);
}

// End Structs

// Start Array of Arrays

public function testArraysOfArrays() (boolean) {
    int[][] aa1 = [[1, 2, 3], [10, 20, 30], [5, 6, 7]];
    int[][] aa2 = [[1, 2, 3], [10, 20, 30], [5, 6, 7]];

    int[][] aaUnmatch = [[1, 2, 3], [10, 99, 30], [5, 6, 7]];

    return reflect:deepEquals(aa1,aa2) &&
           !reflect:deepEquals(aa1,aaUnmatch) &&
           !reflect:deepEquals(aa1,null) &&
           !reflect:deepEquals(null,aa1);
}

// End Array of Arrays

// Start Maps

public function testMaps () (boolean) {
    map m1 = {line1:"No. 20", line2:"Palm Grove",
                 city:"Colombo 03", country:"Sri Lanka", checkinsTimes: ["0900", 2230]};
    map m2 = {line1:"No. 20", line2:"Palm Grove",
                 city:"Colombo 03", country:"Sri Lanka", checkinsTimes: ["0900", 2230]};

    map mUnorder = {line1:"No. 20", city:"Colombo 03", line2:"Palm Grove",
                       country:"Sri Lanka", checkinsTimes: ["0900", 2230]};

    map mArrayUnorder = {line1:"No. 20", line2:"Palm Grove",
                            city:"Colombo 03", country:"Sri Lanka", checkinsTimes: [2230, "0900"]};

    map mStringValueMismatch = {line1:"No. 20", line2:"Palm Groveeeeeeee",
                                   city:"Colombo 03", country:"Sri Lanka", checkinsTimes: ["0900", 2230]};

    map mMissingKeys = {line1:"No. 20",
                           city:"Colombo 03", country:"Sri Lanka", checkinsTimes: ["0900", 2230]};

    return reflect:deepEquals(m1,m2) &&
            reflect:deepEquals(m1,mUnorder) &&
           !reflect:deepEquals(m1,mArrayUnorder) &&
           !reflect:deepEquals(m1,mStringValueMismatch) &&
           !reflect:deepEquals(m1,mMissingKeys) &&
           !reflect:deepEquals(m1,null) &&
           !reflect:deepEquals(null,m1);
}

// End Maps

// Start Any

public function testAnyType() (boolean) {
    any a1 = 5;
    any a2 = 5;

    any aUnmatch1 = 10;
    any aUnmatch2 = "Hello";
    any aUnmatch3 = [4,5];
    any aUnmatch4 = {num: 20, lane:"Palm Grove"};
    any aUnmatch5 = false;

    return reflect:deepEquals(a1,a2) &&
           !reflect:deepEquals(a1,aUnmatch1) &&
           !reflect:deepEquals(a1,aUnmatch2) &&
           !reflect:deepEquals(a1,aUnmatch3) &&
           !reflect:deepEquals(a1,aUnmatch4) &&
           !reflect:deepEquals(a1,aUnmatch5) &&
           !reflect:deepEquals(a1,null) &&
           !reflect:deepEquals(null,a1);
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

json jNull1 = null;
json jNull2 = null;

json empty1 = {};
json empty2 = {};

public function testJSONString() (boolean) {
    return reflect:deepEquals(jString1,jString2) &&
           !reflect:deepEquals(jString1,jStringUnmatch) &&
           !reflect:deepEquals(jString1,jIntUnmatch) &&
           !reflect:deepEquals(jString1,jBooleanUnmatch) &&
           !reflect:deepEquals(jString1,jNull1) &&
           !reflect:deepEquals(jString1,empty1);
}

public function testJSONInt() (boolean) {
    return reflect:deepEquals(jInt1,jInt2) &&
           !reflect:deepEquals(jInt1,jStringUnmatch) &&
           !reflect:deepEquals(jInt1,jIntUnmatch) &&
           !reflect:deepEquals(jInt1,jBooleanUnmatch) &&
           !reflect:deepEquals(jInt1,jNull1) &&
           !reflect:deepEquals(jInt1,empty1);
}

public function testJSONBoolean() (boolean) {
    return reflect:deepEquals(jBoolean1,jBoolean2) &&
           !reflect:deepEquals(jBoolean1,jStringUnmatch) &&
           !reflect:deepEquals(jBoolean1,jIntUnmatch) &&
           !reflect:deepEquals(jBoolean1,jBooleanUnmatch) &&
           !reflect:deepEquals(jBoolean1,jNull1) &&
           !reflect:deepEquals(jBoolean1,empty1);
}

public function testJSONNull() (boolean) {
    return reflect:deepEquals(jNull1,jNull2) &&
           !reflect:deepEquals(jNull1,jStringUnmatch) &&
           !reflect:deepEquals(jNull1,jIntUnmatch) &&
           !reflect:deepEquals(jNull1,jBooleanUnmatch) &&
           !reflect:deepEquals(jNull1,empty1);
}

public function testJSONEmpty() (boolean) {
    return reflect:deepEquals(empty1,empty2) &&
           !reflect:deepEquals(empty1,jStringUnmatch) &&
           !reflect:deepEquals(empty1,jIntUnmatch) &&
           !reflect:deepEquals(empty1,jBooleanUnmatch) &&
           !reflect:deepEquals(empty1,jNull1);
}

public function testJSONObjects() (boolean) {
    json jObj1 = {name:"apple", price: 40.50, new: true};
    json jObj2 = {name:"apple", price: 40.50, new: true};

    json jObjUnordered = {price: 40.50, new: true, name:"apple"};
    json jObjUnmatch1 = {price: 40.50, new: true, name:"orange"};
    json jObjUnmatch2 = {price: 20.00, new: true, name:"apple"};

    return reflect:deepEquals(jObj1,jObj2) &&
            reflect:deepEquals(jObj1,jObjUnordered) &&
           !reflect:deepEquals(jObj1,jObjUnmatch1) &&
           !reflect:deepEquals(jObj1,jObjUnmatch2) &&
           !reflect:deepEquals(jObj1,null) &&
           !reflect:deepEquals(null,jObj1);

}

public function testJSONArray() (boolean) {
    json jArr1 = {primeNumebers: [2, 3, 5, 7, 11, 13]};
    json jArr2 = {primeNumebers: [2, 3, 5, 7, 11, 13]};

    json jArrUnordered = {primeNumebers: [2, 3, 5, 11, 7, 13]};
    json jArrMissing = {primeNumebers: [2, 3, 5, 11, 13]};
    json jArrUnmatch = {primeNumebers: ["test"]};

    return reflect:deepEquals(jArr1,jArr2) &&
           !reflect:deepEquals(jArr1,jArrUnordered) &&
           !reflect:deepEquals(jArr1,jArrMissing) &&
           !reflect:deepEquals(jArr1,jArrUnmatch);

}

public function testJSONNested() (boolean) {
    json jObj1 = {   name:"Target",
                     location:{
                                  address1:"19, sample road",
                                  postalCode: 6789
                              },
                     products:[{price: 40.50, new: true, name:"apple"},
                               {name:"orange", price: 30.50}],
                     manager: null
                 };
    json jObj2 = {   name:"Target",
                     location:{
                                  address1:"19, sample road",
                                  postalCode: 6789
                              },
                     products:[{price: 40.50, new: true, name:"apple"},
                               {name:"orange", price: 30.50}],
                     manager: null
                 };

    json jObjUnordered = {name:"Target",
                             products:[{price:40.50, new:true, name:"apple"},
                                       {name:"orange", price:30.50}],
                             location:{
                                          address1:"19, sample road",
                                          postalCode: 6789
                                      },
                             manager:null
                         };

    json jObjUnmatch1 = {name:"Cubs",
                            products:[{price:40.50, new:true, name:"apple"},
                                      {name:"orange", price:30.50}],
                            location:{
                                         address1:"19, sample road",
                                         postalCode: 6789
                                     },
                            manager:null
                        };

    json jObjUnmatch2 = {name:"Target",
                            products:[{name:"orange", price:30.50},
                                      {price:40.50, new:true, name:"apple"}
                                     ],
                            location:{
                                         address1:"19, sample road",
                                         postalCode: 6789
                                     },
                            manager:null
                        };

    json jObjUnmatch3 = {name:"Target",
                            products:[{name:"orange", price:30.50},
                                      {price:40.50, new:true, name:"apple"}
                                     ],
                            location:{
                                         address1:"70, sample road",
                                         postalCode: 6789
                                     },
                            manager:null
                        };

    json jObjUnmatch4 = {name:"Target",
                            products:[{name:"orange", price:30.50},
                                      {price:40.50, new:false, name:"apple"}
                                     ],
                            location:{
                                         address1:"19, sample road",
                                         postalCode: 6789
                                     },
                            manager:null
                        };

    json jObjUnmatch5 = {   name:"Target",
                            location:{
                                         address1:"19, sample road",
                                         postalCode: 6789
                                     },
                            products:[{price: 88.99, new: true, name:"apple"},
                                      {name:"orange", price: 30.50}],
                            manager: null
                        };

    json jObjUnmatch6 = {   name:"Target",
                            location:{
                                         address1:"19, sample road",
                                         postalCode: 6789
                                     },
                            products:[{price: 40.50, new: true, name:"apple"},
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
                            manager: null
                        };

    json jObjUnmatch8 = {};

    return reflect:deepEquals(jObj1,jObj2) &&
            reflect:deepEquals(jObj1,jObjUnordered) &&
           !reflect:deepEquals(jObj1,jObjUnmatch1) &&
           !reflect:deepEquals(jObj1,jObjUnmatch2) &&
           !reflect:deepEquals(jObj1,jObjUnmatch3) &&
           !reflect:deepEquals(jObj1,jObjUnmatch4) &&
           !reflect:deepEquals(jObj1,jObjUnmatch5) &&
           !reflect:deepEquals(jObj1,jObjUnmatch6) &&
           !reflect:deepEquals(jObj1,jObjUnmatch7) &&
           !reflect:deepEquals(jObj1,jObjUnmatch8);
}


// End JSON