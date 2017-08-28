  xmlns     "http://sample.com/wso2/a1"    as    ns0  ;

function test () {

     iterate     (       string         s       :      list      )      {
         // test
           }

       while       (    a        !=         null        )        {
           // test
            if  (a  >   10  ) {
           // test
                 continue;
           }    else if  (a == 10) {
           // test
               break;
           }    else    {
           // test
               break;
           }
           // test
      }

     try    {
         // test
         string  s = "10";
         var    i, _     = (string)s;
         // test
     }  catch     (errors:Error e) {
     // test
     }   finally    {
     // test
     }

     throw           exception         ;

          map    properties           =        {
                            "a"       :      "b"       ,
                            "c"       :      "d"
                          }     ;

          string         s       =      <   int    >   a  ;

     string         s       =      (   int    )   a  ;

    var      a    ,    b   =     getValues    (    )      ;

      xmlns    "http://sample.com/wso2/a1"    as    ns0    ;

     // without namespaceUri
     x1  @  ["foo1"]   =    "bar1"  ;

       // with a new namespaceUri
           x1  @  ["{http://sample.com/wso2/e}foo2"]   =   "bar2"  ;

             // with an existing namespaceUri
                 x1  @  ["{http://sample.com/wso2/f}foo3"]   =   "bar3"  ;
}
