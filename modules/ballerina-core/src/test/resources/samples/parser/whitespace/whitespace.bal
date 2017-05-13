

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
