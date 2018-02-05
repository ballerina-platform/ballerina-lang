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

    return sp1 === sp2 &&
           !(sp1 !== sp2) &&
           !(sp1 === spUnmatchString) &&
           sp1 !== spUnmatchString &&
           !(sp1 === spUnmatchInt) &&
           sp1 !== spUnmatchInt &&
           !(sp1 === spUnmatchBoolean) &&
           sp1 !== spUnmatchBoolean &&
           !(sp1 === null) &&
           sp1 !== null &&
           !(null === sp1) &&
           null !== sp1;
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

    return ap1 === ap2 &&
           !(ap1 !== ap2) &&
           !(ap1 === apUnmatch) &&
           ap1 !== apUnmatch &&
           !(ap1 === apUnordered) &&
           ap1 !== apUnordered &&
           !(ap1 === null) &&
           ap1 !== null &&
           !(null === ap1) &&
           null !== ap1;
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

    return c1 === c2 &&
           !(c1 !== c2) &&
           !(c1 === c3Unmatch) &&
           c1 !== c3Unmatch &&
           !(c1 === c4Unmatch) &&
           c1 !== c4Unmatch &&
           !(c1 === c5Unmatch) &&
           c1 !== c5Unmatch &&
           !(c1 === null) &&
           c1 !== null &&
           !(null === c1) &&
           null !== c1;
}

// End Structs

// Start Array of Arrays

public function testArraysOfArrays() (boolean) {
    int[][] aa1 = [[1, 2, 3], [10, 20, 30], [5, 6, 7]];
    int[][] aa2 = [[1, 2, 3], [10, 20, 30], [5, 6, 7]];

    int[][] aaUnmatch = [[1, 2, 3], [10, 99, 30], [5, 6, 7]];

    return aa1 === aa2 &&
           !(aa1 !== aa2) &&
           !(aa1 === aaUnmatch) &&
           aa1 !== aaUnmatch &&
           !(aa1 === null) &&
           aa1 !== null &&
           !(null === aa1) &&
           null !== aa1;
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

    return m1 === m2 &&
           !(m1 !== m2) &&
           m1 === mUnorder &&
           !(m1 !== mUnorder) &&
           !(m1 === mArrayUnorder) &&
           m1 !== mArrayUnorder &&
           !(m1 === mStringValueMismatch) &&
           m1 !== mStringValueMismatch &&
           !(m1 === mMissingKeys) &&
           m1 !== mMissingKeys &&
           !(m1 === null) &&
           m1 !== null &&
           !(null === m1) &&
           null !== m1;
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

    return a1 === a2 &&
           !(a1 !== a2) &&
           !(a1 === aUnmatch1) &&
           a1 !== aUnmatch1 &&
           !(a1 === aUnmatch2) &&
           a1 !== aUnmatch2 &&
           !(a1 === aUnmatch3) &&
           a1 !== aUnmatch3 &&
           !(a1 === aUnmatch4) &&
           a1 !== aUnmatch4 &&
           !(a1 === aUnmatch5) &&
           a1 !== aUnmatch5 &&
           !(a1 === null) &&
           a1 !== null &&
           !(null === a1) &&
           null !== a1;
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
    return jString1 === jString2 &&
           !(jString1 !== jString2) &&
           !(jString1 === jStringUnmatch) &&
           jString1 !== jStringUnmatch &&
           !(jString1 === jIntUnmatch) &&
           jString1 !== jIntUnmatch &&
           !(jString1 === jBooleanUnmatch) &&
           jString1 !== jBooleanUnmatch &&
           !(jString1 === jNull1) &&
           jString1 !== jNull1 &&
           !(jString1 === empty1) &&
           jString1 !== empty1;
}

public function testJSONInt() (boolean) {
    return jInt1 === jInt2 &&
           !(jInt1 !== jInt2) &&
           !(jInt1 === jStringUnmatch) &&
           jInt1 !== jStringUnmatch &&
           !(jInt1 === jIntUnmatch) &&
           jInt1 !== jIntUnmatch &&
           !(jInt1 === jBooleanUnmatch) &&
           jInt1 !== jBooleanUnmatch &&
           !(jInt1 === jNull1) &&
           jInt1 !== jNull1 &&
           !(jInt1 === empty1) &&
           jInt1 !== empty1;
}

public function testJSONBoolean() (boolean) {
    return jBoolean1 === jBoolean2 &&
           !(jBoolean1 !== jBoolean2) &&
           !(jBoolean1 === jStringUnmatch) &&
           jBoolean1 !== jStringUnmatch &&
           !(jBoolean1 === jIntUnmatch) &&
           jBoolean1 !== jIntUnmatch &&
           !(jBoolean1 === jBooleanUnmatch) &&
           jBoolean1 !== jBooleanUnmatch &&
           !(jBoolean1 === jNull1) &&
           jBoolean1 !== jNull1 &&
           !(jBoolean1 === empty1) &&
           jBoolean1 !== empty1;
}

public function testJSONNull() (boolean) {
    return jNull1 === jNull2 &&
           !(jNull1 !== jNull2) &&
           !(jNull1 === jStringUnmatch) &&
           jNull1 !== jStringUnmatch &&
           !(jNull1 === jIntUnmatch) &&
           jNull1 !== jIntUnmatch &&
           !(jNull1 === jBooleanUnmatch) &&
           jNull1 !== jBooleanUnmatch &&
           !(jNull1 === empty1) &&
           jNull1 !== empty1;
}

