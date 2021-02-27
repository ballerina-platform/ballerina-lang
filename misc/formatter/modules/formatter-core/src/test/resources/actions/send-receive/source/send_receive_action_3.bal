public function foo() {


    @someAnnotate  {



    }



           worker   w1             returns
                int
                       {
        int i = 100;
        () send = i ->> w2;
                return
            0       ;        }




    @anotherAnnotate
           {
           a  :         "worker",
              b      :      "2"
    }


          worker w2
                    returns
                        string        {
        int lw;
        lw = <- w1;
             return
                   "abc"
                      ;
    }

}



