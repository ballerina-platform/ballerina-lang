// Negative test: worker without return must report "worker" in diagnostic (issue #42746)
public function main() returns error? {
    worker A returns int {

    }
}
