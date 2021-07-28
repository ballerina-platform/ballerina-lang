service / on new http:Listener(8080) {

    # This function has a doc with multiple lines. Once updated, it should
    # preserve these lines
    #
    # + path - Resource path parameter    
    # + caller - Parameter Description    
    # + req - Parameter Description
    resource function get getResource/[int id]/[string...path](http:Caller caller, http:Request req) {
    }
}
