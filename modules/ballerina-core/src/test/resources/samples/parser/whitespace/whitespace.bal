

package  samples.parser   ;


import   ballerina.connectors.twitter    as  tw   ;


  service HelloService {

  @POST {}
  @  Path   {    value:"/tweet", value2   :    [     "value2"]     }

  resource   tweet    (     message m)  {

      tw:TwitterConnector.tweet(t, "");
  }
}
