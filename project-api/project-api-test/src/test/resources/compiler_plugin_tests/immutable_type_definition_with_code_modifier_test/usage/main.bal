import immutable_def_test/records;
import samjs/package_comp_plugin_code_modify_add_function as _;

public function main() {
    records:Record & readonly x = {arr: [1, 2]};

    records:ServerError y = records:fn();
    json actualPayload = y.detail().toJson();
}
