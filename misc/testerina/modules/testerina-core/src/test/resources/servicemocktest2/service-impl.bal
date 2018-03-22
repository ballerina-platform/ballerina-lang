package servicemocktest2;

import ballerina/net.http;

public function hadleGetEvents () returns (http:Response) {

    // Here we need to call the Event Service
    http:Response res = {};
    // Need to improve to handle error case
    json j = getEvents();
    res.setJsonPayload(j);
    return res;
}
