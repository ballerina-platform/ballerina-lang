function testUsingQNameAsString () returns [string, string] {
    string s1 =  ns0  :  wso2  ;
    string temp= "  " +   ns0 :  ballerina+ "  " ;
    string s2 = temp.trim();
    return [s1, s2];
}

function testUsingQNameAsString1 () returns [string, string] {
    string s1 =
        ns0
 :
        wso2
      ;
    string temp = "  " +
 ns0
        :
      ballerina
  +
       "  "
  ;
    string s2 = temp.trim();
    return [s1, s2];
}