documentation{
    Executes a ballerina command
    P{{command}} - Ballerina command
    P{{packageName}} - Package path with necessary flags
    R{{}} - Data piped from the standard output and error output of the process
}
public native function execBallerina(string command, string packagePath) returns (string);