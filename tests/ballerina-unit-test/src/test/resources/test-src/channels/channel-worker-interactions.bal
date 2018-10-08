
channel<json> chn;

function workerWithChannels() returns json {
    worker w1 {
        json key = {"id":50, name:"john"};
        json result;
        result <- chn, key;
        return result;
    }

    worker w2 {
        json key = {name:"john", "id":50};
        json msg = {"payment":10000};
        msg -> chn, key;
    }
}

function sendBeforeReceive() returns json {
    worker w2 {
        json key = {name:"john", "id":50};
        json msg = {"payment":10000};
        msg -> chn, key;
    }

    worker w1 {
        json key = {"id":50, name:"john"};
        json result;
        result <- chn, key;
        return result;
    }
}

function nullKeyChannels() returns json {
    worker w2 {
        json msg = {"payment":10000};
        msg -> chn;
    }

    worker w1 {
        json result;
        result <- chn;
        return result;
    }
}

function multipleInteractions() returns json {
    worker w2 {
        json msg = {"payment":10000};
        json msg2 = {"payment":50000};
        json msg3 = {"payment":60000};
        json key = {"id":50, name:"john"};
        json key2 = {"id":60, name:"john"};
        msg -> chn;
        msg2 -> chn, key;
        msg3 -> chn, key2;
    }

    worker w1 {
        json result;
        json key = {"id":50, name:"john"};
        json key2 = {"id":60, name:"john"};
        result <- chn, key2;
        result <- chn;
        result <- chn, key;
        return result;
    }
}

channel<json> chn2;

function multipleChannels() returns json {
    worker w2 {
        json msg = {"payment":10000};
        json msg2 = {"payment":50000};
        json msg3 = {"payment":60000};
        json key = {"id":50, name:"john"};
        msg -> chn2;
        msg3 -> chn2,key;
        msg2 -> chn,key;
    }

    worker w1 {
        json result;
        json key = {"id":50, name:"john"};
        result <- chn,key;
        result <- chn2, key;
        return result;
    }
}

channel<xml> xmlChn;
function xmlChannels() returns xml {
     worker w2 {
            xml msg = xml `<payment>10000</payment>`;
            xml msg2 = xml `<payment>10000</payment>`;
            xml msg3 = xml `<payment>10000</payment>`;
            xml key = xml `<key><id>50</id><name>john</name></key>`;
            xml key2 = xml `<key><id>60</id><name>john</name></key>`;
            msg -> xmlChn;
            msg2 -> xmlChn,key;
            msg3 -> xmlChn,key2;
        }

        worker w1 {
            xml result;
            xml key = xml `<key><id>50</id><name>john</name></key>`;
            xml key2 = xml `<key><id>60</id><name>john</name></key>`;
            result <- xmlChn, key2;
            result <- xmlChn;
            result <- xmlChn, key;
            return result;
        }
}

channel<int> intChan;
channel<string> strChan;
channel<boolean> boolChan;
channel<byte> byteChan;
channel<float> floatChan;

function primitiveTypeChannels() returns boolean {

    xml key = xml `<key><id>50</id><name>john</name></key>`;
    byte b = 23;
    10 -> intChan, key;
    true -> boolChan, key;
    10.5 -> floatChan, key;
    "message" -> strChan, key;
    b -> byteChan, key;

    int intResult;
    float floatResult;
    byte byteResult;
    string strResult;
    boolean boolResult;
    intResult <- intChan, key;

    if (intResult == 10) {
        floatResult <- floatChan, key;
    } else {
        return false;
    }

    if (floatResult == 10.5) {
        byteResult <- byteChan, key;
    } else {
        return false;
    }

    if (byteResult == b) {
        strResult <- strChan, key;
    } else {
        return false;
    }

    if (strResult == "message") {
        boolResult <- boolChan, key;
        return boolResult;
    }

    return false;

}
