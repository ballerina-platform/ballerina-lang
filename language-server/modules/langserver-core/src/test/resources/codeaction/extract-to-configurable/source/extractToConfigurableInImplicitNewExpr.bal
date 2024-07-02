class MyClient {
    function init(string arg1, string arg2, string arg3, string... arg4) {
    }
}

MyClient cl1 = new ("arg1", "arg2", "arg3", "arg4");
MyClient cl2 = new ("arg1", arg2 = "arg2", arg3 = "arg3");
