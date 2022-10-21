import ballerina/module1;

public function main (string... args) {
   int a = 12;
   int b = 13;
   module1:TestRecord2 response = getResponse(a, b);
}
