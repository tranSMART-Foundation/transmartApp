package com.recomdata.transmart.data.export

import groovy.transform.CompileStatic
import org.transmartproject.core.dataquery.clinical.ClinicalVariable
import org.transmartproject.core.dataquery.clinical.ClinicalVariableColumn
import org.transmartproject.core.dataquery.clinical.ComposedVariable
import org.transmartproject.core.dataquery.clinical.PatientRow

import java.util.Map.Entry

@CompileStatic
class MergingComposedVariablesPatientRowComposer extends ComposedVariablesPatientRowComposer {

    public static final String PATH_DELIM = '\\'
    final int startWithPathLevel
    final Map<String, List<ClinicalVariable>> shortenedPathToVariables

    MergingComposedVariablesPatientRowComposer(List<ComposedVariable> variables) {
        super(variables)
        this.startWithPathLevel = 2
        this.shortenedPathToVariables = groupByShortenedPath()
    }

    MergingComposedVariablesPatientRowComposer(List<ComposedVariable> variables, int startWithPathLevel) {
        super(variables)
        this.startWithPathLevel = startWithPathLevel
        this.shortenedPathToVariables = groupByShortenedPath()
    }

    private Map<String, List<ClinicalVariable>> groupByShortenedPath() {
        variables.collectMany { ComposedVariable var -> var.innerClinicalVariables }
                .groupBy { ClinicalVariable variable ->
            getShortenedPath(variable)
        }
    }

    private String getShortenedPath(ClinicalVariable variable) {
        List<String> parts = variable.key.conceptFullName.parts
        assert parts.size() > startWithPathLevel: "The path ${parts} does not have ${startWithPathLevel} level."
        List<String> shortenedParts = parts.subList(startWithPathLevel, parts.size())
        PATH_DELIM + shortenedParts.join(PATH_DELIM) + PATH_DELIM
    }

    List<String> composeHeader(PatientRow row) {
        List<String> header = [SUBJ_ID_TITLE]
        header.addAll(shortenedPathToVariables.keySet())
        return header
    }

    List<String> composeRow(PatientRow row) {
        Map<ClinicalVariable, Object> variableToValue = [:]

        for (ComposedVariable cvar : variables) {
            Map<ClinicalVariable, Object> cvarMap = (Map<ClinicalVariable, Object>) row.getAt((ClinicalVariableColumn) cvar)
            if (cvarMap == null) {
                continue
            }
            for (Entry<ClinicalVariable, Object> varToVal : cvarMap.entrySet()) {
                if (variableToValue.containsKey(varToVal.key) && variableToValue.get(varToVal.key) != varToVal.value) {
                    throw new IllegalStateException(
                            "Conflicting values for ${varToVal.key.key} for ${row.patient.inTrialId} patient.")
                }
                variableToValue[varToVal.key] = varToVal.value
            }
        }

        List<String> composedRow = [row.patient.inTrialId]
        for (Entry<String, List<ClinicalVariable>> variablesGroupEntry : shortenedPathToVariables.entrySet()) {
            String path = null
            java.lang.Object value = null
            for (ClinicalVariable var : variablesGroupEntry.value) {
                if (variableToValue.get(var) != null) {
                    if (value != null && variableToValue.get(var) != value) {
                        throw new IllegalStateException(
                                "Conflicting values '${value}' (${path}) and '${variableToValue.get(var)}' (${var.key.conceptFullName}) for '${variablesGroupEntry.key}' column for '${row.patient.inTrialId}' patient.")
                    }
                    path = var.key.conceptFullName
                    value = variableToValue.get(var)
                }
            }
            composedRow.add(value ? value.toString() : '')
        }

        return composedRow
    }

}
