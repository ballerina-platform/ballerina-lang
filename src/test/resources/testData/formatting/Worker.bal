import ballerina.lang.system;
import ballerina.lang.messages;

function main (string[] args) {

    message result;
    message msg = {         };
    int x = 100;
    float            y;
          msg     ,         x         ->      sampleWorker        ;
    system:     println         (   "Inside default worker after invoking the sampleWorker"        )       ;
            result  ,         y <- sampleWorker;
    string   s   =       messages:getStringPayload          (   result     )    ;
    system:println("Message received from sampleWorker is " + s)        ;
    system:println("Float received from sampleWorker is " + y);

          worker      sampleWorker         {
        message      m      ;
        int         a;
         float  b       =  12.34        ;
         m   ,  a      <-   default  ;
        system  : println  (   "Passed in integer value is "   +    a  ) ;
        json j;
         j =    {   "name":"tom"    }   ;
             messages   :  setJsonPayload    (   m  ,     j   )   ;
          m   ,   b     ->     default      ;
    }
}
