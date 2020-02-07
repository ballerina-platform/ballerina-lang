import ballerina/io;

function name1 () {
 if(true){
              int a = 0;}else if(false){int b = 0;}else{
 int c = 0;}
}

function name2() {
 if (true){
    int a = 0;if(false){
      int d = 0;}else if(true){
         int e = 0;
              }else{
                     int f = 0;
    }
    }else if(false){
  int b = 0;
                if (false) {
      int g = 0;
                      }        else if(true)    {
  int h = 0;
        }else{int i = 0;}
    }else{
                     int c = 0;
  if (false) {int j = 0;
  }else if(true){
 int k = 0;}else{int l = 0;}}
}

function name3() {
    int result = 0;
    if (result is int) {
    } else {
        io:println("File copy completed. The copied file could be located in " +
        dstPath);
    }
}

function name4() {
    string value = "chunked";
    if (value != "chunked" && value != "compress" && value != "deflate"
    && value != "gzip" && value != "identity") {
           res.statusCode = 400;
         res.setPayload("Transfer-Encoding contains invalid data");
         validationErrorFound = true;
       }
}