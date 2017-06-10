import ballerina.lang.errors;

function test1 ()(string, string){
    string data;
    try{
        try {
            errors:Error test = { msg: "try block error"};
            data = "assigned";
            throw test;
        } finally {
            errors:Error err = { msg : "finally block error"};
            throw err;
        }
    } catch (errors:Error e) {
        return e.msg, data;
    }
    return "Function end", data;
}

function test2(int a)(string){
    try{
        try {
            errors:Error test = { msg: "try block error"};
            throw test;
        } finally {
            if(a > 10) {
                return "finally block";
            }
        }
    } catch (errors:Error e) {
        return e.msg;
    }
    return "Function end";
}

function test3()(string){
    string data;
    try{
        try {
            data = "try";
            return data;
        } finally {
            data = data + " innerFinally";
        }
    } catch (errors:Error e) {
        return e.msg;
    }finally {
        data = data + " outerFinally";
    }
    return "Function end";
}

struct Test4Val {
    string value;
}

function test4()(Test4Val){
    Test4Val data = { value : ""};
    try{
        try {
            data.value = "try";
            return data;
        } finally {
            data.value = data.value + " innerFinally";
        }
    } catch (errors:Error e) {
        return data;
    } finally {
         data.value = data.value + " outerFinally";
    }
    data.value = "end";
    return data;
}

function test5()(string){
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

