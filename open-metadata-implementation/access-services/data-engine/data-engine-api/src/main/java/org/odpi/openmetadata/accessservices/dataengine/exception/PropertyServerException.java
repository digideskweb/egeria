/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.accessservices.dataengine.exception;

/**
 * The PropertyServerException is thrown by the Data Engine OMAS when it is not able to communicate with the
 * property server.
 */
public class PropertyServerException extends DataEngineException {
    /**
     * This is the typical constructor used for creating a PropertyServerException.
     *
     * @param httpCode          http response code to use if this exception flows over a rest call
     * @param className         name of class reporting error
     * @param actionDescription description of function it was performing when error detected
     * @param errorMessage      description of error
     * @param systemAction      actions of the system as a result of the error
     * @param userAction        instructions for correcting the error
     */
    public PropertyServerException(int httpCode, String className, String actionDescription, String errorMessage,
                                   String systemAction, String userAction) {
        super(httpCode, className, actionDescription, errorMessage, systemAction, userAction);
    }


    /**
     * This is the constructor used for creating a PropertyServerException that resulted from a previous error.
     *
     * @param httpCode          http response code to use if this exception flows over a rest call
     * @param className         name of class reporting error
     * @param actionDescription description of function it was performing when error detected
     * @param errorMessage      description of error
     * @param systemAction      actions of the system as a result of the error
     * @param userAction        instructions for correcting the error
     * @param caughtError       the error that resulted in this exception.
     */
    public PropertyServerException(int httpCode, String className, String actionDescription, String errorMessage,
                                   String systemAction, String userAction, Throwable caughtError) {
        super(httpCode, className, actionDescription, errorMessage, systemAction, userAction, caughtError);
    }
}
