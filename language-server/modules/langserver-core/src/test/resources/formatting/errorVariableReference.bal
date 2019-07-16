type SMS error <string, map<string>>;
type SMA error <string, map<anydata>>;
type CMS error <string, map<string>>;
type CMA error <string, map<anydata>>;

const ERROR1 = "Some Error One";
const ERROR2 = "Some Error Two";

function testBasicErrorVariableWithMapDetails() returns [string, string, string, string, map<string>, string?, string?, string?, map<any>, any, any, any] {
    SMS err1 = error ("Error One", message = "Msg One", detail = "Detail Msg");
    SMA err2 = error ("Error Two", message = "Msg Two", fatal = true);

    string reason11;
    map<string> detail11;
    string reason12;
    string? message12;
    string? detail12;
    string? extra12;

         error(   reason11 ,...detail11)=err1;error(  reason12 , message = message12, detail = detail12, extra = extra12)=err1;

    string reason21;
    map<any> detail21;
    string reason22;
    any message22;
    any detail22;
    any extra22;

         error
  (
reason21
 ,
                   ...
               detail21
            )
       =
           err2
            ;
    error(reason22,     message =message22 ,detail  =  detail22  ,   extra   = extra22   ) = err2;

    error (reason22, message = message22, detail = detail22, extra = extra22
              ) = err2;

    error   (reason22,
           message = message22,
   detail=detail22,
extra = extra22
)=err2;

    return [reason11, reason12, reason21, reason22, detail11, message12, detail12, extra12, detail21, message22, detail22, extra22];
}