public function testJSONEmpty() (boolean) {
    return empty1 === empty2 &&
           !(empty1 !== empty2) &&
           !(empty1 === jStringUnmatch) &&
           empty1 !== jStringUnmatch &&
           !(empty1 === jIntUnmatch) &&
           empty1 !== jIntUnmatch &&
           !(empty1 === jBooleanUnmatch) &&
           empty1 !== jBooleanUnmatch &&
           !(empty1 === jNull1) &&
           empty1 !== jNull1;
}

public function testJSONObjects() (boolean) {
    json jObj1 = {name:"apple", price: 40.50, new: true};
    json jObj2 = {name:"apple", price: 40.50, new: true};

    json jObjUnordered = {price: 40.50, new: true, name:"apple"};
    json jObjUnmatch1 = {price: 40.50, new: true, name:"orange"};
    json jObjUnmatch2 = {price: 20.00, new: true, name:"apple"};

    return jObj1 === jObj2 &&
           !(jObj1 !== jObj2) &&
           jObj1 === jObjUnordered &&
           !(jObj1 !== jObjUnordered) &&
           !(jObj1 === jObjUnmatch1) &&
           jObj1 !== jObjUnmatch1 &&
           !(jObj1 === jObjUnmatch2) &&
           jObj1 !== jObjUnmatch2 &&
           !(jObj1 === null) &&
           jObj1 !== null &&
           !(null === jObj1) &&
           null !== jObj1;

}

public function testJSONArray() (boolean) {
    json jArr1 = {primeNumebers: [2, 3, 5, 7, 11, 13]};
    json jArr2 = {primeNumebers: [2, 3, 5, 7, 11, 13]};

    json jArrUnordered = {primeNumebers: [2, 3, 5, 11, 7, 13]};
    json jArrMissing = {primeNumebers: [2, 3, 5, 11, 13]};
    json jArrUnmatch = {primeNumebers: ["test"]};

    return jArr1 === jArr2 &&
           !(jArr1 !== jArr2) &&
           !(jArr1 === jArrUnordered) &&
           jArr1 !== jArrUnordered &&
           !(jArr1 === jArrMissing) &&
           jArr1 !== jArrMissing &&
           !(jArr1 === jArrUnmatch) &&
           jArr1 !== jArrUnmatch;

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

    return jObj1 === jObj2 &&
           !(jObj1 !== jObj2) &&
           jObj1 === jObjUnordered &&
           !(jObj1 !== jObjUnordered) &&
           !(jObj1 === jObjUnmatch1) &&
           jObj1 !== jObjUnmatch1 &&
           !(jObj1 === jObjUnmatch2) &&
           jObj1 !== jObjUnmatch2 &&
           !(jObj1 === jObjUnmatch3) &&
           jObj1 !== jObjUnmatch3 &&
           !(jObj1 === jObjUnmatch4) &&
           jObj1 !== jObjUnmatch4 &&
           !(jObj1 === jObjUnmatch5) &&
           jObj1 !== jObjUnmatch5 &&
           !(jObj1 === jObjUnmatch6) &&
           jObj1 !== jObjUnmatch6 &&
           !(jObj1 === jObjUnmatch7) &&
           jObj1 !== jObjUnmatch7 &&
           !(jObj1 === jObjUnmatch8) &&
           jObj1 !== jObjUnmatch8;
}

public connector ClientConnector1 () {
    action describeGlobal () (string) {
        return "temp";
    }
}

public connector ClientConnector2 () {
    action describeGlobal () (string) {
        return "temp";
    }
}

public function testConnectors() (boolean) {
    ClientConnector1 c1 = create ClientConnector1();
    ClientConnector1 c2 = create ClientConnector1();
    ClientConnector2 cUnmatch = create ClientConnector2();

    return c1 === c2 &&
           !(c1 !== c2) &&
           !(c1 === cUnmatch) &&
           c1 !== cUnmatch &&
           !(c1 === null) &&
           c1 !== null &&
           !(null === c1) &&
           null !== c1;
}

enum State {
    INSTALLED,
    RESOLVED
}

enum State1 {
    INSTALLED,
    RESOLVED
}

struct Person {
    string name;
    State active;
}

enum verb { GET, PUT, POST, DELETE, HEAD, OPTIONS }

public function testEnums() (boolean) {
    State s1 = State.INSTALLED;
    State s2 = State.INSTALLED;

    State1 sUnmatch1 = State1.INSTALLED;
    State1 sUnmatch2 = State1.RESOLVED;
    verb sUnmatch3 = verb.GET;

    Person p1 = {name: "John", active: State.INSTALLED};
    Person p2 = {name: "John", active: State.INSTALLED};

    Person pUnmatch1 = {name: "John", active: State.RESOLVED};

    return s1 === s2 &&
           !(s1 !== s2) &&
           p1.active === p2.active &&
           !(p1.active !== p2.active) &&
           !(s1 === sUnmatch1) &&
           s1 !== sUnmatch1 &&
           !(s1 === sUnmatch2) &&
           s1 !== sUnmatch2 &&
           !(s1 === sUnmatch3) &&
           s1 !== sUnmatch2 &&
           !(p1.active === pUnmatch1.active) &&
           p1.active !== pUnmatch1.active &&
           !(p1 === null) &&
           p1 !== null &&
           !(null === p1) &&
           null !== p1;
}

// End JSON