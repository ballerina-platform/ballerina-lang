package ballerina.sample.petstore;

@APIDescription(
        Version = "1.0.0",
        Title = "Swagger Petstore (Simple)",
        Description = "A sample API that uses a petstore as an example to demonstrate features in the swagger-2.0 specification",
        SwaggerVersion = "2.0")
@APIConfiguration(
        Schemes =  [ "http" ],
        Host = "petstore.swagger.io")
@Consumes({"application/json"})
@Produces({"application/json"})
@Path("/api") // BasePath or Path.
service PetStore{

    @GET
    @Path("/pets")
    @Produces({ "application/json", "application/xml", "text/xml", "text/html" })
    @Responses(values = {
            @Response(code = 200,
                      description = "pet response",
                      response = pet[]),
            @Response(code = default,
                      description = "unexpected error",
                      response = errorModel)
    })
    @ResourceInfo(description = "Returns all pets from the system that the user has access to")
    resource findPets(message m,
                      @QueryParam(name = "tags", description = "tags to filter by", required = false) String[] tags,
                      @QueryParam(name = "limit", description = "maximum number of results to return", required = false)
                        int limit) {
        reply m;
    }

    @POST
    @Path("/pets")
    @Produces({ "application/json" })
    @Responses(values = {
            @Response(code = 200,
                      description = "pet response",
                      response = pet),
            @Response(code = default,
                      description = "unexpected error",
                      response = errorModel)
    })
    @ResourceInfo(description = "Creates a new pet in the store.  Duplicates are allowed")
    resource addPet(message m){
        reply m;
    }

    @GET
    @Path("/pets/{id}")
    @Produces({ "application/json", "application/xml", "text/xml", "text/html" })
    @Responses(values = {
            @Response(code = 200,
                      description = "pet response",
                      response = pet),
            @Response(code = default,
                      description = "unexpected error",
                      response = errorModel)
    })
    @ResourceInfo(description = " Returns a user based on a single ID, if the user does not have access to the pet")
    resource findPetById(message m,
                         @QueryParam(name = "id", description = "ID of pet to fetch", required = ture) int id){
        reply m;
    }

    @DELETE
    @Path("/pets/{id}")
    @Produces({ "application/json", "application/xml" })
    @Responses(values = {
            @Response(code = 204,
                      description = "pet deleted"),
            @Response(code = default,
                      description = "unexpected error")
    })
    @ResourceInfo(description = "deletes a single pet based on the ID supplied")
    resource deletePet(message m,
                       @QueryParam(name = "id", description = "Pet id to delete", required = ture) int id){
        reply m;
    }
}

type pet{
    @Property(name = "id", required = true)
    int id;

    @Property(name = "name", required = true)
    string name;

    @Property(name = "tag", required = false)
    string tag;
}

type newPet{
    @Property(name = "id", required = false)
    int id;

    @Property(name = "name", required = true)
    string name;

    @Property(name = "tag", required = false)
    string tag;
}

type errorModel{
    @Property(name = "code", required = true)
    int code;

    @Property(name = "message", required = true)
    string message;
}
