package com.dga.clade.nuxeo.core;

import com.dga.clade.nuxeo.constant.Constants;
import com.dga.clade.nuxeo.core.operation.MoveDocument;
import com.dga.clade.nuxeo.helper.NuxeoHelper;
import com.google.common.collect.Maps;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import javax.inject.Inject;
import java.util.Map;

@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@LocalDeploy("com.dga.clade.nuxeo.core")
public class TestMoveDocument {

    private static final org.apache.commons.logging.Log log = LogFactory.getLog(MoveDocument.class);
    @Inject
    CoreSession session;

    @Before
    public void initRepo() {
        // Création de la structure de Folders servant aux tests
        DocumentModel dgaFolder =null;
        if (session != null)
            dgaFolder= session.createDocumentModel("/", "DocumentsToMove", "Folder");

        dgaFolder.setPropertyValue("dc:title", "DocumentsToMove");
        session.createDocument(dgaFolder);
        session.save();

        DocumentModel docFolder = session.createDocumentModel("/DocumentsToMove", "Destination", "Folder");
        docFolder.setPropertyValue("dc:title", "Destination");
        docFolder = session.createDocument(docFolder);
        session.save();

        DocumentModel file = session.createDocumentModel("/DocumentsToMove", "DocMove01", "File");
        file.setPropertyValue("dc:title", "DocToMove01");
        file.setPropertyValue("dc:description", "Doc to move 01");
        file = session.createDocument(file);
        session.save();

    }

    @Test
    public void testNonFolderishDestination() {
        DocumentModel fileToMove = session.getDocument(new PathRef("/DocumentsToMove/DocMove01"));
        Assert.assertNotNull(fileToMove);

        try {
            Map<String, Object> map = Maps.newHashMap();
            map.put("destination", fileToMove);
            map.put("value", fileToMove);
            Properties newMetadatas = new Properties();
            newMetadatas.put("dc:description", "");
            map.put("metadatas", newMetadatas);

            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob)NuxeoHelper.runOperation(session, null, MoveDocument.ID, map));
            Assert.assertEquals(Constants.ENTITY_EXCEPTION, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals("500", object.get(Constants.STATUS));
            Assert.assertEquals("La destination doit être de type conteneur : DocMove01 [File] User : Administrator", object.get(Constants.MESSAGE));
        }catch(Exception e) {
            Assert.assertFalse(true);
        }
    }

    @Test
    public void testSameDestination() {
        DocumentModel destination = session.getDocument(new PathRef("/DocumentsToMove"));
        Assert.assertNotNull(destination);
        DocumentModel fileToMove = session.getDocument(new PathRef("/DocumentsToMove/DocMove01"));
        Assert.assertNotNull(fileToMove);

        try {
            Map<String, Object> map = Maps.newHashMap();
            map.put("destination", destination);
            map.put("value", fileToMove);

            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob)NuxeoHelper.runOperation(session, null, MoveDocument.ID, map));
            Assert.assertEquals(Constants.ENTITY_EXCEPTION, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals("500", object.get(Constants.STATUS));
            Assert.assertEquals("Le dossier destination ne doit pas etre le parent de la source : DocumentsToMove [Folder] User : Administrator", object.get(Constants.MESSAGE));
        }catch(Exception e) {
            Assert.assertFalse(true);
        }
    }

    @Test
    public void testMove() {

        log.info("---Test de l'opéation MoveDocument");
        DocumentModel folderDest = session.getDocument(new PathRef("/DocumentsToMove/Destination/"));
        Assert.assertNotNull(folderDest);

        DocumentModel fileToMove = session.getDocument(new PathRef("/DocumentsToMove/DocMove01"));
        Assert.assertNotNull(fileToMove);

        Properties props = new Properties();
        props.put("dc:title", "");

        Map<String, Object> map = Maps.newHashMap();
        map.put("metadatas", props);
        map.put("destination", folderDest);
        map.put("value", fileToMove);

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob)NuxeoHelper.runOperation(session, null, MoveDocument.ID, map));

            Assert.assertEquals(Constants.ENTITY_DOCUMENT, object.get(Constants.ENTITY_TYPE));
            JSONObject metadatas = (JSONObject)object.get(Constants.ENTRY);
            String id = (String)metadatas.get("uid");
            Assert.assertNotNull(id);
            Assert.assertEquals("DocToMove01", metadatas.get("dc:title"));
            Assert.assertNull(metadatas.get("dc:description"));
            Assert.assertNull(metadatas.get("dc:created"));
            Assert.assertEquals("/DocumentsToMove/Destination/DocMove01", metadatas.get("path"));
        }catch(Exception e) {
            Assert.assertFalse(true);
        }
    }

}
