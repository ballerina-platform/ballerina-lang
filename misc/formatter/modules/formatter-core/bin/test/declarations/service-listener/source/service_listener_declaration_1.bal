import ballerina/http;

 service   hello   on   new    http  :  Listener  (  9090  )   {
     resource    function   sayHello  (  http  :  Caller   caller  ,   http :   Request   req   )   returns    error  ?   {
         check    caller   ->   respond  (  "Hello"  )  ;



     }
}
