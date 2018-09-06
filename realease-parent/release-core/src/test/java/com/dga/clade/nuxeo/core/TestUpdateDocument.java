package com.dga.clade.nuxeo.core;

import com.dga.clade.nuxeo.constant.Constants;
import com.dga.clade.nuxeo.core.operation.UpdateDocument;
import com.dga.clade.nuxeo.core.operation.helper.DocUtils;
import com.dga.clade.nuxeo.helper.NuxeoHelper;
import com.google.common.collect.Maps;
import net.sf.json.JSONObject;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.io.download.DownloadService;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.core.versioning.VersioningService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@Deploy("org.nuxeo.ecm.automation.server")
@LocalDeploy("com.dga.clade.nuxeo.core")
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
public class TestUpdateDocument {

    @Inject
    CoreSession session;

    @Inject
    protected VersioningService versioningService;

    private DocumentModel document1;

    @Before
    public void initRepo() {
        createEspaceCapi();
        //createDocNumerique();
    }

    private void createEspaceCapi() {
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
        espace.setPropertyValue("dos:motscles", (Serializable) keywordsList);
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
        espace1.setPropertyValue("dos:motscles", (Serializable) keywordsList2);
        espace1 = session.createDocument(espace1);

        DocumentModel dossier = session.createDocumentModel(espace.getPathAsString(), "Dossier01", "Dossier");
        dossier.setPropertyValue("dc:title", "Dossier documentaire 01");
        dossier.setPropertyValue("dos:nuid", new Long(123456789));
        dossier.setPropertyValue("dos:titre", "Dossier documentaire 01");
        dossier.setPropertyValue("dos:auteur", "DGA");
        dossier.setPropertyValue("dos:creation", new Date());
        dossier.setPropertyValue("dos:modif", new Date());
        dossier.setPropertyValue("dos:complement", "Ce dossier comporte les éléments de l'espace de capitalisation 01");
        keywordsList = Lists.newArrayList();
        keywordsList.add("dossier");
        keywordsList.add("dga");
        keywordsList.add("clade");
        keywordsList.add("nuxeo");
        keywordsList.add("ausy");
        dossier.setPropertyValue("dos:motscles", (Serializable) keywordsList);
        session.createDocument(dossier);

        DocumentModel dossier2 = session.createDocumentModel(espace1.getPathAsString(), "Dossier02", "Dossier");
        dossier2 = session.createDocument(dossier2);
        session.save();
    }

