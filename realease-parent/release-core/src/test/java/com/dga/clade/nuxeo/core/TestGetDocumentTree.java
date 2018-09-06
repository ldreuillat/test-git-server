package com.dga.clade.nuxeo.core;

import com.dga.clade.nuxeo.constant.Constants;
import com.dga.clade.nuxeo.core.operation.GetDocumentTree;
import com.dga.clade.nuxeo.helper.NuxeoHelper;
import com.google.common.collect.Maps;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.assertj.core.util.Lists;
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
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@Deploy("org.nuxeo.ecm.automation.server")
@LocalDeploy("com.dga.clade.nuxeo.core")
@RepositoryConfig(init=DefaultRepositoryInit.class, cleanup= Granularity.METHOD)

public class TestGetDocumentTree {

    @Inject
    CoreSession session;

    @Before
    public void initRepo() {
        createEspaceCapiAndEspaceBib();
        createDocNumerique();
        createNoticBib();
    }

    private void createEspaceCapiAndEspaceBib() {
        DocumentModel espace = session.createDocumentModel("/", "EspaceCapi01", "EspaceCapi");
        espace.setPropertyValue("dc:title", "Espace capitalisation 01");
        espace.setPropertyValue("dos:nuid", new Long(000000001));
        espace.setPropertyValue("dos:titre", "Espace capitalisation 01");
        espace.setPropertyValue("dos:auteur", "DGA");
        espace.setPropertyValue("dos:creation", new Date());
        espace.setPropertyValue("dos:modif", new Date());
        espace.setPropertyValue("dos:complement", "Il s'agit de l'espace de capitalisation 01");
        List<String> keywordsList = Lists.newArrayList();
        keywordsList.add("espace");
        keywordsList.add("capitalisation");
        keywordsList.add("numérique");
        espace.setPropertyValue("dos:motscles", (Serializable)keywordsList);
        espace = session.createDocument(espace);

        DocumentModel espace1 = session.createDocumentModel("/", "EspaceBib01", "EspaceBib");
        espace1.setPropertyValue("dc:title", "Espace Biblio 01");
        espace1.setPropertyValue("dos:nuid", new Long(000000002));
        espace1.setPropertyValue("dos:titre", "Espace Biblio 01");
        espace1.setPropertyValue("dos:auteur", "DGA");
        espace1.setPropertyValue("dos:creation", new Date());
        espace1.setPropertyValue("dos:modif", new Date());
        espace1.setPropertyValue("dos:complement", "Il s'agit de l'espace biblio 01");
        List<String> keywordsList2 = Lists.newArrayList();
        keywordsList2.add("espace");
        keywordsList2.add("biblio");
        keywordsList2.add("numérique");
        espace1.setPropertyValue("dos:motscles", (Serializable)keywordsList2);
        espace1 = session.createDocument(espace1);

        DocumentModel dossier = session.createDocumentModel(espace.getPathAsString(), "Dossier01", "Dossier");
        dossier.setPropertyValue("dc:title", "Dossier documentaire 01");
        dossier.setPropertyValue("dos:nuid", new Long(123456789));
        dossier.setPropertyValue("dos:titre", "Dossier documentaire 01");
        dossier.setPropertyValue("dos:auteur", "DGA");
        dossier.setPropertyValue("dos:creation", new Date());
        dossier.setPropertyValue("dos:modif", new Date());
        dossier.setPropertyValue("dos:complement", "Ce dossier comporte les éléments numériques de l'espace de capitalisation 01");
        keywordsList = Lists.newArrayList();
        keywordsList.add("dossier");
        keywordsList.add("dga");
        keywordsList.add("clade");
        keywordsList.add("nuxeo");
        keywordsList.add("ausy");
        dossier.setPropertyValue("dos:motscles", (Serializable)keywordsList);
        session.createDocument(dossier);

        DocumentModel dossier1 = session.createDocumentModel(espace.getPathAsString(), "DossierEmpty01", "Dossier");
        dossier1.setPropertyValue("dc:title", "Dossier documentaire vide 01");
        dossier1.setPropertyValue("dos:nuid", new Long(58945612));
        dossier1.setPropertyValue("dos:titre", "Dossier documentaire vide 01");
        dossier1.setPropertyValue("dos:auteur", "DGA");
        dossier1.setPropertyValue("dos:creation", new Date());
        dossier1.setPropertyValue("dos:modif", new Date());
        dossier1.setPropertyValue("dos:complement", "Ce dossier est un dossier vide de capitalisation de test");
        session.createDocument(dossier1);

        DocumentModel dossier2 = session.createDocumentModel(espace1.getPathAsString(), "Dossier02", "Dossier");
        dossier2.setPropertyValue("dc:title", "Dossier documentaire 02");
        dossier2.setPropertyValue("dos:nuid", new Long(987654321));
        dossier2.setPropertyValue("dos:titre", "Dossier documentaire 02");
        dossier2.setPropertyValue("dos:auteur", "DGA");
        dossier2.setPropertyValue("dos:creation", new Date());
        dossier2.setPropertyValue("dos:modif", new Date());
        dossier2.setPropertyValue("dos:complement", "Ce dossier comporte les éléments bibliographiques de l'espace de capitalisation 01");
        keywordsList = Lists.newArrayList();
        keywordsList.add("documents");
        keywordsList.add("numérique");
        keywordsList.add("clade");
        keywordsList.add("nuxeo");
        dossier2.setPropertyValue("dos:motscles", (Serializable)keywordsList);
        session.createDocument(dossier2);
        session.save();
    }

