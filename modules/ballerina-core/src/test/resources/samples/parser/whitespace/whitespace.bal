

package  samples.parser   ;


service HelloService {

  @POST {}
  @Path {value:"/tweet"}
  resource tweet (message m) {

  }
}
