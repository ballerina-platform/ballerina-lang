package ballerina.net.websub;

public struct SubscriberService {
}

function <SubscriberService s> getEndpoint () returns (SubscriberServiceEndpoint) {
    SubscriberServiceEndpoint ep = {};
    return ep;
}
