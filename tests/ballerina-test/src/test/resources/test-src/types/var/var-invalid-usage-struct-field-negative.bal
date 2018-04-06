type Person {
    string name;
    int age;
};


function test() returns string {
   Person human = {};
   var human.name = getHumanName();
   return human.name;
}

function getHumanName() returns string {
   return "Human Name";
}