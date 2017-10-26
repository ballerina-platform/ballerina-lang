struct Person {
    string name;
    int age;
}


function test()(string){
   Person human = {};
   var human.name = getHumanName();
   return human.name;
}

function getHumanName()(string){
   return "Human Name";
}