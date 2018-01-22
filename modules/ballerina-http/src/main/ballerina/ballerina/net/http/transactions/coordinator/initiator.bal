package transactions.coordinator;

import ballerina.util;

struct UpdateStockQuoteRequest {
    string symbol;
    float price;
}

function beginTransaction () returns (json) {
    http:Request req = {};
    CreateTransactionContextRequest ctcReq = {participantId: util:uuid(), coordinationType:"2pc"};
    var jsonPayload, _ = <json>ctcReq;
    req.setJsonPayload(jsonPayload);

    var res= createTransactionContext(req);
    var payload = res.getJsonPayload();
    return payload;
}

function commitTransaction (json txnContext) returns (json) {
    var txnId, _ = (string) txnContext["transactionId"];
    CommitRequest commitReq = {transactionId:txnId};
    //var j, e = coordinatorEP.commitTransaction(commitReq);
    var j, _ = <json>commitReq;
    http:Request req = {};
    req.setJsonPayload(j);
    //Think of returning error
    var res = commitTransactionFunction(req);
    json jsonRes = res.getJsonPayload();

    println(jsonRes);
    return jsonRes;
}

function abortTransaction (json txnContext) returns (json) {
    var txnId, _ = (string) txnContext["transactionId"];
    AbortRequest abortReq = {transactionId:txnId};
    var j, _ = <json>abortReq;
    http:Request req = {};
    req.setJsonPayload(j);
    var res = abortTransactionFunction(req);
    var jsonRes = res.getJsonPayload();
    println(jsonRes);
    return jsonRes;
}
