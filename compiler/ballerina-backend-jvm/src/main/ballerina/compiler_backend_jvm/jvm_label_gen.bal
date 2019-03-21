type LabelGenerator object {
    map<jvm:Label> bbLabels = {};

    function getLabel(string labelKey) returns jvm:Label {
        var result = self.bbLabels[labelKey];
        if (result is jvm:Label) {
            return result;
        } else {
            jvm:Label label = new;
            self.bbLabels[labelKey] = label;
            return label;
        }
    }
};
