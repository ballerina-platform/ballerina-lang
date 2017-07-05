import ballerina.lang.system;

const string envVar = system:getEnv("env_var");

function accessConstant() (string) {
    return envVar;
}
