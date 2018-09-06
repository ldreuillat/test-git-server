package com.dga.clade.nuxeo.core;

import com.dga.clade.nuxeo.constant.Constants;
import com.dga.clade.nuxeo.core.operation.DeleteDocs;
import com.dga.clade.nuxeo.helper.NuxeoHelper;
import com.google.common.collect.Maps;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@Deploy("org.nuxeo.ecm.automation.server")
@LocalDeploy("com.dga.clade.nuxeo.core")
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)

public class TestDeletingDocuments {

    @Inject
    CoreSession session;

    private DocumentModel firstDocument;
    private DocumentModel secondDocument;
    private DocumentModel thirdDocument;

    @Before
    public void createDocuments() throws OperationException {

        //Création des documents
        firstDocument = session.createDocumentModel("/", "Doc01", "File");
        firstDocument.setPropertyValue("dc:title", "mon premier Document");
        firstDocument = session.createDocument(firstDocument);
        session.save();

        secondDocument = session.createDocumentModel("/", "Doc02", "File");
        secondDocument.setPropertyValue("dc:title", "mon deuxieme Document");
        secondDocument = session.createDocument(secondDocument);
        session.save();

        thirdDocument = session.createDocumentModel("/", "Doc03", "File");
        thirdDocument.setPropertyValue("dc:title", "mon troisieme Document");
        thirdDocument = session.createDocument(thirdDocument);
        session.save();
    }

    @Test
    //test de suppression d'une liste de documents
    public void testDeleteDocuments() throws OperationException {
        Map<String, Object> map = Maps.newHashMap();
        List<DocumentModel> documents = new ArrayList<DocumentModel>();
        documents.add(firstDocument);
        documents.add(secondDocument);
        documents.add(thirdDocument);
        map.put("docs", documents);

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob)NuxeoHelper.runOperation(session, null, DeleteDocs.ID, map));
            Assert.assertEquals(Constants.ENTITY_DOCUMENTS, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals(session.getRepositoryName(), object.get(Constants.REPOSITORY));
            Assert.assertEquals(3, object.get(Constants.COUNT));

            Boolean existeDoc1 = session.exists(firstDocument.getRef());
            Assert.assertFalse(existeDoc1);
            Boolean existeDoc2 = session.exists(secondDocument.getRef());
            Assert.assertFalse(existeDoc2);
            Boolean existeDoc3 = session.exists(thirdDocument.getRef());
            Assert.assertFalse(existeDoc3);
        }catch(OperationException e) {
            Assert.assertNotNull(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    //test de suppression avec une liste de documents vide
    public void testDeleteDocumentsEmpty() throws OperationException {

        Map<String, Object> map = Maps.newHashMap();
        List<DocumentModel> documents = new ArrayList<DocumentModel>();
        map.put("docs", documents);

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob)NuxeoHelper.runOperation(session, null, DeleteDocs.ID, map));
            Assert.assertEquals(Constants.ENTITY_EXCEPTION, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals("500", object.get(Constants.STATUS));
            Assert.assertEquals("la liste des documents est vide", object.get(Constants.MESSAGE));
        }catch(Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    //test de suppression avec une liste de documents nulle
    public void testDeleteDocumentsNull() throws OperationException {

        Map<String, Object> map = Maps.newHashMap();

        try {
            NuxeoHelper.runOperation(session, null, DeleteDocs.ID, map);
        }catch(OperationException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    //test de suppression d'une liste de documents dont un document n'existe pas.
    public void testDeleteDocumentsNotExist() throws OperationException {

        Map<String, Object> map = Maps.newHashMap();
        List<DocumentModel> documents = new ArrayList<DocumentModel>();
        documents.add(secondDocument);
        session.removeDocument(firstDocument.getRef());
        documents.add(firstDocument);
        map.put("docs", documents);

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob)NuxeoHelper.runOperation(session, null, DeleteDocs.ID, map));
            Assert.assertEquals(Constants.ENTITY_EXCEPTION, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals("500", object.get(Constants.STATUS));
            Assert.assertEquals("le document : mon premier Document n'existe pas", object.get(Constants.MESSAGE));

            Boolean existeDoc = session.exists(secondDocument.getRef());
            Assert.assertTrue(existeDoc);
        }catch(Exception e) {
            Assert.assertTrue(false);
        }
    }
}
