channel<json> chn;

function workerWithChannels() returns xml {
    worker w1 {
        json key = {"id":50, name:"john"};
        xml result;
        result <- chn, key;
        return result;
    }

    worker w2 {
        json key = {"id":50, name:"john"};
        string msg = "payment:1000";
        msg, key -> chn;
    }
}

channel<map> mapChannel;
channel<(json,json)> tupleChan;

function myFunc() {
    json key = {"id":50, name:"john"};
    string msg = "payment:1000";
    msg, key -> lastChan;
}

channel<string> lastChan;