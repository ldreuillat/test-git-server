package com.dga.clade.nuxeo.helper;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.runtime.api.Framework;

import java.io.IOException;
import java.util.Map;

/**
 * Helper to manipulate standard actions on Documents
 */
public class NuxeoHelper {
    public static final String NXQL_PREFIX = "ecm:mixinType != 'HiddenInNavigation' AND ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted'";

    private NuxeoHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static CoreSession getSession() {
        RepositoryManager repositoryManager = Framework.getService(RepositoryManager.class);

        return CoreInstance.openCoreSession(repositoryManager.getDefaultRepositoryName());
    }

    public static CoreSession getSession(String userName) {
        RepositoryManager repositoryManager = Framework.getService(RepositoryManager.class);

        return CoreInstance.openCoreSession(repositoryManager.getDefaultRepositoryName(), userName);
    }

    public static Object runOperation(CoreSession session, Object objet, String operationID, Map<String, Object> map) throws OperationException {
        OperationContext ctx = new OperationContext(session);
        if (objet != null) {
            ctx.setInput(objet);
        }

        AutomationService automationService = Framework.getService(AutomationService.class);

        Object response = null;

        if (map != null) {
            response = automationService.run(ctx, operationID, map);
        } else {
            response = automationService.run(ctx, operationID);
        }


       return response;
    }

    /**
     * Méthode retournant le contenu au format JSONArray d'un Blob
     * @param blob
     * @return
     * @throws IOException
     */
    public static JSONArray getJSONArrayFromBlob(Blob blob) throws IOException {
        return JSONArray.fromObject(blob.getString());
    }

    /**
     * Méthode retournant le contenu au format JSONObject d'un Blob
     * @param blob
     * @return
     * @throws IOException
     */
    public static JSONObject getJSONObjectFromBlob(Blob blob) throws IOException {
        return JSONObject.fromObject(blob.getString());
    }

    /**
     * Affichage générique des informations du document dans un log
     * @param doc
     * @param modifier
     * @return String
     */
    public static String displayDocInfos(DocumentModel doc, String modifier){
        return doc.getName() + " [" + doc.getType() + "] User : " + modifier;
    }
}
