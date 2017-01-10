import ballerina.lang.string;
import ballerina.lang.system;

function testValueOf(long l, double d)(string){
    int i;
    //long l; // Related to issue #679
    float f;
    //double d; // Related to issue #679
    string s;
    boolean b;
    string result;

    i = 10;
    //l = 100; // Related to issue #679
    f = 10.1f;
    //d = 10.1; // Related to issue #679
    s = "hello";
    b = false;

    result = string:valueOf(i);
    result = result + string:valueOf(l);
    result = result + string:valueOf(f);
    result = result + string:valueOf(d);
    result = result + string:valueOf(s);
    result = result + string:valueOf(b);
    // 1010010.110.1hellofalse
    return result;
}

function testValueOfJSON()(string){
    json j;
    j = `{"counties" : [{"code":"US" , "currency":"Dollar"}, {"code":"UK" , "currency":"Pound"}]}`;
    return string:valueOf(j);
}

function testValueOfXML()(string){
    xml x;
    x = `<counties><country><code>US</code><currency>Dollar</currency></country><country><code>UK</code><currency>Pound</currency></country></counties>`;
    return string:valueOf(x);
}


function testCommon()(boolean, string){
    string source1;
    string source2;
    string source3;

    string tmp;
    string expectReplace;
    string expectReplaceFirst;
    string msg;
    boolean result;

    source1 = "Hello World..!!! I am Ballerina. This is a ballerina.lang.string test input.";
    source2 = "Hello World..!!!";
    source3 = "aaabaabaab";
    expectReplace = "babbbbb";
    expectReplaceFirst = "babaabaab";
    msg = "Test Case success.";
    result = true;

    // source1 length is 76.
    if(string:length(source1) != 76){
        msg = "string:length Test case failed.";
        system:log(5, msg);
        result = false;
    }

    if(!string:contains(source1, "test")){
        msg = "string:contains Test case failed.";
        system:log(5, msg);
        result = false;
    }

    if(!string:equalsIgnoreCase(source2, "heLlo woRld..!!!")){
        msg = "string:equalsIgnoreCase Test case Failed.";
        system:log(5, msg);
        result = false;
    }

    if(!string:hasPrefix(source2, "Hello")){
        msg = "string:hasPrefix Test case Failed.";
        system:log(5, msg);
        result = false;
    }

    if(!string:hasSuffix(source2, "..!!!")){
        msg = "string:hasSuffix Test case Failed.";
        system:log(5, msg);
        result = false;
    }

    if(string:indexOf(source2, "World")!=6){
        msg = "tring:indexOf Test case Failed.";
        system:log(5, msg);
        result = false;
    }

    if(string:lastIndexOf(source2, "l")!=9){
        msg = "string:lastIndexOf Test case Failed.";
        system:log(5, msg);
        result = false;
    }

    tmp = string:replace(source3, "aa" , "b");
    if(expectReplace != tmp){
        msg ="string:replace Test case Failed. Found : " + tmp + ", expected : " + expectReplace;
        system:log(5, msg);
        result = false;
    }

    tmp = "";
    tmp = string:replaceAll(source3, "aa" , "b");
    if(expectReplace != tmp){
        msg ="string:replaceAll Test case Failed. Found : " + tmp + ", expected : " + expectReplace;
        system:log(5, msg);
        result = false;
    }

    tmp = "";
    tmp = string:replaceFirst(source3, "aa" , "b");
    if(expectReplaceFirst != tmp){
        msg ="string:replaceFirst Test case Failed. Found : " + tmp + ", expected : " + expectReplaceFirst;
        system:log(5, msg);
        result = false;
    }

    tmp = "";
    tmp = string:toLowerCase(source2);
    if(tmp!="hello world..!!!"){
        msg ="string:toLowerCase Test case Failed. Found :" + tmp;
        system:log(5, msg);
        result = false;
    }


    tmp = "";
    tmp = string:toUpperCase(source2);
    if(tmp!="HELLO WORLD..!!!"){
        msg ="string:toUpperCase Test case Failed. Found :" + tmp;
        system:log(5, msg);
        result = false;
    }

    tmp = string:trim(" Hello  World  ..!!! ");
    if(tmp!="Hello  World  ..!!!"){
        msg ="string:trim Test case Failed. Found :" + tmp;
        system:log(5, msg);
        result = false;
    }

    return result, msg;
}
