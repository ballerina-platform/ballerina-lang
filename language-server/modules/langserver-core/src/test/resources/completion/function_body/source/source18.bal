type MyFirstError distinct error;
type MySecondError distinct error;

public type MyErrorDetail record {
    string fileName;
};

type MyIntersectionError MyFirstError & error<MyErrorDetail>;
type MyUnionError MyFirstError | MySecondError;

public function main() {
    My   
}
