import com.recomdata.export.ExportTableNew
import grails.converters.JSON
import org.jfree.chart.servlet.ChartDeleter
import org.jfree.chart.servlet.ServletUtilities
import org.transmart.searchapp.AccessLog
import org.transmart.searchapp.AuthUser
import org.transmartproject.core.users.User

import javax.servlet.ServletException
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpSession

class ChartController {

    def index = {}

    def i2b2HelperService
    def springSecurityService
    def chartService
    def accessLogService
    User currentUserBean
    def highDimensionQueryService


    def displayChart = {
        HttpSession session = request.getSession();
        String filename = request.getParameter("filename");
        log.trace("Trying to display:" + filename)
        if (filename == null) {
            throw new ServletException("Parameter 'filename' must be supplied");
        }

        //  Replace ".." with ""
        //  This is to prevent access to the rest of the file system
        filename = ServletUtilities.searchReplace(filename, "..", "");

        //  Check the file exists
        File file = new File(System.getProperty("java.io.tmpdir"), filename);
        if (!file.exists()) {
            throw new ServletException("File '" + file.getAbsolutePath()
                    + "' does not exist");
        }

        //  Check that the graph being served was created by the current user
        //  or that it begins with "public"
        boolean isChartInUserList = false;
        ChartDeleter chartDeleter = (ChartDeleter) session.getAttribute(
                "JFreeChart_Deleter");
        if (chartDeleter != null) {
            isChartInUserList = chartDeleter.isChartAvailable(filename);
        }

        boolean isChartPublic = false;
        if (filename.length() >= 6) {
            if (filename.substring(0, 6).equals("public")) {
                isChartPublic = true;
            }
        }

        boolean isOneTimeChart = false;
        if (filename.startsWith(ServletUtilities.getTempOneTimeFilePrefix())) {
            isOneTimeChart = true;
        }

        //if (isChartInUserList || isChartPublic || isOneTimeChart) {
        /*Code change by Jeremy Isikoff, Recombinant Inc. to always serve up images*/

        //  Serve it up
        ServletUtilities.sendTempFile(file, response);
        return;
    }

    /**
     * Action to get the counts for the children of the passed in concept key
     */
    def childConceptPatientCounts = {

        def paramMap = params;

        def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
        log.trace("Called childConceptPatientCounts action in ChartController")
        log.trace("User is:" + user.username);
        log.trace(user.toString());
        def concept_key = params.concept_key;
        log.trace("Requested counts for parent_concept_path=" + concept_key);
        def counts = i2b2HelperService.getChildrenWithPatientCountsForConcept(concept_key,user)
        def access = i2b2HelperService.getChildrenWithAccessForUserNew(concept_key, user)
        log.trace("access:" + (access as JSON));
        log.trace("counts = " + (counts as JSON))

        def obj = [counts: counts, accesslevels: access, test1: "works"]
        render obj as JSON
    }

    /**
     * Action to get the patient count for a concept
     */
    def conceptPatientCount = {
        String concept_key = params.concept_key;
        PrintWriter pw = new PrintWriter(response.getOutputStream());
        pw.write(i2b2HelperService.getPatientCountForConcept(concept_key).toString());
        pw.flush();
    }

    /**
     * Action to get the distribution histogram for a concept
     */
    def conceptDistribution = {

        // Lets put a bit of 'audit' in here
        new AccessLog(username: springSecurityService.getPrincipal().username, event: "DatasetExplorer-Set Value Concept Histogram", eventmessage: "Concept:" + params.concept_key, accesstime: new java.util.Date()).save()

        // We retrieve the result instance ids from the client
        def concept = params.concept_key ?: null

        // We retrieve the highdimension parameters from the client, if they were passed
        def omics_params = [:]
        params.findAll { k, v ->
            k ==~ /omics_/
        }.each { k, v ->
            omics_params[k] = v
        }

        // Collect concept information
        // We need to force computation for an empty instance ID
        concept = chartService.getConceptAnalysis(concept: i2b2HelperService.getConceptKeyForAnalysis(concept), omics_params: omics_params, subsets: [ 1: [ exists: true, instance : "" ], 2: [ exists: false ], commons: [:]], chartSize : [width : 245, height : 180])

        PrintWriter pw = new PrintWriter(response.getOutputStream());
        pw.write(concept.commons.conceptHisto)
        pw.flush();
    }


