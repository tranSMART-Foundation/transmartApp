import com.recomdata.export.ExportColumn
import com.recomdata.export.ExportRowNew
import com.recomdata.export.ExportTableNew
import groovy.sql.Sql
import org.apache.commons.io.IOUtils
import java.io.Reader
import org.transmartproject.core.exceptions.InvalidRequestException
import org.transmartproject.core.querytool.ConstraintByOmicsValue

import static org.transmart.authorization.QueriesResourceAuthorizationDecorator.checkQueryResultAccess
/**
 * Author: Denny Verbeeck (dverbeec@its.jnj.com)
 */
class HighDimensionQueryService {

    def dataSource
    def i2b2HelperService
    def highDimensionResourceService

    def getHighDimensionalConceptSet(String result_instance_id1, String result_instance_id2) {
        def result = []

        if (result_instance_id1) result.addAll(getHighDimensionalConceptKeysInSubset(result_instance_id1));
        if (result_instance_id2) result.addAll(getHighDimensionalConceptKeysInSubset(result_instance_id2));

        result
    }

    /**
     *
     * @param resultInstanceId
     * @return
     */
    def getHighDimensionalConceptKeysInSubset(String resultInstanceId) {
        Sql sql = new Sql(dataSource)
        String sqlt = """SELECT REQUEST_XML FROM QT_QUERY_MASTER c INNER JOIN QT_QUERY_INSTANCE a
		    ON a.QUERY_MASTER_ID=c.QUERY_MASTER_ID INNER JOIN QT_QUERY_RESULT_INSTANCE b
		    ON a.QUERY_INSTANCE_ID=b.QUERY_INSTANCE_ID WHERE RESULT_INSTANCE_ID = ?""";

        String xmlrequest = "";
        def concepts = []
        sql.eachRow(sqlt, [resultInstanceId], { row ->
            def xml
            try {
                def clobObject = row.request_xml
                Reader stream = clobObject.getCharacterStream()
                StringWriter w = new StringWriter()
                IOUtils.copy(stream, w)
                String clobAsString = w.toString()
                xml = new XmlSlurper().parseText(clobAsString)
            } catch (exception) {
                throw new InvalidRequestException('Malformed XML document: ' +
                        exception.message, exception)
            }
            xml.panel.each { p ->
                p.item.each { i ->
                    if (i.constrain_by_omics_value.size()) {
                        def constraint_params = [:]
                        constraint_params.concept_key = i.item_key.toString()
                        constraint_params.omics_selector = i.constrain_by_omics_value.omics_selector.toString()
                        constraint_params.omics_value_type = i.constrain_by_omics_value.omics_value_type.toString()
                        constraint_params.omics_value_operator = i.constrain_by_omics_value.omics_value_operator.toString()
                        constraint_params.omics_value_constraint = i.constrain_by_omics_value.omics_value_constraint.toString()
                        constraint_params.omics_projection_type = i.constrain_by_omics_value.omics_projection_type.toString()
                        constraint_params.omics_property = i.constrain_by_omics_value.omics_property.toString()
                        concepts.add(constraint_params)
                    }
                }
            }
        })
        log.info "High dimensional concepts found: " + concepts
        concepts
    }

    /**
     * Adds a column of high dimensional data to the grid export table
     */
    def ExportTableNew addHighDimConceptDataToTable(ExportTableNew tablein, omics_constraint, String result_instance_id) {
        checkQueryResultAccess result_instance_id

        def concept_key = omics_constraint.concept_key
        def selector = omics_constraint.omics_selector
        String columnid = (concept_key + selector + omics_constraint.omics_projection_type).encodeAsSHA1()
        String columnname = selector + " in " + concept_key

        /*add the column to the table if its not there*/
        if (tablein.getColumn("subject") == null) {
            tablein.putColumn("subject", new ExportColumn("subject", "Subject", "", "string"));
        }
        if (tablein.getColumn(columnid) == null) {
            tablein.putColumn(columnid, new ExportColumn(columnid, columnname, "", "number"));
        }

        def resource = highDimensionResourceService.getHighDimDataTypeResourceFromConcept(concept_key)

        if (resource != null) {

            def data = resource.getDistribution(
                    new ConstraintByOmicsValue(projectionType: omics_constraint.omics_projection_type,
                            property      : omics_constraint.omics_property,
                            selector      : omics_constraint.omics_selector),
                    concept_key,
                    (result_instance_id == "" ? null : result_instance_id as Long))

            data.each { s, v ->
                String subject = s.toString() // this is a Long
                String value = v.toString() // this is a Double
                /*If I already have this subject mark it in the subset column as belonging to both subsets*/
                if (tablein.containsRow(subject)) /*should contain all subjects already if I ran the demographics first*/ {
                    tablein.getRow(subject).put(columnid, value);
                } else
                /*fill the row*/ {
                    ExportRowNew newrow = new ExportRowNew();
                    newrow.put("subject", subject);
                    newrow.put(columnid, value);
                    tablein.putRow(subject, newrow);
                }
            }

            //pad all the empty values for this column
            for (ExportRowNew row : tablein.getRows()) {
                if (!row.containsColumn(columnid)) {
                    row.put(columnid, "NULL");
                }
            }
        }

        return tablein;
    }
}
