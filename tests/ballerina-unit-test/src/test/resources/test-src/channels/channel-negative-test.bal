channel<json> chn = new;

function workerWithChannels() returns xml {
    worker w2 {
        json key = {"id":50, name:"john"};
        string msg = "payment:1000";
        msg -> chn, key;
    }

    json key = {"id":50, name:"john"};
    xml result = xml `tt`;
    result = <- chn, key;
    return result;
}

channel<map<any>> mapChannel = new;
channel<(json,json)> tupleChan = new;

function myFunc() {
    json key = {"id":50, name:"john"};
    string msg = "payment:1000";
    msg -> lastChan, key;
}

channel<string> lastChan = new;

@sensitive
channel<string> annoChan = new;
