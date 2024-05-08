import record_field_type_from_imported_module.subModule_1;

type RecUsed record {
    subModule_1:Baz x;
};

type RecUnused record {
    subModule_1:Corge x;
};

public function main() {
    RecUsed rec = {
        x: 1
    };
}
