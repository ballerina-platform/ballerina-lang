
public type balCommand "run" | "docker" | "build" | "install" | "uninstall" | "pull" | "push" | "init" | "serach" |
"doc" | "grpc" | "swagger" | "test" | "version" | "encrypt";

documentation{
    Executes a ballerina command
    P{{command}} - Ballerina command
    P{{packageName}} - Package path with necessary flags
    R{{}} - Data piped from the standard output and error output of the process
}
public native function execBallerina(@sensitive balCommand command, @sensitive string packagePath) returns (string);