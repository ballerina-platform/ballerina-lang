
channel<json> chn;

function workerWithChannels() returns json {
    worker {
        json key = {"id":50, name:"john"};
        json result;
        result <- chn, key;
        return result;
    }

    worker {
        json key = {"id":50, name:"john"};
        {"payment":10000}, key -> chn;
    }
}