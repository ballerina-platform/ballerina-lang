package samples.annotation;

@Path("/orderService")
@Description("This is an echo service")
service OrderService {

    @POST
    @Path("/order")
    @Consume("application/json")
    @Produce("application/json")
    resource order(message m, Order order) {
        //stuff
        reply response;
    }

    @GET
    @Path("/order/{orderId}")
    @Produce("application/json")
    resource getOrder(message m, @PathParam("orderId") int id) {
        //stuff
            // this is a
            // block comment
        reply response; //responding
    }

    @PUT
    @Path("/order/{orderId}")
    @Consume("application/json")
    @Produce("application/json")
    resource updateOrder(message m, @PathParam("orderId") int id, @QueryParam("orderType") string myType) {
        //stuff
        reply response;
    }

    @DELETE
    @Path("/order/{orderId}")
    @Produce("application/json")
    resource deleteOrder(message m, @PathParam("orderId") int id) {
        // stuff
        reply response;
    }

}

