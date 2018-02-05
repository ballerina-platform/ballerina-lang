    connector   test  (    )   {

    }

    connector    test    (  string     a  )     {

    }

    connector   test  (    string   a    ,   string     b  )     {

    }

    function main (string[] args) {
          TestConnector         tc       =        create         TestConnector() ;
             tc      .     actionHello("Chanaka")       ;
        tc      .       actionHello()       ;
    }

    connector TestConnector (       ) {

        action actionHello          (TestConnector testConnector,        string      greeting) {
            system:println("Hello " + greeting);
        }
    }

              connector            IntroductionConnector         <TestConnector testC>         (      ) {

                                    action actionHello (IntroductionConnector introConnector, string name) {
    system:println("I'm " + name);
    }
    }

