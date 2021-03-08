import ballerina/module1;

class File {
   string path;
   string contents;
   function init(string p)
                     returns error? {
      self.path = p;
      self.contents =
         check io:fileReadString(p);         
   }
}

function name() {
    new File("test.txt");
}
