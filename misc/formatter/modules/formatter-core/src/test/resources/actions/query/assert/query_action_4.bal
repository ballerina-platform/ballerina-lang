public function main() {
    var c = from var i in 0 ... 5
        select i
        where i %2 != 0;
}
