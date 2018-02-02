const int const1 = 0;
const string const2 = "test const";

connector testClientConnector (int param1,int param2) {
    action testAction (int param3,int param4) (int return1,int return2) {
        int var1 = return2;
        int var2 = return1;
        v
        return var1, var2;
    }
}