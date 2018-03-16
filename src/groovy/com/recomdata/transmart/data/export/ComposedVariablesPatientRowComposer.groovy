package com.recomdata.transmart.data.export

import org.transmartproject.core.dataquery.clinical.ComposedVariable
import org.transmartproject.core.dataquery.clinical.PatientRow

class ComposedVariablesPatientRowComposer implements RowComposer<PatientRow> {

    final static String SUBJ_ID_TITLE = 'Subject ID'
    final List<ComposedVariable> variables

    ComposedVariablesPatientRowComposer(List<ComposedVariable> variables) {
        this.variables = variables
    }

    List<String> composeHeader(PatientRow row) {
        [SUBJ_ID_TITLE] +
                variables.collectMany { ComposedVariable var ->
                    row[var].collect { it.key.label }
                }
    }

    List<String> composeRow(PatientRow row) {
        [row.patient.inTrialId] +
                variables.collectMany { ComposedVariable var ->
                    row[var].values()
                }
    }
}
