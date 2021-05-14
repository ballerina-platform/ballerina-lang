import unknown/module;

type R record {|
    module:foo a;
|};

public function main() {
    R & readonly bar = 2;
}
