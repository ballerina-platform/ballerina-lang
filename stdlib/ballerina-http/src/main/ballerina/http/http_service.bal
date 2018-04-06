package ballerina.http;

public type Service object {
    public function getEndpoint() returns ServiceEndpoint {
        ServiceEndpoint ep = new;
        return ep;
    }
};
