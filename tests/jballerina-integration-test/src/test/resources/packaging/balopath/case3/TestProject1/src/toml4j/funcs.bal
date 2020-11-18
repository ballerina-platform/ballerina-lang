import ballerina/java;

public function getToml() returns handle = @java:Constructor {
    'class: "com.moandjiezana.toml.Toml"
} external;

public function read(handle instance, handle content) returns handle = @java:Method {
    name: "read",
    'class: "com.moandjiezana.toml.Toml",
    paramTypes: ["java.lang.String"]
} external;


public function getString(handle instance, handle key) returns handle = @java:Method {
  name: "getString",
  'class: "com.moandjiezana.toml.Toml"
} external;