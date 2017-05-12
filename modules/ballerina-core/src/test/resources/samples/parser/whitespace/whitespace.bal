

package  samples.parser   ;


import   ballerina.connectors.twitter    as  tw   ;


  service HelloService {

  @POST {}
  @Path {value:"/tweet"}
  resource   tweet    (     message m)  {

      tw:TwitterConnector.tweet(t, "");
  }
}
