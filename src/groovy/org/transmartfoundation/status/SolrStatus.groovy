package org.transmartfoundation.status

class SolrStatus {

	String url
	boolean connected
	boolean rwgAvailable
	int rwgNumberOfRecords
	boolean browseAvailable
	int browseNumberOfRecords
	boolean sampleAvailable
	int sampleNumberOfRecords
	Date lastProbe

	String toString () {
		return "SolrStatus (URL: " + url + ") - probe at: " + lastProbe
	}

        String toHTMLString () {
		return  'url: '                   + url + "<br/>" +
			'connected: '             + connected + "<br/>" +
			'rwgAvailable: '          + rwgAvailable + "<br/>" +
			'rwgNumberOfRecords: '    + rwgNumberOfRecords + "<br/>" +
			'browseAvailable: '       + browseAvailable + "<br/>" +
			'browseNumberOfRecords: ' + browseNumberOfRecords + "<br/>" +
			'sampleAvailable: '       + sampleAvailable + "<br/>" +
			'sampleNumberOfRecords: ' + sampleNumberOfRecords + "<br/>" +
			'lastProbe: '             + lastProbe  + "<br/>"
	}

}
