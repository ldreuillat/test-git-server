package com.dga.clade.nuxeo.exception;

import org.nuxeo.ecm.automation.server.jaxrs.RestOperationException;

/**
 * Classe d'exception des appels REST Automation
 * @Author ldreuillat
 * @Date 17/07/2018
 * @Version 1.0
 **/
public class NuxeoCladeException extends RestOperationException {
    public NuxeoCladeException(String message, Throwable cause) {
        super(message, cause);
    }

    public NuxeoCladeException(String message) {
        super(message);
    }

    public NuxeoCladeException(Throwable cause) {
        super(cause);
    }
}