    private void createDocNumerique() {
        // Création d'un DocNumerique à la racine de EspaceCapi
        DocumentModel espaceCapi = session.getDocument(new PathRef("/EspaceCapi01"));
        DocumentModel docNumRoot = session.createDocumentModel(espaceCapi.getPathAsString(), "DocNumRoot", "DocNumerique");
        docNumRoot.setPropertyValue("clc:titre", "DocNumRoot");
        docNumRoot.setPropertyValue("clc:type", "Avis");
        docNumRoot.setPropertyValue("clc:auteurdoc", "Pierre Richard");
        docNumRoot = session.createDocument(docNumRoot);
        Assert.assertNotNull(docNumRoot);

        // Création de DocNumerique sous un Dossier
        DocumentModel dossier = session.getDocument(new PathRef("/EspaceCapi01/Dossier01"));
        DocumentModel docNum = session.createDocumentModel(dossier.getPathAsString(), "DocNum01", "DocNumerique");
        docNum.setPropertyValue("clc:titre", "DocNum01");
        docNum.setPropertyValue("clc:type", "CCTP");
        docNum.setPropertyValue("clc:auteurdoc", "Robert Martin");
        docNum = session.createDocument(docNum);
        Assert.assertNotNull(docNum);

        DocumentModel docNum2 = session.createDocumentModel(dossier.getPathAsString(), "DocNum02", "DocNumerique");
        docNum2.setPropertyValue("clc:titre", "DocNum02");
        docNum2.setPropertyValue("clc:type", "CCTP2");
        docNum2.setPropertyValue("clc:auteurdoc", "Robert Martin2");
        docNum2 = session.createDocument(docNum2);
        Assert.assertNotNull(docNum2);

        session.save();
    }

    private void createNoticBib() {
        DocumentModel dossier = session.getDocument(new PathRef("/EspaceBib01/Dossier02"));
        DocumentModel notBib = session.createDocumentModel(dossier.getPathAsString(), "Notice01", "NoticeBib");
        notBib.setPropertyValue("clc:titre", "NoticeBib01");
        notBib.setPropertyValue("clc:type", "BB");
        notBib.setPropertyValue("clc:auteurdoc", "toto");
        notBib = session.createDocument(notBib);
        Assert.assertNotNull(notBib);

        DocumentModel notBib2 = session.createDocumentModel(dossier.getPathAsString(), "Notice02", "NoticeBib");
        notBib2.setPropertyValue("clc:titre", "NoticeBib02");
        notBib2.setPropertyValue("clc:type", "BB2");
        notBib2.setPropertyValue("clc:auteurdoc", "toto2");
        notBib2 = session.createDocument(notBib2);
        Assert.assertNotNull(notBib2);

        session.save();
    }

