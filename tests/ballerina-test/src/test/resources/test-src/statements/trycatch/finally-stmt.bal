function test1 () returns (string, string){
    string data;
    try{
        try {
            error test = { message: "try block error"};
            data = "assigned";
            throw test;
        } finally {
            error err = { message : "finally block error"};
            throw err;
        }
    } catch (error e) {
        return (e.message, data);
    }
    return ("Function end", data);
}

function test2(int a) returns (string){
    try{
        try {
            error test = { message: "try block error"};
            throw test;
        } finally {
            if(a > 10) {
                return "finally block";
            }
        }
    } catch (error e) {
        return e.message;
    }
    return "Function end";
}

function test3() returns (string){
    string data;
    try{
        try {
            data = "try";
            return data;
        } finally {
            data = data + " innerFinally";
        }
    } catch (error e) {
        return e.message;
    }finally {
        data = data + " outerFinally";
    }
    return "Function end";
}

type Test4Val {
    string value;
}

function test4() returns (Test4Val){
    Test4Val data = { value : ""};
    try{
        try {
            data.value = "try";
            return data;
        } finally {
            data.value = data.value + " innerFinally";
        }
    } catch (error e) {
        return data;
    } finally {
         data.value = data.value + " outerFinally";
    }
    data.value = "end";
    return data;
}

function test5() returns (string){
    string value = "";
    try{
        try {
            value = "start";
        } finally {
            int i = 0;
            while(i < 5){
                if(i==3){
                    break;
                }
                i = i + 1;
                value = value + i;
            }
        }
    } finally {
         value = value + "end";
    }
    return value;
}


function test6 () returns (Test4Val) {
    Test4Val data = {value:""};
    try {
        try {
            data.value = "try";
        } finally {
              data.value = data.value + " innerFinally";
              return data;
          }
    } catch (error e) {
        return data;
    } finally {
        data.value = data.value + " outerFinally";
    }
    data.value = "end";
    return data;
}

function test7 () returns (Test4Val) {
    Test4Val data = {value:""};
    try {
        try {
            data.value = "try";
            return data;
        } finally {
              data.value = data.value + " innerFinally";
              try {
                  data.value = data.value + " innerInnerTry";
              }finally {
                   data.value = data.value + " innerInnerFinally";
               }
          }
    } finally {
          data.value = data.value + " outerFinally";
      }
    data.value = "end";
    return data;
}

function test8 () returns (string) {
    try {
        try {
            return "ok";
        } finally {
              return "innerOk";
          }
    } finally {
          return "OuterOk";
      }
    return "OK";
}

function test9 () returns (Test4Val) {
    Test4Val data = {value:""};
    try {
        try {
            data.value = "try";
            return data;
        } finally {
              data.value = data.value + " innerFinally";
              try {
                  data.value = data.value + " innerInnerTry";
                  return data;
              }finally {
                   data.value = data.value + " innerInnerFinally";
               }
          }
    } finally {
          data.value = data.value + " outerFinally";
          return data;
      }
    data.value = "end";
    return data;
}

function testBreak1 () returns (Test4Val) {
    int i = 0;
    Test4Val data = {value:"s"};
    while (i < 5) {
        i = i + 1;
        try {
            data.value = data.value + " t";
            if (i == 3) {
                break;
            }
            data.value = data.value + "-";
        }finally {
             data.value = data.value + "f" + i;
         }
    }
    return data;
}

function testNext1 () returns (Test4Val) {
    int i = 0;
    Test4Val data = {value:"s"};
    while (i < 5) {
        i = i + 1;
        try {
            data.value = data.value + " t";
            if (i == 3) {
                next;
            }
            data.value = data.value + "-";
        }finally {
             data.value = data.value + "f" + i;
         }
    }
    return data;
}

function testAbort1 () returns (Test4Val) {
    int i = 0;
    Test4Val data = {value:"s"};
    transaction {
        try {
            data.value = data.value + " t";
            if (i == 0) {
                abort;
            }
            data.value = data.value + "-";
        }finally {
             data.value = data.value + "f";
         }
    }
    return data;
}

function testAbort2 () returns (Test4Val) {
    int i = 0;
    Test4Val data = {value:"s"};
    transaction {
        while (i < 5) {
            i = i + 1;
            try {
                data.value = data.value + " t";
                if (i == 2) {
                    abort;
                }
                data.value = data.value + "-";
            }finally {
                 data.value = data.value + "f" + i;
             }
        }
    }
    return data;
}
