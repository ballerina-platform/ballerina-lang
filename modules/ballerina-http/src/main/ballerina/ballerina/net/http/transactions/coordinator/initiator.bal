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

    var res = createTransactionContext(req);
    var payload = res.getJsonPayload();
    return payload;
}
//
//function callBusinessService (json txnContext) {
//    endpoint<BizClient> participantEP {
//        create BizClient();
//    }
//    var txnId, _ = (string)txnContext["transactionId"];
//    var regURL, _ = (string )txnContext["registerAtURL"];
//
//    float price = math:randomInRange(200 ,250) + math:random();
//    UpdateStockQuoteRequest bizReq = {symbol:"GOOG", price: price};
//    var j, e = participantEP.call(txnId, regURL, bizReq, "127.0.0.1", 8888);
//    j, e = participantEP.call(txnId, regURL, bizReq, "127.0.0.1", 8889);
//    println(e);
//    println(j);
//}
//
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
//
//function abortTransaction (json txnContext) returns (json) {
//    endpoint<TransactionClient> coordinatorEP {
//        create TransactionClient();
//    }
//    var txnId, _ = (string) txnContext["transactionId"];
//    AbortRequest abortReq = {transactionId:txnId};
//    var j, e = coordinatorEP.abortTransaction(abortReq);
//    println(e);
//    println(j);
//    return j;
//}