    @Test
    /**
     * Test de récupération d'une arborescence mixte contenant à la fois des Dossier et DocNumerique sans métadonnées
     */
    public void testGetMixDocumentTree() {

        DocumentModel espace = session.getDocument(new PathRef("/EspaceCapi01"));
        Assert.assertNotNull(espace);

        Map<String, Object> map = Maps.newHashMap();
        map.put("value",espace);
        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob)NuxeoHelper.runOperation(session, null, GetDocumentTree.ID, map));

            Assert.assertEquals(Constants.ENTITY_DOCUMENTS, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals(3, object.get(Constants.COUNT));
            JSONArray metadatas = (JSONArray) object.get(Constants.ENTRIES);
            JSONObject objectMeta = (JSONObject) metadatas.get(0);

            Assert.assertEquals("DocNumRoot", objectMeta.get("clc:titre"));
            Assert.assertNull(objectMeta.get("dos:titre"));

            objectMeta = (JSONObject) metadatas.get(1);
            Assert.assertEquals("Dossier documentaire 01", objectMeta.get("dos:titre"));
            Assert.assertNull(objectMeta.get("clc:titre"));

            objectMeta = (JSONObject) metadatas.get(2);
            Assert.assertEquals("Dossier documentaire vide 01", objectMeta.get("dos:titre"));
            Assert.assertNull(objectMeta.get("clc:titre"));
        }catch(Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    /**
     * Test de récupération d'une arborescence mixte contenant à la fois des Dossier et DocNumerique avec métadonnées
     */
    public void testGetMixDocumentTreeWithMetadata() {

        DocumentModel espace = session.getDocument(new PathRef("/EspaceCapi01"));
        Assert.assertNotNull(espace);

        Map<String, Object> map = Maps.newHashMap();
        Properties metadatas = new Properties();
        metadatas.put("dc:title", "");
        metadatas.put("clc:titre", "");
        metadatas.put("clc:type", "");
        metadatas.put("clc:auteurdoc", "");
        map.put("metadatas", metadatas);
        map.put("value",espace);

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob)NuxeoHelper.runOperation(session, null, GetDocumentTree.ID, map));

            Assert.assertEquals(Constants.ENTITY_DOCUMENTS, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals(3, object.get(Constants.COUNT));
            JSONArray entries = (JSONArray) object.get(Constants.ENTRIES);
            JSONObject objectMeta = (JSONObject) entries.get(0);

            Assert.assertEquals("DocNumRoot", objectMeta.get("dc:title"));
            Assert.assertEquals("DocNumRoot", objectMeta.get("clc:titre"));
            Assert.assertEquals("Avis", objectMeta.get("clc:type"));
            Assert.assertEquals("Pierre Richard", objectMeta.get("clc:auteurdoc"));
            Assert.assertNull(objectMeta.get("dos:titre"));

            objectMeta = (JSONObject) entries.get(1);
            Assert.assertEquals("Dossier documentaire 01", objectMeta.get("dc:title"));
            Assert.assertNull(objectMeta.get("clc:titre"));
            Assert.assertNull(objectMeta.get("clc:type"));
            Assert.assertNull(objectMeta.get("clc:auteurdoc"));
            Assert.assertNull(objectMeta.get("dos:titre"));

            objectMeta = (JSONObject) entries.get(2);
            Assert.assertEquals("Dossier documentaire vide 01", objectMeta.get("dc:title"));
            Assert.assertNull(objectMeta.get("clc:titre"));
            Assert.assertNull(objectMeta.get("clc:type"));
            Assert.assertNull(objectMeta.get("clc:auteurdoc"));
            Assert.assertNull(objectMeta.get("dos:titre"));
        }catch(Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    //test pour retourner l'arborescence des documents
    public void testGetDocumentTree() {

        DocumentModel dossier = session.getDocument(new PathRef("/EspaceBib01/Dossier02"));
        Assert.assertNotNull(dossier);

        Map<String, Object> map = Maps.newHashMap();
        map.put("value",dossier);
        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob)NuxeoHelper.runOperation(session, null, GetDocumentTree.ID, map));

            Assert.assertEquals(Constants.ENTITY_DOCUMENTS, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals(2, object.get(Constants.COUNT));
            JSONArray metadatas = (JSONArray) object.get(Constants.ENTRIES);
            JSONObject objectMeta = (JSONObject) metadatas.get(0);

            Assert.assertEquals("NoticeBib01", objectMeta.get("clc:titre"));
            Assert.assertEquals("BB",objectMeta.get("clc:type"));
            Assert.assertEquals("toto", objectMeta.get("clc:auteurdoc"));

            objectMeta = (JSONObject) metadatas.get(1);
            Assert.assertEquals("NoticeBib02", objectMeta.get("clc:titre"));
            Assert.assertEquals("BB2",objectMeta.get("clc:type"));
            Assert.assertEquals("toto2", objectMeta.get("clc:auteurdoc"));

        }catch(Exception e) {
            Assert.assertTrue(false);
        }
    }
