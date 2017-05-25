function testComplexMapToJson() (json) {
    Person p = {name:"Supun"};
    json j = {title:"SE"};
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
              ],
              parent:p,
              occupation:j
            };
    return m;
}