    @Test
    //test pour retourner l'exception lorsque les metadonnées et les blobs sont nuls
    public void testUpdateDocumentWithNotMetadatas() {

        DocumentModel docNum = session.createDocumentModel("/EspaceCapi01/Dossier01", "DocNum01", "DocNumerique");
        docNum.setPropertyValue("clc:titre", "DocNum01");
        docNum.setPropertyValue("clc:type", "CCTP");
        docNum.setPropertyValue("clc:auteurdoc", "Robert Martin");

        docNum = session.createDocument(docNum);
        session.save();
        Assert.assertNotNull(docNum);

        Map<String, Object> map = Maps.newHashMap();
        map.put("value", docNum);

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob) NuxeoHelper.runOperation(session, null, UpdateDocument.ID, map));
            Assert.assertEquals(com.dga.clade.nuxeo.constant.Constants.ENTITY_EXCEPTION, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals("500", object.get(com.dga.clade.nuxeo.constant.Constants.STATUS));
            Assert.assertEquals("Il faut insérer des métadonnées pour la mise à jour : " + NuxeoHelper.displayDocInfos(docNum, session.getPrincipal().getName()) + " : " + "null", object.get(Constants.MESSAGE));
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    //test pour mettre à jour les métadonnées d'un document
    public void testUpdateDocumentMetadatas() throws OperationException {

        DocumentModel dossier = session.getDocument(new PathRef("/EspaceBib01/Dossier02"));
        dossier = session.createDocument(dossier);
        Assert.assertNotNull(dossier);

        DocumentModel notBib = session.createDocumentModel(dossier.getPathAsString(), "Notice01", "NoticeBib");
        notBib.setPropertyValue("clc:titre", "NoticeBib01");
        notBib.setPropertyValue("clc:type", "BB");
        notBib.setPropertyValue("clc:auteurdoc", "toto");
        notBib = session.createDocument(notBib);
        session.save();
        Assert.assertNotNull(notBib);

        Map<String, Object> map = Maps.newHashMap();
        Properties prop = new Properties();

        prop.put("clc:titre", "notice mise à jour");
        prop.put("clc:type", "le type met à jour");
        prop.put("clc:auteurdoc", "auteur met à jour");
        map.put("metadatas", prop);
        map.put("value", notBib);

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob) NuxeoHelper.runOperation(session, null, UpdateDocument.ID, map));

            Assert.assertEquals(Constants.ENTITY_DOCUMENT, object.get(Constants.ENTITY_TYPE));
            JSONObject metadatas = (JSONObject) object.get(Constants.ENTRY);

            Assert.assertEquals("notice mise à jour", metadatas.get("clc:titre"));
            Assert.assertEquals("le type met à jour", metadatas.get("clc:type"));
            Assert.assertEquals("auteur met à jour", metadatas.get("clc:auteurdoc"));

        } catch (Exception e) {
            Assert.assertTrue(false);
        }

    }

    @Test
    //test pour mettre à jour le blob du fichier principal d'un document
    public void testUpdateDocumentBlobPrincipal() throws OperationException, IOException {

        DocumentModel docNum = session.createDocumentModel("/EspaceCapi01/Dossier01", "DocNum01", "DocNumerique");
        docNum.setPropertyValue("clc:titre", "DocNum01");
        docNum.setPropertyValue("clc:type", "CCTP");
        docNum.setPropertyValue("clc:auteurdoc", "Robert Martin");
        docNum = session.createDocument(docNum);
        Assert.assertNotNull(docNum);

        Map<String, Object> mapObj = Maps.newHashMap();
        Map<String, Object> map = Maps.newHashMap();
        Map<String, Object> mapObj2 = Maps.newHashMap();
        List<Map<String, Object>> map1 = new ArrayList<Map<String, Object>>();

        File oldFile = FileUtils.getResourceFileFromContext("data/File01.pdf");
        File FileAssoc = FileUtils.getResourceFileFromContext("data/FileAssoc_1.pdf");
        Blob blobAssoc = new FileBlob(FileAssoc);
        Blob blob0 = new FileBlob(oldFile);

        mapObj.put("nom", blob0.getFilename());
        mapObj.put("contenu", blob0);
        mapObj.put("status", "stat");

        mapObj2.put("nom", blobAssoc.getFilename());
        mapObj2.put("contenu", blobAssoc);
        mapObj2.put("status", "stat");
        map1.add(mapObj2);

        docNum.setPropertyValue("clc:fichierprinc", (Serializable) mapObj);
        docNum.setPropertyValue("clc:fichiersassoc", (Serializable) map1);
        session.save();

        File fileUpdate = FileUtils.getResourceFileFromContext("data/FileUpdate.pdf");
        Blob blob1 = new FileBlob(fileUpdate);
        map.put("value", docNum);

        DownloadService downloadService = Framework.getService(DownloadService.class);
       String url1 = downloadService.getDownloadUrl(docNum, docNum.getPathAsString(), blob1.getFilename());

        BlobList blobs = new BlobList();
        blobs.add(blob1);

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob) NuxeoHelper.runOperation(session, blobs, UpdateDocument.ID, map));
            Assert.assertEquals(Constants.ENTITY_DOCUMENT, object.get(Constants.ENTITY_TYPE));
            JSONObject metadatas = (JSONObject) object.get(Constants.ENTRY);
            Assert.assertEquals("DocNum01", metadatas.get("clc:titre"));
            Assert.assertEquals("CCTP", metadatas.get("clc:type"));

            Map mp = (Map) metadatas.get("clc:fichierprinc");
            Object obj = mp.get("nom");
            Object obj1 = mp.get("contenu");
            Assert.assertEquals("FileUpdate.pdf", obj);
            Assert.assertEquals(url1, obj1);

            List<Map> lst = (List<Map>) metadatas.get("clc:fichiersassoc");
            Map mp0 = lst.get(0);
            Object obj0 = mp0.get("nom");
            Assert.assertEquals("FileAssoc_1.pdf", obj0);

        } catch (Exception e) {
            Assert.assertTrue(false);
        }

    }

    @Test
    //test pour mettre à jour les blobs le fichier Principal et les fichiers associes d'un document
    public void testUpdateDocumentBlobAssocies() throws OperationException, IOException {

        DocumentModel docNum = session.createDocumentModel("/EspaceCapi01/Dossier01", "DocNum01", "DocNumerique");
        docNum.setPropertyValue("clc:titre", "DocNum01");
        docNum.setPropertyValue("clc:type", "CCTP");
        docNum.setPropertyValue("clc:auteurdoc", "Robert Martin");
        docNum = session.createDocument(docNum);
        Assert.assertNotNull(docNum);

        Map<String, Object> mapObj = Maps.newHashMap();
        Map<String, Object> mapObj2 = Maps.newHashMap();
        Map<String, Object> map = Maps.newHashMap();
        List<Map<String, Object>> map1 = new ArrayList<Map<String, Object>>();

        File oldFile = FileUtils.getResourceFileFromContext("data/File01.pdf");
        Blob blob0 = new FileBlob(oldFile);
        File FileAssoc = FileUtils.getResourceFileFromContext("data/FileAssoc_1.pdf");
        Blob blobAssoc = new FileBlob(FileAssoc);
        File fileUpdate = FileUtils.getResourceFileFromContext("data/FileUpdate.pdf");
        Blob blob1 = new FileBlob(fileUpdate);
        File fileAssoc2 = FileUtils.getResourceFileFromContext("data/FileAssoc_2.pdf");
        Blob blobAssoc2 = new FileBlob(fileAssoc2);

        mapObj.put("nom", blob0.getFilename());
        mapObj.put("contenu", blob0);
        mapObj.put("status", "stat");

        mapObj2.put("nom", "fichier associer");
        mapObj2.put("contenu", blobAssoc);
        mapObj2.put("status", "stat");
        map1.add(mapObj2);
        docNum.setPropertyValue("clc:fichierprinc", (Serializable) mapObj);
        docNum.setPropertyValue("clc:fichiersassoc", (Serializable) map1);
        session.save();

        map.put("value", docNum);

        BlobList blobs = new BlobList();
        blobs.add(blob1);
        blobs.add(blobAssoc2);


        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob) NuxeoHelper.runOperation(session, blobs, UpdateDocument.ID, map));
            Assert.assertEquals(Constants.ENTITY_DOCUMENT, object.get(Constants.ENTITY_TYPE));
            JSONObject metadatas = (JSONObject) object.get(Constants.ENTRY);
            Assert.assertEquals("DocNum01", metadatas.get("clc:titre"));
            Assert.assertEquals("CCTP", metadatas.get("clc:type"));

            Map mp = (Map) metadatas.get("clc:fichierprinc");
            Object obj = mp.get("nom");
            Assert.assertEquals("FileUpdate.pdf", obj);

            List<Map> lst = (List<Map>) metadatas.get("clc:fichiersassoc");
            Map mp0 = lst.get(0);
            Object obj0 = mp0.get("nom");
            Assert.assertEquals("FileAssoc_2.pdf", obj0);

        } catch (Exception e) {
            Assert.assertTrue(false);
        }

    }

    @Test
    //test avec une list des blobs vide
    public void testUpdateDocumentListBlobEmpty() throws OperationException, IOException {

        DocumentModel docNum = session.createDocumentModel("/EspaceCapi01/Dossier01", "DocNum01", "DocNumerique");
        docNum.setPropertyValue("clc:titre", "DocNum01");
        docNum.setPropertyValue("clc:type", "CCTP");
        docNum.setPropertyValue("clc:auteurdoc", "Robert Martin");
        docNum = session.createDocument(docNum);
        Assert.assertNotNull(docNum);

        Map<String, Object> map = Maps.newHashMap();
        session.save();

        map.put("value", docNum);
        BlobList blobs = new BlobList();

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob) NuxeoHelper.runOperation(session, blobs, UpdateDocument.ID, map));
            Assert.assertEquals(Constants.ENTITY_EXCEPTION, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals("500", object.get(Constants.STATUS));
            Assert.assertEquals("Erreur de mise à jour, La liste des blobs est vide : " + NuxeoHelper.displayDocInfos(docNum, session.getPrincipal().getName()) + " : " + blobs, object.get(Constants.MESSAGE));

        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    /**
     * Test l'update de version d'un document avec un type de version invalide
     */
    @Test
    public void testUpdateDocumentBadVersion()  {
        DocumentModel docNum = session.createDocumentModel("/EspaceCapi01/Dossier01", "DocBadVersM01", "DocNumerique");
        docNum.setPropertyValue("clc:titre", "DocBadVersM01");
        docNum = session.createDocument(docNum);
        session.save();
        Assert.assertNotNull(docNum);
        assertVersion("0.0", docNum);
        assertLatestVersion(null, docNum);
        assertVersionLabel("0.0", docNum);

        Properties prop = new Properties();
        prop.put("clc:titre", "file update");

        Map<String, Object> map = Maps.newHashMap();
        map.put("value", docNum);
        map.put("version", "MEDIUM");
        map.put("metadatas", prop);

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob) NuxeoHelper.runOperation(session, null, UpdateDocument.ID, map));
            Assert.assertEquals(Constants.ENTITY_EXCEPTION, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals("500", object.get(Constants.STATUS));
            Assert.assertEquals("Le type de version : MEDIUM n'est pas valide", object.get(Constants.MESSAGE));
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    /**
     * Test l'update de version majeure d'un document
     */
    @Test
    public void testUpdateDocumentMajorVersion()  {

        DocumentModel docNum = session.createDocumentModel("/EspaceCapi01/Dossier01", "DocNumVersM01", "DocNumerique");
        docNum.setPropertyValue("clc:titre", "DocNumVersM01");
        docNum = session.createDocument(docNum);
        session.save();
        Assert.assertNotNull(docNum);
        assertVersion("0.0", docNum);
        assertLatestVersion(null, docNum);
        assertVersionLabel("0.0", docNum);

        Properties prop = new Properties();
        prop.put("clc:titre", "file update");

        Map<String, Object> map = Maps.newHashMap();
        map.put("value", docNum);
        map.put("version", "MAJOR");
        map.put("metadatas", prop);

        try {
            // Première montée de version majeure
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob) NuxeoHelper.runOperation(session, null, UpdateDocument.ID, map));
            Assert.assertEquals(Constants.ENTITY_DOCUMENT, object.get(Constants.ENTITY_TYPE));
            JSONObject metadatas = (JSONObject) object.get(Constants.ENTRY);
            Assert.assertEquals("file update", metadatas.get("clc:titre"));
            Assert.assertEquals("1.0", metadatas.get("version"));

            DocumentModelList docs = session.query("SELECT * FROM Document WHERE clc:titre = 'file update' AND ecm:mixinType != 'HiddenInNavigation' AND ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted'");
            Assert.assertEquals(1, docs.size());
            DocumentModel doc = docs.get(0);
            doc.refresh();
            assertVersion("1.0", doc);
            assertLatestVersion("1.0", docNum);

            // Deuxième montée de version majeure
            prop.put("clc:titre", "file update 2");
            map.put("metadatas", prop);
            object = NuxeoHelper.getJSONObjectFromBlob((Blob) NuxeoHelper.runOperation(session, null, UpdateDocument.ID, map));
            Assert.assertEquals(Constants.ENTITY_DOCUMENT, object.get(Constants.ENTITY_TYPE));
            metadatas = (JSONObject) object.get(Constants.ENTRY);
            Assert.assertEquals("file update 2", metadatas.get("clc:titre"));
            Assert.assertEquals("2.0", metadatas.get("version"));

            docs = session.query("SELECT * FROM Document WHERE clc:titre = 'file update 2' AND ecm:mixinType != 'HiddenInNavigation' AND ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted'");
            Assert.assertEquals(1, docs.size());
            doc = docs.get(0);
            assertVersion("2.0", doc);
            assertLatestVersion("2.0", docNum);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    /**
     * Test l'update de version mineure d'un document
     */
    @Test
    public void testUpdateDocumentMinorVersion()  {

        DocumentModel docNum = session.createDocumentModel("/EspaceCapi01/Dossier01", "DocNumVers01", "DocNumerique");
        docNum.setPropertyValue("clc:titre", "DocNumVers01");
        docNum = session.createDocument(docNum);
        Assert.assertNotNull(docNum);
        assertVersion("0.0", docNum);
        assertLatestVersion(null, docNum);
        assertVersionLabel("0.0", docNum);

        Properties prop = new Properties();
        prop.put("clc:titre", "file update");

        Map<String, Object> map = Maps.newHashMap();
        map.put("value", docNum);
        map.put("version", "MINOR");
        map.put("metadatas", prop);

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob) NuxeoHelper.runOperation(session, null, UpdateDocument.ID, map));
            Assert.assertEquals(Constants.ENTITY_DOCUMENT, object.get(Constants.ENTITY_TYPE));
            JSONObject metadatas = (JSONObject) object.get(Constants.ENTRY);
            Assert.assertEquals("file update", metadatas.get("clc:titre"));
            Assert.assertEquals("0.1", metadatas.get("version"));

            DocumentModelList docs = session.query("SELECT * FROM Document WHERE clc:titre = 'file update' AND ecm:mixinType != 'HiddenInNavigation' AND ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted'");
            Assert.assertEquals(1, docs.size());
            DocumentModel doc = docs.get(0);
            assertVersion("0.1", doc);
            assertLatestVersion("0.1", docNum);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    protected void assertVersion(String expected, DocumentModel doc) {
        Assert.assertEquals(expected, DocUtils.getMajor(doc) + "." + DocUtils.getMinor(doc));
    }

    protected void assertLatestVersion(String expected, DocumentModel doc) {
        DocumentModel ver = doc.getCoreSession().getLastDocumentVersion(doc.getRef());
        if (ver == null) {
            Assert.assertNull(expected);
        } else {
            assertVersion(expected, ver);
        }
    }

    protected void assertVersionLabel(String expected, DocumentModel doc) {
        Assert.assertEquals(expected, versioningService.getVersionLabel(doc));
    }
}