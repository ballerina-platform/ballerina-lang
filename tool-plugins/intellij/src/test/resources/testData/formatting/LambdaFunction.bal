import ballerina.lang.time;

function test1 () (int) {
function    (string, string)        returns      (time:Time)         createTime         =        time:parse;
time    :   Time   c     =   createTime(   "2017-07-20T00:00:00.000-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ"   )  ;
return   c    .   time;
}
