

package  samples.parser   ;


import   ballerina.connectors.twitter    as  tw   ;


  service HelloService {

  any SERVICE_VAR1  =   2    ;



  @POST {}
  @  Path   {    value:"/tweet", value2   :    [     "value2"]     }

  resource   tweet    (     message    m   ,   int i)  {

      tw:TwitterConnector.tweet(t, "");

    worker  SampleWorker   {

      }

  }
}

native function testNativeFunction (message m, int i) (message);

function testBalFunction (message m, int i)
                            (message)  {


}

native function testNativeFunction2 (message m, int i) (message) ;

connector twitterConnector  (message m)   {

    action tweet  (   Twitter t, string msg)    (message )   {

    }

}

connector twitterConnector2  (message m)  {


}

struct  Person   {

}

struct  Student   {

}


typemapper studentToPerson  (Student m)    (Person)     {


}

const int  SAMPLE_CONST   =    30     ;


struct  Teacher   {

}


annotation Util  attach   service, resource, connector     {

}

any GLOBAL_VAR1  =   2    ;


