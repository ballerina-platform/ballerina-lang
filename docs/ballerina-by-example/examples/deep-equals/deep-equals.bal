function main (string[] args) {
    //Deep equals for strings.
    string helloString1 = "Hello";
    string helloString2 = "Hello";
    string welcomeString2 = "Welcome";
    if (helloString1 === helloString2) {
        println("helloString1 has the same value as helloString2");
    }
    if (helloString1 !== welcomeString2) {
        println("helloString1 is not equal to welcomeString2");
    }


    //Deep equals for int.
    int intValue5 = 5;
    int intValueFive = 5;
    int intValue = 10;
    if (intValue5 === intValueFive) {
        println("Integer values are equal");
    }
    if (intValue5 !== intValue) {
        println("5 is not equal to 5");
    }


    //Deep equals for arrays. Order of the values are considered.
    float[] floatValues1 = [2.0, 20.5];
    float[] floatValues2 = [2.0, 20.5];
    float[] unOrderedFloats = [2.0, 20.5];
    float[] unMatchingFloats = [7.5, 20.5];
    if (floatValues1 === floatValues2) {
        println("floatValues1 has the values as floatValues2 and are in order.");
    }
    if (floatValues1 !== unOrderedFloats) {
        println("Arrays have the same values but not in the same order.");
    }
    if (floatValues1 !== unMatchingFloats) {
        println("Array has different values.");
    }


    //Deep equals for structs. Order of the fields does not matter. The order of the values of an array field does matter.
    Person nick = {name: "Nick", age: 25, married: true, address: ["20", "Palm Grove"]};
    Person nickUnorderedFields = {age: 25, married: true, name: "Nick", address: ["20", "Palm Grove"]};
    Person jack = {name: "Jack", age: 25, married: true, address: ["787", "Castro Street"]};
    Person nickWithInvalidAddress = {age: 25, married: true, name: "Nick", address: ["Palm Grove", "20"]};
    if (nick === nickUnorderedFields) {
        println("This is nick even if fields are unordered.");
    }
    if (nick !== jack) {
        println("Nick is not Jack.");
    }
    if (nick !== nickWithInvalidAddress) {
        println("Address of Nick is different.");
    }


    //Deep equals for maps. The order of the attributes are always considered. Values of array attributes should be in order.
    map map1 = {line1:"No. 20", line2:"Palm Grove", checkinsTimes: ["0900", 2230]};
    map map2 = {line1:"No. 20", line2:"Palm Grove", checkinsTimes: ["0900", 2230]};
    map unmatchingMap = {line1:"No. 787", line2:"Castro Street", checkinsTimes: ["0900", 2230]};
    map unorderedMap = {line2:"Palm Grove", line1:"No. 20", checkinsTimes: ["0900", 2230]};
    if (map1 === map2) {
        println("map1 and map2 has the same values and are in the right order.");
    }
    if (map1 !== unmatchingMap) {
        println("The values of the map attributes are different.");
    }
    if (map1 !== unorderedMap) {
        println("The order of the map attributes are different.");
    }


    //Deep equals for any type.
    any any1 = 5;
    any any2 = 5;
    any anyUnmatchInt = 10;
    any anyUnmatchString = "Hello";
    any anyUnmatchArray = [4, 5];
    if (any1 === any2) {
        println("any1 and any2 are equal as they have the same values.");
    }
    if (any1 !== anyUnmatchInt) {
        println("5 is not equal to 10.");
    }
    if (any1 !== anyUnmatchString) {
        println("5 is not a string.");
    }
    if (any1 !== anyUnmatchArray) {
        println("5 is not an array.");
    }


    //Deep Equals for JSON. The order of the members does not matter.
    json jObj1 = {   name:"Target",
                     location:{
                                  address1:"15150 Cedar Ave  Apple Valley",
                                  postalCode: 55124
                              },
                     products:[{price: 40.50, new: true, name:"apple"},
                               {name:"orange", price: 30.50}],
                     manager: null
                 };
    json jObj2 = {   name:"Target",
                     location:{
                                  address1:"15150 Cedar Ave  Apple Valley",
                                  postalCode: 55124
                              },
                     products:[{price: 40.50, new: true, name:"apple"},
                               {name:"orange", price: 30.50}],
                     manager: null
                 };
    json jObjUnmatch = {name:"Target",
                            products:[{price:40.50, new:true, name:"apple"},
                                      {name:"orange", price:30.50}],
                            location:{
                                         address1:"14333 Hwy 13  Savage",
                                         postalCode: 55378
                                     },
                            manager:null
                        };
    if (jObj1 === jObj2) {
        println("It is the same store.");
    }
    if (jObj1 !== jObjUnmatch) {
        println("Address of the store is different.");
    }
}

struct Person {
    string name;
    int age = -1;
    boolean married;
    string[] address;
}

