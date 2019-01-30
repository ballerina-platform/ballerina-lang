import ballerina/http;

http:ServiceEndpointConfiguration helloWorldEPConfig = {
  secureSocket: {
                    keyStore: {
               path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
          password: "ballerina"
           }
       }
};