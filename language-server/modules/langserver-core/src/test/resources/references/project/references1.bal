import pkg1;

@readonly string READ_ONLY_VAR;
@final string FINAL_VAR = "Final variable";

type Info {
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