//
    @Test
    //test pour retourner l'arborescence des documents

    public void testGetDocumentTreeWithMetadata() {
        DocumentModel dossier = session.getDocument(new PathRef("/EspaceCapi01/Dossier01"));
        Assert.assertNotNull(dossier);

        Map<String, Object> map = Maps.newHashMap();
        Properties metadatas = new Properties();
        metadatas.put("clc:titre", "");
        map.put("metadatas", metadatas);
        map.put("value", dossier);

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob)NuxeoHelper.runOperation(session, null, GetDocumentTree.ID, map));

            Assert.assertEquals(Constants.ENTITY_DOCUMENTS, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals(2, object.get(Constants.COUNT));
            JSONArray metaList = (JSONArray) object.get(Constants.ENTRIES);
            JSONObject objectMeta = (JSONObject) metaList.get(0);
            Assert.assertEquals("DocNum01", objectMeta.get("clc:titre"));

        }catch(Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    //test sans metadatas
    public void testGetDocumentTreeWithoutMetadata() {
        DocumentModel dossier = session.getDocument(new PathRef("/EspaceCapi01/Dossier01"));
        Assert.assertNotNull(dossier);

        Map<String, Object> map = Maps.newHashMap();
        map.put("value", dossier);

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob)NuxeoHelper.runOperation(session, null, GetDocumentTree.ID, map));

            Assert.assertEquals(Constants.ENTITY_DOCUMENTS, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals(2, object.get(Constants.COUNT));
            JSONArray metaList = (JSONArray) object.get(Constants.ENTRIES);
            JSONObject objectMeta = (JSONObject) metaList.get(0);
            Assert.assertEquals("DocNum01", objectMeta.get("clc:titre"));

        }catch(Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    //test si un document n'est pas un folder
    public void testDocumentIsNotFolder() {

        DocumentModel docNum = session.getDocument(new PathRef("/EspaceCapi01/Dossier01/DocNum01"));
        Assert.assertNotNull(docNum);

        Map<String, Object> map = Maps.newHashMap();
        map.put("value", docNum);

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob)NuxeoHelper.runOperation(session, null, GetDocumentTree.ID, map));
            Assert.assertEquals(Constants.ENTITY_EXCEPTION, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals("500", object.get(Constants.STATUS));
            Assert.assertEquals("Document parent non conforme. Création impossible du document : " + NuxeoHelper.displayDocInfos(docNum, session.getPrincipal().getName()), object.get(Constants.MESSAGE));
        }catch(Exception e) {
            Assert.assertTrue(false);
        }
    }


}