    def conceptDistributionForSubset = {

        // Lets put a bit of 'audit' in here
        new AccessLog(username: springSecurityService.getPrincipal().username, event: "DatasetExplorer-Set Value Concept Histogram for subset", eventmessage: "Concept:" + params.concept_key, accesstime: new java.util.Date()).save()

        // We retrieve the result instance ids from the client
        def concept = params.concept_key ?: null

        // We retrieve the highdimension parameters from the client, if they were passed
        def omics_params = [:]
        params.findAll { k, v ->
            k ==~ /omics_/
        }.each { k, v ->
            omics_params[k] = v
        }

        // Collect concept information
        concept = chartService.getConceptAnalysis(concept: i2b2HelperService.getConceptKeyForAnalysis(concept), omics_params: omics_params, subsets: chartService.getSubsetsFromRequest(params), chartSize : [width : 245, height : 180])

        PrintWriter pw = new PrintWriter(response.getOutputStream());
        pw.write(concept.commons.conceptHisto)
        pw.flush();
    }

    def conceptDistributionWithValues = {
        // Lets put a bit of 'audit' in here
        new AccessLog(username: springSecurityService.getPrincipal().username, event: "DatasetExplorer-Concept Distribution With Values", eventmessage: "Concept:" + params.concept_key, accesstime: new java.util.Date()).save()

        def concept = params.concept_key ?: null

        // We retrieve the highdimension parameters from the client, if they were passed
        def omics_params = [:]
        params.findAll { k, v ->
            k.startsWith("omics_")
        }.each { k, v ->
            omics_params[k] = v
        }
        // Collect concept information
        concept = chartService.getConceptAnalysis(concept: i2b2HelperService.getConceptKeyForAnalysis(concept), omics_params: omics_params, subsets: [ 1: [ exists: true, instance : "" ], 2: [ exists: false ], commons: [:]], chartSize : [width : 245, height : 180])

        render concept as JSON
    }

    /**
     * Gets an analysis for a concept key and comparison
     */
    def analysis = {

        // Lets put a bit of 'audit' in here
        new AccessLog(username: springSecurityService.getPrincipal().username, event: "DatasetExplorer-Analysis by Concept", eventmessage: "RID1:" + params.result_instance_id1 + " RID2:" + params.result_instance_id2 + " Concept:" + params.concept_key, accesstime: new java.util.Date()).save()

        // We retrieve the result instance ids from the client
        def concept = params.concept_key ?: null
        def concepts = [:]


        // We retrieve the highdimension parameters from the client, if they were passed
        def omics_params = [:]
        params.findAll { k, v ->
            k.startsWith("omics_")
        }.each { k, v ->
            omics_params[k] = v
        }
        // We add the key to our cache set
        chartService.keyCache.add(concept)

        // Collect concept information
        concepts[concept] = chartService.getConceptAnalysis(concept: i2b2HelperService.getConceptKeyForAnalysis(concept), omics_params: omics_params, subsets: chartService.getSubsetsFromRequest(params))

        // Time to delivery !
        render(template: "conceptsAnalysis", model: [concepts: concepts])
    }

    /**
     * Action to get the basic statistics for the subset comparison and render them
     */
    def basicStatistics = {

        // Lets put a bit of 'audit' in here
        new AccessLog(username: springSecurityService.getPrincipal().username, event: "DatasetExplorer-Basic Statistics", eventmessage: "RID1:" + params.result_instance_id1 + " RID2:" + params.result_instance_id2, accesstime: new java.util.Date()).save()

        // We clear the keys in our cache set
        chartService.keyCache.clear()

        // This clears the current session grid view data table and key cache
        request.session.setAttribute("gridtable", null);

        // We retrieve all our charts from our ChartService
        def subsets = chartService.computeChartsForSubsets(chartService.getSubsetsFromRequest(params))
        def concepts = chartService.getConceptsForSubsets(subsets)
        concepts.putAll(chartService.getHighDimensionalConceptsForSubsets(subsets))
        // Time to delivery !
        render(template: "summaryStatistics", model: [subsets: subsets, concepts: concepts])
    }

    def analysisGrid = {

        String concept_key = params.concept_key;
        def result_instance_id1 = params.result_instance_id1;
        def result_instance_id2 = params.result_instance_id2;
        def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)

        /*which subsets are present? */
        boolean s1 = (result_instance_id1 == "" || result_instance_id1 == null) ? false : true;
        boolean s2 = (result_instance_id2 == "" || result_instance_id2 == null) ? false : true;

        def al = new AccessLog(username: springSecurityService.getPrincipal().username, event: "DatasetExplorer-Grid Analysis Drag", eventmessage: "RID1:" + result_instance_id1 + " RID2:" + result_instance_id2 + " Concept:" + concept_key, accesstime: new java.util.Date())
        al.save()

