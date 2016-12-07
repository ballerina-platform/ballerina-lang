package sample.annotation.petstore;

@ServiceInfo(
        Version = "1.0.0" ,
        Title = "Swagger Petstore (Simple)" ,
        Description = "A sample API that uses a petstore as an example to demonstrate features in the swagger-2.0 specification" ,
        termOfService = "http://helloreverb.com/terms/" ,
        contact = @Contact( name = "Swagger API team" , email = "foo@example.com" , url = "http://swagger.io" ) ,
        license = @License( name = "MIT", url = "http://opensource.org/licenses/MIT")
        )
@ServiceConfig( schemes =  { "http" }, host = "petstore.swagger.io" )
@Consumes({"application/json"})
@Produces({"application/json"})
@Path("/api")
service PetStore{

    @GET
    @Path("/pets")
    @Produces({ "application/json", "application/xml", "text/xml", "text/html" })
    @Responses({
            @Response(code = 200,
                      description = "pet response",
                      response = Pet[]),
            @Response(code = default,
                      description = "unexpected error",
                      response = ErrorModel)
    })
    @ResourceInfo(
        description = "Returns all pets from the system that the user has access to"
        operationId = "findPets";
    )
    @ParametersInfo ({
        @ParameterInfo(
            name = "tags" ,
            description = "tags to filter by" ,
            required = false ,
            type = "array" ,
            items =  @items ( type = "string" ) ,
            collectionFormat = "csv" ,
        ),
        @ParameterInfo(
            name = "limit" ,
            description = "maximum number of results to return" ,
            required = false ,
            type = "integer" ,
        ),
    })
    resource findPets(message m, @QueryParam("tags") String[] tags, @QueryParam("limit") int limit) {
        // Auto Generated Code by Ballerina. Implement your logic here.
        reply m;
    }

    @POST
    @Path("/pets")
    @Produces({ "application/json" })
    @Responses({
            @Response(code = 200,
                      description = "pet response",
                      response = Pet),
            @Response(code = default,
                      description = "unexpected error",
                      response = ErrorModel)
    })
    @ResourceInfo(
        description = "Creates a new pet in the store.  Duplicates are allowed"
        operationId = "addPet";
    )
    @ParametersInfo ({
        @ParameterInfo(
            name = "pet" ,
            description = "Pet to add to the store" ,
            required = true
        )
    })
    resource addPet(message m, @Body("pet") NewPet newPetObject){
        // Auto Generated Code by Ballerina. Implement your logic here.
        reply m;
    }

    @GET
    @Path("/pets/{id}")
    @Produces({ "application/json", "application/xml", "text/xml", "text/html" })
    @Responses({
            @Response(code = 200,
                      description = "pet response",
                      response = Pet),
            @Response(code = default,
                      description = "unexpected error",
                      response = ErrorModel)
    })
    @ResourceInfo(
        description = "Returns a user based on a single ID, if the user does not have access to the pet"
        operationId = "findPetById";
    )
    @ParametersInfo ({
        @ParameterInfo(
            name = "id" ,
            description = "ID of pet to fetch" ,
            required = true,
            type = "integer" ,
        )
    })
    resource findPetById(message m, @PathParam("id") long id) {
        // Auto Generated Code by Ballerina. Implement your logic here.
        reply m;
    }

    @DELETE
    @Path("/pets/{id}")
    @Responses({
            @Response(code = 204,
                      description = "pet deleted"
            @Response(code = default,
                      description = "unexpected error",
                      response = ErrorModel)
    })
    @ResourceInfo(
        description = "deletes a single pet based on the ID supplied"
        operationId = "deletePet";
    )
    @ParametersInfo ({
        @ParameterInfo(
            name = "id" ,
            description = "ID of pet to delete" ,
            required = true,
            type = "integer" ,
        )
    })
    resource deletePet(message m, @PathParam("id") long id) {
        // Auto Generated Code by Ballerina. Implement your logic here.
        reply m;
    }
}

type pet{
    @Property(required = true)
    long id;

    @Property(required = true)
    string name;

    @Property(required = false)
    string tag;
}

type newPet{
    @Property(required = false)
    int id;

    @Property(required = true)
    string name;

    @Property(required = false)
    string tag;
}

type errorModel{
    @Property(required = true)
    int code;

    @Property(required = true)
    string message;
}
