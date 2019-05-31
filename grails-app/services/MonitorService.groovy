import groovy.sql.Sql

 class MonitorService {

    def grailsApplication
    def dataSource
    def rserveStatusService
    def solrStatusService

     def getObservationCount() {
        Sql sql = new Sql(dataSource)
        String sqlText = "SELECT COUNT(*) FROM i2b2demodata.observation_fact"
        def result = sql.firstRow(sqlText)
        return result.values()[0]
    }

    def getLoadedStudies() {
        Sql sql = new Sql(dataSource)
        String sqlText = "SELECT c_name FROM i2b2metadata.i2b2 WHERE c_visualattributes = 'FAS'"
        return sql.rows(sqlText).collect({ it.getAt(0)}).join(", ")
    }

     def getAppVersion() {
        return grailsApplication.metadata['app.version']
    }

     def getDatabaseStatusText(isDBError, errorMessage) {
        if (!isDBError) {
            return "Database Connection Status: " + "OK<br/>" +
                    "Observation Count: " + observationCount + "<br/>" +
                    "Loaded Studies: " + loadedStudies + "<br/>" +
        }
        else {
            return "Database Connection Status: " + "ERROR<br/>" +
                    "Last Database Error Message: " + errorMessage + "<br/>"
        }
    }

     def getOtherStatusText() {
        return  "App Version: " + appVersion + "<br/><br/>" +
                "RServe Status <br/>" +
                "-------------------- </br>" +
                rserveStatusService.status.toHTMLString() + "</br>" +
                "Solr Status <br/>" +
                "-------------------- </br>" +
                solrStatusService.status.toHTMLString()
    }



 }
