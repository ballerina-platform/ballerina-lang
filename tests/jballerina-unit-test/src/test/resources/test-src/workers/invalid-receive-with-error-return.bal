import ballerina/io;


type TrxError error<string, TrxErrorData>;

type TrxErrorData record {|
    string message?;
    error cause?;
    string data = "";
|};

public function main() {
    worker w1{
      int i = 2;
      error? success = i ->> w2;
      io:println("w1");
    }

    worker w2 returns TrxError|boolean{
      int j = 25;
      if(false){
           TrxError err = error("trxErr", data = "test");
           return err;
      }
      j = <- w1;
      io:println(j);
      return true;
    }
}

