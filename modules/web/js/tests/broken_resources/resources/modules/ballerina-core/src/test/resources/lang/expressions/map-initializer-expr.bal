function mapInitTest() (map) {
    map animals;
    animals = {"animal1":"Lion", "animal2":"Cat", "animal3":"Leopard", "animal4":"Dog"};
    return animals;
}

function testNestedMapInit () (map) {
    map m = {"name":"Supun", "info":{"city":"Colombo", "country":"SriLanka"}};
    return m;
}

function testMapInitWithJson () (map) {
    json j = {"city":"Colombo", "country":"SriLanka"};
    map m = {"name":"Supun", "info":j};
    return m;
}

function testComplexMapInit() (map) {
    map m = { name:"Supun", 
              age:25,
              gpa:2.81,
              status:true,
              info:null, 
              address:{city:"CA", "country":"USA"},
              intArray:[7,8,9],
              addressArray:[
                    {address:{city:"Colombo", "country":"SriLanka"}},
                    {address:{city:"Kandy", "country":"SriLanka"}},
                    {address:{city:"Galle", "country":"SriLanka"}}
              ]
            };
    return m;
}