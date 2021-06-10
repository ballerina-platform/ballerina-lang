
public function main() {
    func();

    getFunction();
}

function func() returns (function () returns int)[] => [() => 1];

function getFunction() returns function()[] {

}
