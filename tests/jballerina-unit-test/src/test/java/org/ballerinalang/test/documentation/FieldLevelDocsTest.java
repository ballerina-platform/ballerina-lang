/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.documentation;

import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.ballerinalang.docgen.generator.model.BClass;
import org.ballerinalang.docgen.generator.model.DefaultableVariable;
import org.ballerinalang.docgen.generator.model.DocPackage;
import org.ballerinalang.docgen.generator.model.Module;
import org.ballerinalang.docgen.generator.model.ModuleDoc;
import org.ballerinalang.docgen.generator.model.Record;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Test class to check record/bClass field-level documentation in docerina.
 */
public class FieldLevelDocsTest {

    // record with module-level documentation
    private Record addressRecord;
    // record with field-level documentation
    private Record personRecord;
    // record with both module-level & field-level documentation
    private Record apartmentRecord;
    // BClass with module-level documentation
    private BClass studentCls;
    // BClass with field-level documentation
    private BClass teacherCls;
    // bClass with both module-level & field-level documentation
    private BClass employeeCls;

    @BeforeClass
    public void setup() throws IOException {
        String sourceRoot =
                "test-src" + File.separator + "documentation" + File.separator + "record_object_fields_project";
        io.ballerina.projects.Project project = BCompileUtil.loadProject(sourceRoot);
        Map<String, ModuleDoc> moduleDocMap = BallerinaDocGenerator.generateModuleDocMap(project);
        DocPackage docerinaDocPackage = BallerinaDocGenerator.getDocsGenModel(moduleDocMap, project.currentPackage()
                .packageOrg().toString(), project.currentPackage().packageVersion().toString());
        Module testModule = docerinaDocPackage.modules.get(0);

        for (Record record : testModule.records) {
            String recordName = record.name;

            if ("Address".equals(recordName)) {
                addressRecord = record;
            } else if ("Person".equals(recordName)) {
                personRecord = record;
            } else if ("Apartment".equals(recordName)) {
                apartmentRecord = record;
            }
        }

        for (BClass cls : testModule.classes) {
            String objName = cls.name;

            if ("Student".equals(objName)) {
                studentCls = cls;
            } else if ("Teacher".equals(objName)) {
                teacherCls = cls;
            } else if ("Employee".equals(objName)) {
                employeeCls = cls;
            }
        }
    }

    @Test(description = "Test records with module-level field docs")
    public void testRecordWithModuleLevelFieldDocs() {
        DefaultableVariable streetField = null;
        DefaultableVariable cityField = null;
        DefaultableVariable countryCodeField = null;

        List<DefaultableVariable> fields = addressRecord.fields;

        for (DefaultableVariable field : fields) {
            String fieldName = field.name;

            if ("street".equals(fieldName)) {
                streetField = field;
            } else if ("city".equals(fieldName)) {
                cityField = field;
            } else if ("countryCode".equals(fieldName)) {
                countryCodeField = field;
            }
        }

        testDescription(streetField, "street of the address\n");
        testDescription(cityField, "city of the address\n");
        testDescription(countryCodeField, "country code of the address\n");
    }

    @Test(description = "Test records with field-level field docs")
    public void testRecordWithFieldLevelFieldDocs() {
        DefaultableVariable nameField = null;
        DefaultableVariable ageField = null;
        DefaultableVariable countryCodeField = null;

        List<DefaultableVariable> fields = personRecord.fields;

        for (DefaultableVariable field : fields) {
            String fieldName = field.name;

            if ("name".equals(fieldName)) {
                nameField = field;
            } else if ("age".equals(fieldName)) {
                ageField = field;
            } else if ("countryCode".equals(fieldName)) {
                countryCodeField = field;
            }
        }

        testDescription(nameField, "name of the person\n");
        testDescription(ageField, "age of the person\n");
        testDescription(countryCodeField, "country code of the person\n");
    }


    @Test(description = "Test records with both module-level & field-level field docs. field-level is the priority")
    public void testRecordWithModuleLevelAndFieldLevelFieldDocs() {
        DefaultableVariable numberField = null;
        DefaultableVariable streetField = null;
        DefaultableVariable countryCodeField = null;

        List<DefaultableVariable> fields = apartmentRecord.fields;

        for (DefaultableVariable field : fields) {
            String fieldName = field.name;

            if ("number".equals(fieldName)) {
                numberField = field;
            } else if ("street".equals(fieldName)) {
                streetField = field;
            } else if ("countryCode".equals(fieldName)) {
                countryCodeField = field;
            }
        }

        testDescription(numberField, "apartment no\n");
        testDescription(streetField, "apartment street\n");
        testDescription(countryCodeField, "apartment country-code\n");
    }

    @Test(description = "Test bClass with module-level field docs")
    public void testObjectWithModuleLevelFieldDocs() {
        DefaultableVariable nameField = null;
        DefaultableVariable ageField = null;

        List<DefaultableVariable> fields = studentCls.fields;

        for (DefaultableVariable field : fields) {
            String fieldName = field.name;

            if ("name".equals(fieldName)) {
                nameField = field;
            } else if ("age".equals(fieldName)) {
                ageField = field;
            }
        }

        testDescription(nameField, "student name\n");
        testDescription(ageField, "student age\n");
    }

    @Test(description = "Test bClass with field-level field docs")
    public void testObjectWithFieldLevelFieldDocs() {
        DefaultableVariable nameField = null;
        DefaultableVariable ageField = null;

        List<DefaultableVariable> fields = teacherCls.fields;

        for (DefaultableVariable field : fields) {
            String fieldName = field.name;

            if ("name".equals(fieldName)) {
                nameField = field;
            } else if ("age".equals(fieldName)) {
                ageField = field;
            }
        }

        testDescription(nameField, "Teacher name\n");
        testDescription(ageField, "Teacher age\n");
    }

    @Test(description = "Test bClass with both module-level & field-level field docs. field-level is the priority")
    public void testObjWithModuleLevelAndFieldLevelFieldDocs() {
        DefaultableVariable empNoField = null;
        DefaultableVariable ageField = null;

        List<DefaultableVariable> fields = employeeCls.fields;

        for (DefaultableVariable field : fields) {
            String fieldName = field.name;

            if ("empNo".equals(fieldName)) {
                empNoField = field;
            } else if ("age".equals(fieldName)) {
                ageField = field;
            }
        }

        testDescription(empNoField, "funny number\n");
        testDescription(ageField, "funny age\n");
    }

    @Test(description = "Test documentation of bClasses with markdown styles")
    public void testObjectDocsWithMarkdownStyles() {
        Assert.assertEquals(teacherCls.description.trim(),
                "`Teacher` object in *school* located in **New York**\n"
                        + "`Senior` teacher of the school");
    }

    @Test(description = "Test documentation of records with markdown styles")
    public void testRecordDocsWithMarkdownStyles() {
        Assert.assertEquals(apartmentRecord.description.trim(),
                "`Apartment` record in the *town*\n"
                        + "`test` documentation row");
    }

    private void testDescription(DefaultableVariable field, String expectedDesc) {
        Assert.assertNotNull(field);
        Assert.assertEquals(field.description, expectedDesc);
    }

}
