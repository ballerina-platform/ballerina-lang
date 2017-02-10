import ballerina.lang.message;
import ballerina.lang.system;

@BasePath ("/passthrough")
service passthrough {

    @POST
    @Path("/test")
    resource passthrough (message msg) {
      worker sampleWorker (message m)  {
        double amount;
        double sumD;
        int quantity;
        double a;
        json j;

        j = `{"name":"chanaka"}`;
        message:setJsonPayload(m, j);
        sumD = 123d;
        amount = 222d;
        quantity = 12;
        a = 123d;
        sumD = sumD + ( amount * quantity );
        system:println(sumD);
        reply m;
      }
      double aa;
      message result;
      aa = 13;
      system:println(aa);
      msg -> sampleWorker;
      system:println("After worker");
      system:println("Doing something else");
      system:println("Doing another thing");
      result <- sampleWorker;
      reply result;
    }
}
