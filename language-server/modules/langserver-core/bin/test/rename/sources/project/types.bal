import project.module2;
import project.module1;

public function checkError() returns module1:AppError {
    return error module1:AppError("Custom error", code = "123");
}

public function f1() returns module2:Mod1Error {
    return error module2:Mod1Error("message", data = "");
}
