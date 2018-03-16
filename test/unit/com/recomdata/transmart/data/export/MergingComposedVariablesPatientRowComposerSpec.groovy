package com.recomdata.transmart.data.export

import org.transmartproject.core.concept.ConceptKey
import org.transmartproject.core.dataquery.Patient
import org.transmartproject.core.dataquery.clinical.ClinicalVariable
import org.transmartproject.core.dataquery.clinical.ClinicalVariableColumn
import org.transmartproject.core.dataquery.clinical.ComposedVariable
import org.transmartproject.core.dataquery.clinical.PatientRow
import spock.lang.Shared
import spock.lang.Specification

class MergingComposedVariablesPatientRowComposerSpec extends Specification {

    interface ComposedClinicalVariableColumn extends ClinicalVariableColumn, ComposedVariable {}

    @Shared study1 = Mock(ComposedClinicalVariableColumn)
    @Shared study2 = Mock(ComposedClinicalVariableColumn)
    @Shared patientRow1 = Mock(PatientRow)
    @Shared patientRow2 = Mock(PatientRow)
    @Shared patientRow3 = Mock(PatientRow)
    @Shared patientRow4 = Mock(PatientRow)

    def setupSpec() {
        def age1 = Mock(ClinicalVariable)
        age1.key >> new ConceptKey('\\\\code\\Public Studies\\Study1\\Demo\\Age\\')
        def gender = Mock(ClinicalVariable)
        gender.key >> new ConceptKey('\\\\code\\Public Studies\\Study1\\Demo\\Gender\\')
        study1.innerClinicalVariables >> [age1, gender]

        def age2 = Mock(ClinicalVariable)
        age2.key >> new ConceptKey('\\\\code\\Public Studies\\Study2\\Demo\\Age\\')
        def sex = Mock(ClinicalVariable)
        sex.key >> new ConceptKey('\\\\code\\Public Studies\\Study2\\Demo\\Sex\\')
        study2.innerClinicalVariables >> [age2, sex]

        def patient1 = Mock(Patient)
        patient1.inTrialId >> 'patient1'
        patientRow1.patient >> patient1
        patientRow1.getAt(study1) >> [ (age1): 56, (gender): 'F' ]

        def patient2 = Mock(Patient)
        patient2.inTrialId >> 'patient2'
        patientRow2.patient >> patient2
        patientRow2.getAt(study2) >> [ (age2): 42, (sex): 'M' ]

        def patient3 = Mock(Patient)
        patient3.inTrialId >> 'patient3'
        patientRow3.patient >> patient3
        patientRow3.getAt(study1) >> [ (age1): 30, (gender): 'M' ]
        patientRow3.getAt(study2) >> [ (age2): 30, (sex): 'M' ]

        def patient4 = Mock(Patient)
        patient4.inTrialId >> 'patient4'
        patientRow4.patient >> patient4
        patientRow4.getAt(study1) >> [ (age1): 34, (gender): 'F' ]
        patientRow4.getAt(study2) >> [ (age2): 35, (sex): 'F' ]
    }

    def 'test merging columns works'() {

        when:
        def testee = new MergingComposedVariablesPatientRowComposer([study1, study2])
        then:
        testee.composeHeader(patientRow1) == [ 'Subject ID', '\\Demo\\Age\\', '\\Demo\\Gender\\', '\\Demo\\Sex\\' ]
        testee.composeRow(patientRow1) == [ 'patient1', '56', 'F', '' ]
        testee.composeRow(patientRow2) == [ 'patient2', '42', '', 'M' ]
        testee.composeRow(patientRow3) == [ 'patient3', '30', 'M', 'M' ]

        when:
        testee.composeRow(patientRow4)
        then:
        def e = thrown(IllegalStateException)
        e.message == "Conflicting values '34' (\\Public Studies\\Study1\\Demo\\Age\\) and '35' (\\Public Studies\\Study2\\Demo\\Age\\) for '\\Demo\\Age\\' column for 'patient4' patient."
    }
}
