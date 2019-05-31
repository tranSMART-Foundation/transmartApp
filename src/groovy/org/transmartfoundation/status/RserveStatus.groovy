package org.transmartfoundation.status

class RserveStatus {

    String url
    boolean connected
    boolean simpleExpressionOK
    boolean librariesOk
    String lastErrorMessage
    Date lastProbe

    String toString () {
        return "RserveStatus (" + url + ") - probe at: " + lastProbe
    }

    String toHTMLString () {
        return 'url: '            + url + "</br>" +
        'connected: '             + connected + "</br>" +
        'simpleExpressionOK: '    + simpleExpressionOK + "</br>" +
        'librariesOk: '           + librariesOk + "</br>" +
        'lastErrorMessage: '      + lastErrorMessage + "</br>" +
        'lastProbe: '             + lastProbe + "</br>"
    }


}
