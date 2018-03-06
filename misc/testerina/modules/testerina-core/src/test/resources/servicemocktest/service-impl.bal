package src.test.resources.servicemocktest;

import ballerina.io;
import ballerina.net.http;

public function hadleGetEvents () (http:OutResponse res) {

    // Here we need to call the Event Service
    res = {};
    // Need to improve to handle error case
    json j = getEvents();
    res.setJsonPayload(j);
    return;
}
