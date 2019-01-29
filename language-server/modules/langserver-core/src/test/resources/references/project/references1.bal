import pkg1;

const string constantVariable = "Read Only Variable";
final string finalVariable = "Final variable";

type Info record {
    int id;
    string name;
};

function funcSameFile() {

}

function testFunc () returns Info|() {
     funcSameFile();
     string|() name = pkg1:getName();

     Info info = {
          id: 1,
          name: "test"
     };

     string infoName = info.name;
     return info;
}