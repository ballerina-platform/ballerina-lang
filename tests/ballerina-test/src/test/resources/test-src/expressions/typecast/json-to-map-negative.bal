function testComplexJsonToMap() returns (map) {
    json j = {name:"Supun", 
              age:25,
              gpa:2.81,
              status:true,
              info:null, 
              address:{city:"Colombo", "country":"SriLanka"}, 
              marks:[1,5,7]};
    map m = <map> j;
    return m;
}
