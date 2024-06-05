// Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

USCorePatientProfile patientProfile = {
    "extension": [
        {
            "extension": [
                {
                    "url": "tribalAffiliation",
                    "valueCodeableConcept": {
                        "coding": [
                            {
                                "system": "http://terminology.hl7.org/CodeSystem/v3-TribalEntityUS",
                                "code": "187",
                                "display": "Paiute-Shoshone Tribe of the Fallon Reservation and Colony, Nevada"
                            }
                        ],
                        "text": "Shoshone"
                    }
                }
            ]
        }
    ]
};

public type USCorePatientProfile record {|

    Extension[] extension?;
|};

public function main() {
};

public type Extension CodeableConceptExtension|StringExtension|CodingExtension|CodeExtension|IntegerExtension
|Base64BinaryExtension|BooleanExtension|CanonicalExtension|DateExtension|DateTimeExtension|DecimalExtension
|IdExtension|InstantExtension|Integer64Extension|MarkdownExtension|OidExtension|PositiveIntExtension|TimeExtension|UnsignedIntExtension
|UriExtension|UrlExtension|UuidExtension|AddressExtension|AgeExtension|AnnotationExtension|AttachmentExtension|CodeableReferenceExtension
|ContactPointExtension|CountExtension|DistanceExtension|DurationExtension|HumanNameExtension|IdentifierExtension|MoneyExtension
|PeriodExtension|QuantityExtension|RangeExtension|RatioExtension|RatioRangeExtension|ReferenceExtension|SampledDataExtension|SignatureExtension
|TimingExtension|ContactDetailExtension|DataRequirementExtension|ExpressionExtension|ParameterDefinitionExtension
|RelatedArtifactExtension|TriggerDefinitionExtension|UsageContextExtension|AvailabilityExtension|ExtendedContactDetailExtension|DosageExtension
|MetaExtension|ExtensionExtension;

public type uri string;

public type ExtensionExtension record {|
    *Element;

    uri url;
    Extension[] extension?;
|};

public type StringExtension record {|
    *Element;

    uri url;
    string valueString;
|};

public type CodingExtension record {|
    *Element;

    uri url;
    Coding valueCoding;
|};

public type CodeExtension record {|
    *Element;

    uri url;
    code valueCode;
|};

public type IntegerExtension record {|
    *Element;

    uri url;
|};

public type CodeableConcept record {|
    *Element;
    string id?;
    Extension[] extension?;

    Coding[] coding?;
    string text?;
|};

public type Coding record {|
    *Element;
    string id?;
    Extension[] extension?;

    uri system?;
    string 'version?;
    code code?;
    string display?;
    boolean userSelected?;
|};

public type code string;

public type CodeableConceptExtension record {|
    *Element;

    uri url;
    CodeableConcept valueCodeableConcept;
|};

public type Base64BinaryExtension record {|
    *Element;

    uri url;
|};

public type BooleanExtension record {|
    *Element;

    uri url;
    boolean valueBoolean;
|};

public type CanonicalExtension record {|
    *Element;

    uri url;
|};

public type DateExtension record {|
    *Element;

    uri url;
|};

public type DateTimeExtension record {|
    *Element;

    uri url;
|};

public type DecimalExtension record {|
    *Element;

    uri url;
    decimal valueDecimal;
|};

public type IdExtension record {|
    *Element;

    uri url;
|};

public type InstantExtension record {|
    *Element;

    uri url;
|};

//generate extension record types for type valueInteger64
public type Integer64Extension record {|
    *Element;

    uri url;
|};

public type MarkdownExtension record {|
    *Element;

    uri url;
|};

public type OidExtension record {|
    *Element;

    uri url;
|};

public type PositiveIntExtension record {|
    *Element;

    uri url;
|};

public type TimeExtension record {|
    *Element;

    uri url;
|};

public type UnsignedIntExtension record {|
    *Element;

    uri url;
|};

public type UriExtension record {|
    *Element;

    uri url;
    uri valueUri;
|};

public type UrlExtension record {|
    *Element;

    uri url;
|};

public type UuidExtension record {|
    *Element;

    uri url;
|};

public type AddressExtension record {|
    *Element;

    uri url;
|};

public type AgeExtension record {|
    *Element;

    uri url;
|};

public type AnnotationExtension record {|
    *Element;

    uri url;
|};

public type AttachmentExtension record {|
    *Element;

    uri url;
|};

public type CodeableReferenceExtension record {|
    *Element;

    uri url;
|};

public type ContactPointExtension record {|
    *Element;

    uri url;
|};

public type CountExtension record {|
    *Element;

    uri url;
|};

public type DistanceExtension record {|
    *Element;

    uri url;
|};

public type DurationExtension record {|
    *Element;

    uri url;
|};

public type HumanNameExtension record {|
    *Element;

    uri url;
|};

public type IdentifierExtension record {|
    *Element;

    uri url;
|};

public type MoneyExtension record {|
    *Element;

    uri url;
|};

public type PeriodExtension record {|
    *Element;

    uri url;
|};

public type QuantityExtension record {|
    *Element;

    uri url;
|};

public type RangeExtension record {|
    *Element;

    uri url;
|};

public type RatioExtension record {|
    *Element;

    uri url;
|};

public type RatioRangeExtension record {|
    *Element;

    uri url;
|};

public type ReferenceExtension record {|
    *Element;

    uri url;
|};

public type SampledDataExtension record {|
    *Element;

    uri url;
|};

public type SignatureExtension record {|
    *Element;

    uri url;
|};

public type TimingExtension record {|
    *Element;

    uri url;
|};

public type ContactDetailExtension record {|
    *Element;

    uri url;
|};

public type DataRequirementExtension record {|
    *Element;

    uri url;
|};

public type ExpressionExtension record {|
    *Element;

    uri url;
|};

public type ParameterDefinitionExtension record {|
    *Element;

    uri url;
|};

public type RelatedArtifactExtension record {|
    *Element;

    uri url;
|};

public type TriggerDefinitionExtension record {|
    *Element;

    uri url;
|};

public type UsageContextExtension record {|
    *Element;

    uri url;
|};

public type AvailabilityExtension record {|
    *Element;

    uri url;
|};

public type ExtendedContactDetailExtension record {|
    *Element;

    uri url;
|};

public type DosageExtension record {|
    *Element;

    uri url;
|};

public type MetaExtension record {|
    *Element;
|};

public type Element record {|
    string id?;
    Extension[] extension?;
    Element...;
|};
