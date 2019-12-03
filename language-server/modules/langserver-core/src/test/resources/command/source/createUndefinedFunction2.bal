import ballerina/http;

public function main (string... args) {
   int a = 12;
   int b = 13;
   http:Response response = getResponse(a, b);
}
