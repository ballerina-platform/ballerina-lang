import ballerina/lang.'int;

function parse(string num) returns int|error {
   return 'int:fromString(num);
}

public function checkpanicTest() {
      int   y          =
      checkpanic       parse ( "120"  )  ;
}
