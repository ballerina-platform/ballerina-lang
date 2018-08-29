
channel<json> chn;

function workerWithChannels() returns json {
    worker w1 {
        json key = {"id":50, name:"john"};
        json result;
        result <- chn, key;
        return result;
    }

    worker w2 {
        json key = {"id":50, name:"john"};
        json msg = {"payment":10000};
        msg, key -> chn;
    }
}

function sendBeforeReceive() returns json {
    worker w2 {
        json key = {"id":50, name:"john"};
        json msg = {"payment":10000};
        msg, key -> chn;
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
        msg2,key -> chn;
        msg3,key2 -> chn;
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
        msg3,key -> chn2;
        msg2,key -> chn;
    }

    worker w1 {
        json result;
        json key = {"id":50, name:"john"};
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
            msg2,key -> xmlChn;
            msg3,key2 -> xmlChn;
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

function primitiveTypeChannels() returns float {
    worker w1 {
        xml key = xml `<key><id>50</id><name>john</name></key>`;
        10,key -> intChan;
        string strResult;
        strResult <- strChan, 10;
        true, strResult -> boolChan;
        byte byteResult;
        byteResult <- byteChan, true;
        10.5, byteResult -> floatChan;
    }

    worker w2 {
        xml key = xml `<key><id>50</id><name>john</name></key>`;
        int intResult;
        intResult <- intChan,key;
        "message",intResult -> strChan;
        boolean boolResult;
        boolResult <- boolChan, "message";
        byte b = 23;
        float floatResult;
        if(boolResult) {
            b,true -> byteChan;
            floatResult <- floatChan, b;
        }
        return floatResult;
    }
}