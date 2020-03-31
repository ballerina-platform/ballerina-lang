function serviceConstruct() {
    service doOrderOutboundDispatcherETLService =    service{
              resource function onTrigger()     {
        int a = 0;
 }
         };
}

function serviceConstruct1() {
    service doOrderOutboundDispatcherETLService =
             service {
   resource function onTrigger(){
                int a = 0;
           }
};
}

function serviceConstruct2() {
    service doOrderOutboundDispatcherETLService =@http:ServiceConfig {}  service{
             resource function onTrigger() {
       int a = 0;
        }
        };
}

function serviceConstruct3() {
    service doOrderOutboundDispatcherETLService =
        @http:ServiceConfig {} service {
    resource function onTrigger() {
               int a = 0;
           }
        };
}

function serviceConstruct4() {
    service doOrderOutboundDispatcherETLService =
 @http:ServiceConfig {
                   basePath: ""
  }      service   {
               resource function onTrigger() {
                   int a = 0;
       }
            };
}