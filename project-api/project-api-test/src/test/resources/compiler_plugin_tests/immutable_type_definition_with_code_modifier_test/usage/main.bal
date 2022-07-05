import immutable_def_test/defns;
import samjs/package_comp_plugin_code_modify_add_function as _;

public function main() {
    defns:Record & readonly x = {arr: [1, 2]};

    defns:ServerError y = defns:fn();
    json _ = y.detail().toJson();

    defns:Tuple & readonly z = 1;
    defns:Tuple & readonly _ = ["foo", z];

    error a = error("oops!");
    string _ = a.detail().toString();

    defns:RecordTwo & readonly b = {
        ob: object {
            int i = 1;
        }
    };
    map<defns:RecordTwo> & readonly c = {b};
    defns:TupleTwo & readonly _ = c;

    defns:Foo & readonly d;
}
