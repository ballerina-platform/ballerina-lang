type Details record {|
    readonly int id;
    readonly string country;
|};

public function foo() {
    Details & readonly immutableDetails = {
        id: 112233,
        country: "Sri Lanka"
    };

    readonly readonlyValue = "hello";
}
