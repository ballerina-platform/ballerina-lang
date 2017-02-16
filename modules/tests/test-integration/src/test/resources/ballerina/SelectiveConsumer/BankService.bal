package bal.integration.selectiveConsumer;

@BasePath("/bankCreditService")
service BankCreditService{

    @POST
    @Path ("/specialityCreditDep")
    resource specialityCreditDep (message m) {
        reply m;
    }

    @POST
    @Path ("/normalCreditDep")
    resource normalCreditDep (message m) {
        reply m;
    }
}

@BasePath("/bankCardService")
service BankCardService{

    @POST
    @Path ("/specialityCustomerDep")
    resource specialityCustomerDep (message m) {
        reply m;
    }

    @POST
    @Path ("/normalCustomerDep")
    resource normalCustomerDep (message m) {
        reply m;
    }
}