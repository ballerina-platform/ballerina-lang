channel<json> chn;

function workerWithChannels() returns json {
    worker w1 {
        json key = {"id":50, name:"john"};
        json result;
        // result <- chn, key;
        result <- 
        return result;
    }

    worker w2 {
        json key = {"id":50, name:"john"};
        json msg = {"payment":10000};
        // msg -> chn, key;
        msg -> 
    }
}