        //XXX: session is a questionable place to store this because it breaks multi-window/tab nav
        ExportTableNew table = (ExportTableNew) request.getSession().getAttribute("gridtable");
        if (table == null) {

            table = new ExportTableNew();
            if (s1) i2b2HelperService.addAllPatientDemographicDataForSubsetToTable(table, result_instance_id1, "subset1", user);
            if (s2) i2b2HelperService.addAllPatientDemographicDataForSubsetToTable(table, result_instance_id2, "subset2", user);

            List<String> keys = i2b2HelperService.getConceptKeysInSubsets(result_instance_id1, result_instance_id2);
            Set<String> uniqueConcepts = i2b2HelperService.getDistinctConceptSet(result_instance_id1, result_instance_id2);

            log.debug("Unique concepts: " + uniqueConcepts);
            log.debug("keys: " + keys)

            for (int i = 0; i < keys.size(); i++) {

                if (!i2b2HelperService.isHighDimensionalConceptKey(keys.get(i))) {
                    log.trace("adding concept data for " + keys.get(i));
                    if (s1) i2b2HelperService.addConceptDataToTable(table, keys.get(i), result_instance_id1, user);
                    if (s2) i2b2HelperService.addConceptDataToTable(table, keys.get(i), result_instance_id2, user);
                }
            }

            def highDimConcepts = highDimensionQueryService.getHighDimensionalConceptSet(result_instance_id1, result_instance_id2)
            highDimConcepts.each {
                if (s1) highDimensionQueryService.addHighDimConceptDataToTable(table, it, result_instance_id1)
                if (s2) highDimensionQueryService.addHighDimConceptDataToTable(table, it, result_instance_id2)
            }
        }
        PrintWriter pw = new PrintWriter(response.getOutputStream());

        if (concept_key && !concept_key.isEmpty()) {
            // We retrieve the highdimension parameters from the client, if they were passed
            def omics_params = [:]
            params.findAll { k, v ->
                k.startsWith("omics_")
            }.each { k, v ->
                omics_params[k] = v
            }
            if (omics_params) {
                omics_params.concept_key = concept_key
                if (s1) highDimensionQueryService.addHighDimConceptDataToTable(table, omics_params, result_instance_id1)
                if (s2) highDimensionQueryService.addHighDimConceptDataToTable(table, omics_params, result_instance_id2)
            }
            else {
                String parentConcept = i2b2HelperService.lookupParentConcept(i2b2HelperService.keyToPath(concept_key));
                Set<String> cconcepts = i2b2HelperService.lookupChildConcepts(parentConcept, result_instance_id1, result_instance_id2);

                def conceptKeys = [];
                def prefix = concept_key.substring(0, concept_key.indexOf("\\", 2));

                if (!cconcepts.isEmpty()) {
                    for (cc in cconcepts) {
                        def ck = prefix + i2b2HelperService.getConceptPathFromCode(cc);
                        conceptKeys.add(ck);
                    }
                } else
                    conceptKeys.add(concept_key);

                for (ck in conceptKeys) {
                    if (s1) i2b2HelperService.addConceptDataToTable(table, ck, result_instance_id1, user);
                    if (s2) i2b2HelperService.addConceptDataToTable(table, ck, result_instance_id2, user);
                }
            }

        }
        pw.write(table.toJSONObject().toString(5));
        pw.flush();

        request.getSession().setAttribute("gridtable", table);
    }

    def reportGridTableExport() {

        ExportTableNew gridTable = request.session.gridtable

        def exportedVariablesCsv = gridTable.columnMap.entrySet()
                .collectAll { "${it.value.label} (id = ${it.key})" }.join(', ')

        def trialsCsv = gridTable.rows
                .collectAll { it['TRIAL'] }.unique().join(', ')

        accessLogService.report(currentUserBean, 'Grid View Data Export',
                eventMessage: "User (IP: ${request.getHeader('X-FORWARDED-FOR') ?: request.remoteAddr}) just exported" +
                        " data for trial(s) (${trialsCsv}): variables (${exportedVariablesCsv}) measurements for the" +
                        " following patient set(s): " +
                        [params.result_instance_id1, params.result_instance_id2].findAll().join(', '),
                requestURL: request.forwardURI)

        render 'ok'
    }

    def clearGrid = {
        log.debug("Clearing grid");
        request.getSession().setAttribute("gridtable", null);
        log.debug("Setting export filename to null, since there is nothing to export")
        request.getSession().setAttribute("expdsfilename", null);
        PrintWriter pw = new PrintWriter(response.getOutputStream());
        response.setContentType("text/plain");
        pw.write("grid cleared!");
        pw.flush();
    }


    def exportGrid = {
        byte[] bytes = ((ExportTableNew) request.getSession().getAttribute("gridtable")).toCSVbytes();
        int outputSize = bytes.length;
        //response.setContentType("application/vnd.ms-excel");
        response.setContentType("text/csv");
        response.setHeader("Content-disposition", "attachment; filename=" + "export.csv");
        response.setContentLength(outputSize);
        ServletOutputStream servletoutputstream = response.getOutputStream();
        servletoutputstream.write(bytes);
        servletoutputstream.flush();
    }
}

