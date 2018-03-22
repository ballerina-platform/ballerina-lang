function testComplexMapToJson() returns (json) {
    map m = { name:"Supun", 
              age:25,
              gpa:2.81,
              status:true
            };
    json j2 = <json> m;
    return j2;
}
