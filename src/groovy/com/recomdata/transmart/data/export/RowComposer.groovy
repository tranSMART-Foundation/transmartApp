package com.recomdata.transmart.data.export

import org.transmartproject.core.dataquery.DataRow

interface RowComposer<R extends DataRow> {
    List<String> composeHeader(R row)

    List<String> composeRow(R row)
}