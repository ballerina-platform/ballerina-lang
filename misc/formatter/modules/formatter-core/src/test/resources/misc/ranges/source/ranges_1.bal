import ballerina/io;

public class StringListNode {
    String data;
         StringList     next;
    public      function      init   (         String    data    ) {
             self.data    =     data
                 ;
         }
    public         function       setData        (   String      data   )    {
            self.data     =    data    ;
       }

    public                     function               getData   (      )            returns           String          {
        return      self.data        ;
           }

    public           function        setNext
               (     StringList       next      )      {
        self.next
               =         next         ;
            }

    public    function     getNext           (   ) returns         StringList {
        return          self.next
              ;
           }
                   }

public         class         StringList         {
    StringListNode
           root                   ;
    StringListNode              current        ;

    public            function           init    (      )     {
              StringListNode             node       =      new         StringListNode  (
                               )
             }
      }


  public       function         main      (      )       {
         io       :       println   (       "Hello, World!"     )    ;
      }
