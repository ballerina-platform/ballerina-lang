import ballerina/io;

public        function                 workerSendToWorker    (      )      returns     int   {
      @strand  {      thread      :   "any"
       }
    worker  w1   {
          int       i      =      40    ;
        i
          ->
          w2    ;
      }

      @strand {     thread    :      "any"     }
    worker      w2       returns         int        {
           int      j      =      25     ;
             j    =    <-
             w1   ;

      io   :   println   (  j  )   ;
        return    j   ;
       }
    int   ret    =    wait   w2   ;

       return
        ret
        +
        1
        ;
}
