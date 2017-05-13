

package  samples.parser   ;


import   ballerina.connectors.twitter    as  tw   ;


  service HelloService {

  @POST {}
  @  Path   {    value:"/tweet", value2   :    [     "value2"]     }

  resource   tweet    (     message m)  {

      tw:TwitterConnector.tweet(t, "");
  }
}

native function testNativeFunction (message m, int i) (message) throws exception;

function testBalFunction (message m, int i)
                            (message) throws exception  {


}

native function testNativeFunction2 (message m, int i) (message) throws exception;

connector twitterConnector  (message m)   {


}

connector twitterConnector2  (message m)  {


}

struct  Person   {

}

struct  Student   {

}


typemapper studentToPerson  (Student m)    (Person)     {


}

struct  Teacher   {

}
