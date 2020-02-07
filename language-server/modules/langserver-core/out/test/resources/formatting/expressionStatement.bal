import ballerina/http;
import ballerina/io;

function name1() {
    http:Response res = new;res .setPayload("test payload") ;name2() ;io : println ("test invocation") ;
}

function name2() {

}