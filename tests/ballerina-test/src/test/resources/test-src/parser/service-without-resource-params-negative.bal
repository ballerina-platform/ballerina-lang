import ballerina/http;

service<http:Service> HelloService {

 @http:ResourceConfig {
      methods:["GET"],
      path:"/"
}
   tweet (){
      int b;
      return m;
  }
}
