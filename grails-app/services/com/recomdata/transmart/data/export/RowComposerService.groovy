package com.recomdata.transmart.data.export

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.transmartproject.core.dataquery.clinical.ComposedVariable

class RowComposerService {

    static transactional = false

    GrailsApplication grailsApplication

    RowComposer build(List<ComposedVariable> variables) {
        def mergeColumns = grailsApplication.config.org.transmart.data.export.clinical.mergeColumns
        if (mergeColumns) {
            if (mergeColumns.startWithPathLevel) {
                new MergingComposedVariablesPatientRowComposer(variables, mergeColumns.startWithPathLevel as Integer)
            } else {
                new MergingComposedVariablesPatientRowComposer(variables)
            }
        } else {
            new ComposedVariablesPatientRowComposer(variables)
        }
    }
}