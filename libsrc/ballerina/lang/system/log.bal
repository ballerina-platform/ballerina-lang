package ballerina.lang.system;

native function log(int logLevel, string s);

native function print(string s);
native function print(int i);
native function print(long l);
native function print(float f);
native function print(double d);
native function print(xml e);
native function print(json j);

native function println(string s);
native function println((int i);
native function println(long l);
native function println(float f);
native function println(double d);
native function println(xml e);
native function println(json j);

native function epochTime() (long)

native function currentTimeMillis() (long)