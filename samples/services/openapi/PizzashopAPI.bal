package samples.annotation;


//
//Work in progress
//


@BasePath("/orderService")
@Service(description = "Service which is fully comply with OpenAPI/Swagger API definitions")

service OrderService {

    @POST
    @Path("/pizza")
    @Consume("application/json")
    @Produce("application/json")
    resource order(message m, Order order) {
        //stuff
        reply response;
    }



}

