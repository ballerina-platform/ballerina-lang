import project.module2;

public type AppErrorData record {
    string code;
};

public type AppError error<AppErrorData>;

public type AnyError AppError|module2:Mod1Error;
