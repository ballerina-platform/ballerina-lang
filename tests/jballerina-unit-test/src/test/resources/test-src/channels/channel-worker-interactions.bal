
channel<json> chn = new;

function workerWithChannels() returns json {
    worker w2 {
        json key = {name:"john", "id":50};
        json msg = {"payment":10000};
        msg -> chn, key;
    }

    json key = {"id":50, name:"john"};
    json result = {};
    result = <- chn, key;
    return result;
}

function sendBeforeReceive() returns json {
    worker w2 {
        json key = {name:"john", "id":50};
        json msg = {"payment":10000};
        msg -> chn, key;
    }

    json key = {"id":50, name:"john"};
    json result = {};
    result = <- chn, key;
    return result;
}

function nullKeyChannels() returns json {
    worker w2 {
        json msg = {"payment":10000};
        msg -> chn;
    }

    json result = {};
    result = <- chn;
    return result;
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

    json result = {};
    json key = {"id":50, name:"john"};
    json key2 = {"id":60, name:"john"};
    result = <- chn, key2;
    result = <- chn;
    result = <- chn, key;
    return result;
}

channel<json> chn2 = new;

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

    json result = {};
    json key = {"id":50, name:"john"};
    result = <- chn,key;
    result = <- chn2, key;
    return result;
}

channel<xml> xmlChn = new;
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

        xml result = xml `key`;
        xml key = xml `<key><id>50</id><name>john</name></key>`;
        xml key2 = xml `<key><id>60</id><name>john</name></key>`;
        result = <- xmlChn, key2;
        result = <- xmlChn;
        result = <- xmlChn, key;
        return result;
}

channel<int> intChan = new;
channel<string> strChan = new;
channel<boolean> boolChan = new;
channel<byte> byteChan = new;
channel<float> floatChan = new;

function primitiveTypeChannels() returns boolean {

    xml key = xml `<key><id>50</id><name>john</name></key>`;
    byte b = 23;
    10 -> intChan, key;
    true -> boolChan, key;
    10.5 -> floatChan, key;
    "message" -> strChan, key;
    b -> byteChan, key;

    int intResult = 0;
    float floatResult = 0;
    byte byteResult = 0;
    string strResult = "";
    boolean boolResult = false;
    intResult = <- intChan, key;

    if (intResult == 10) {
        floatResult = <- floatChan, key;
    } else {
        return false;
    }

    if (floatResult == 10.5) {
        byteResult = <- byteChan, key;
    } else {
        return false;
    }

    if (byteResult == b) {
        strResult = <- strChan, key;
    } else {
        return false;
    }

    if (strResult == "message") {
        boolResult = <- boolChan, key;
        return boolResult;
    }

    return false;

}
