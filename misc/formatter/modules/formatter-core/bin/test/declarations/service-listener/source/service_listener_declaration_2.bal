    type   Annot      record    {
         string      val  ;
 }   ;

  public    annotation    Annot       RAnnot       on       object     function     ;
public      annotation        ServiceAnnot      on     service   ;

@ServiceAnnot
service       S      /       on     lsn     {
    public    string     magic        =     "The Somebody Else's Problem field"    ;

    @RAnnot      {    val   :    "anot-val"      }     resource    function     get   processRequest()  returns json {
        return   {   output  :   "Hello"   }  ;
            }

    function   createError()      returns         @tainted      error?      {
        return    ()   ;
          }
   }

        service        "sample.demo.foo"      on         listner1          {

    }
