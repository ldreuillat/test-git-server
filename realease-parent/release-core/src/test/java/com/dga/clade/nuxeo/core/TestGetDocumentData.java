package com.dga.clade.nuxeo.core;

import com.dga.clade.nuxeo.constant.Constants;
import com.dga.clade.nuxeo.core.operation.GetDocumentData;
import com.dga.clade.nuxeo.helper.NuxeoHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.sf.json.JSONObject;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.automation.core.util.StringList;
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
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)

public class TestGetDocumentData {

    private static final org.apache.commons.logging.Log log = LogFactory.getLog(TestGetDocumentData.class);
    @Inject
    CoreSession session;

    @Before
    public void initRepo() {
        createEspaceCapiAndEspaceBib();
        //createDocNumerique();
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
        dossier = session.createDocument(dossier);

        DocumentModel dossier2 = session.createDocumentModel(espace1.getPathAsString(), "Dossier02", "Dossier");
        dossier2 = session.createDocument(dossier2);
        session.save();
    }

    @Test
    public void testGetDocumentWithIncompatibleMetadata() {

        DocumentModel File01 = session.createDocumentModel("/", "File01", "File");
        File01.setPropertyValue("dc:title", "File01");
        File01.setPropertyValue("dc:description", "Fichier de test 01");
        File01 = session.createDocument(File01);
        session.save();
        Assert.assertNotNull(File01);

        Map<String, Object> map = Maps.newHashMap();
        Properties props = new Properties();
        props.put("dc:title", "");
        props.put("clc:titre", "");
        props.put("clc:type", "");
        props.put("clc:auteurdoc", "");

        map.put("metadatas", props);
        map.put("value", File01);

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob)NuxeoHelper.runOperation(session, null, GetDocumentData.ID, map));

            Assert.assertEquals(Constants.ENTITY_DOCUMENT, object.get(Constants.ENTITY_TYPE));
            JSONObject metadatas = (JSONObject) object.get(Constants.ENTRY);
            Assert.assertEquals("File01", metadatas.get("dc:title").toString());
            Assert.assertNull(metadatas.get("clc:titre"));
            Assert.assertNull(metadatas.get("clc:type"));
            Assert.assertNull(metadatas.get("clc:auteurdoc"));
        } catch (Exception e) {
            Assert.assertFalse(true);
        }
    }

    @Test
    public void testGetDocumentWithDefaultMetadataFile() {

        DocumentModel File01 = session.createDocumentModel("/", "File02", "File");
        File01.setPropertyValue("dc:title", "File02");
        File01.setPropertyValue("dc:description", "Fichier de test 02");
        File01 = session.createDocument(File01);
        session.save();
        Assert.assertNotNull(File01);

        Map<String, Object> map = Maps.newHashMap();
        map.put("value", File01);

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob)NuxeoHelper.runOperation(session, null, GetDocumentData.ID, map));

            Assert.assertEquals(Constants.ENTITY_DOCUMENT, object.get(Constants.ENTITY_TYPE));
            JSONObject metadatas = (JSONObject) object.get(Constants.ENTRY);
            Assert.assertEquals("File02", metadatas.get("dc:title").toString());
            Assert.assertEquals("Fichier de test 02", metadatas.get("dc:description").toString());
            Assert.assertEquals("Administrator", metadatas.get("dc:creator").toString());
        } catch (Exception e) {
            Assert.assertFalse(true);
        }
    }

    @Test
    public void testGetDocumentWithDefaultMetadataDossier() {

        DocumentModel dossier2 = session.getDocument(new PathRef("/EspaceBib01/Dossier02"));
        dossier2.setPropertyValue("dc:title", "Dossier documentaire 02");
        dossier2.setPropertyValue("dos:nuid", new Long(987654321));
        dossier2.setPropertyValue("dos:titre", "Dossier documentaire 02");
        dossier2.setPropertyValue("dos:auteur", "DGA");
        dossier2.setPropertyValue("dos:creation", new Date());
        dossier2.setPropertyValue("dos:modif", new Date());
        dossier2.setPropertyValue("dos:complement", "Ce dossier comporte les autres éléments de l'espace de capitalisation 01");
        List<String> keywordsList1 = Lists.newArrayList();
        keywordsList1.add("documents");
        keywordsList1.add("numérique");
        keywordsList1.add("clade");
        keywordsList1.add("nuxeo");
        dossier2.setPropertyValue("dos:motscles", (Serializable) keywordsList1);
        dossier2 = session.createDocument(dossier2);
        Assert.assertNotNull(dossier2);

        Map<String, Object> map = Maps.newHashMap();
        map.put("value", dossier2);

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob)NuxeoHelper.runOperation(session, null, GetDocumentData.ID, map));

            Assert.assertEquals(Constants.ENTITY_DOCUMENT, object.get(Constants.ENTITY_TYPE));
            JSONObject metadatas = (JSONObject) object.get(Constants.ENTRY);

            StringList str = new StringList();
            str.add("documents");
            str.add("numérique");
            str.add("clade");
            str.add("nuxeo");

            Assert.assertEquals("987654321", metadatas.get("dos:nuid").toString());
            Assert.assertEquals("Dossier documentaire 02", metadatas.get("dos:titre"));
            Assert.assertEquals("Dossier documentaire 02", metadatas.get("dc:title"));
            Assert.assertEquals(str, metadatas.get("dos:motscles"));
        } catch (Exception e) {
            Assert.assertFalse(true);
        }
    }

    @Test
    public void testGetDocumentWithDefaultMetadataDocNumerique() {

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
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob)NuxeoHelper.runOperation(session, null, GetDocumentData.ID, map));

            Assert.assertEquals(Constants.ENTITY_DOCUMENT, object.get(Constants.ENTITY_TYPE));
            JSONObject metadatas = (JSONObject) object.get(Constants.ENTRY);

            Assert.assertEquals("DocNum01", metadatas.get("clc:titre"));
            Assert.assertEquals("CCTP", metadatas.get("clc:type"));
            Assert.assertEquals("Robert Martin", metadatas.get("clc:auteurdoc"));
        } catch (Exception e) {
            Assert.assertFalse(true);
        }
    }

    @Test
    public void testGetNoticeBibWithSpecificMetadata() {
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

        Properties props = new Properties();
        props.put("clc:titre", "");
        props.put("clc:type", "");
        props.put("clc:auteurdoc", "");
        props.put("clc:etat", "");

        Map<String, Object> map = Maps.newHashMap();
        map.put("metadatas", props);
        map.put("value", notBib);

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob) NuxeoHelper.runOperation(session, null, GetDocumentData.ID, map));

            Assert.assertEquals(Constants.ENTITY_DOCUMENT, object.get(Constants.ENTITY_TYPE));
            JSONObject metadatas = (JSONObject) object.get(Constants.ENTRY);

            Assert.assertEquals("NoticeBib01", metadatas.get("clc:titre"));
            Assert.assertEquals("BB", metadatas.get("clc:type"));
            Assert.assertNotNull(metadatas.get("clc:auteurdoc"));

        } catch (Exception e) {
            Assert.assertFalse(true);
        }
    }

    @Test
    public void testGetDocumentWithMetadataDocNumerique() {

        DocumentModel dossier = session.getDocument(new PathRef("/EspaceBib01/Dossier02"));
        Assert.assertNotNull(dossier);

        DocumentModel docNum = session.createDocumentModel(dossier.getPathAsString(), "DocNum01", "DocNumerique");
        docNum.setPropertyValue("clc:titre", "DocNum01");
        docNum.setPropertyValue("clc:type", "CCTP");
        docNum.setPropertyValue("clc:auteurdoc", "Robert Martin");
        docNum.setPropertyValue("cln:timbre", "12345");
        docNum.setPropertyValue("cln:description", "Ceci est un document numérique de test");
        docNum.setPropertyValue("cln:original_copie", "COPIE_01");
        docNum = session.createDocument(docNum);
        session.save();
        Assert.assertNotNull(docNum);

        Map<String, Object> map = Maps.newHashMap();
        Properties props = new Properties();
        props.put("clc:titre", "");
        props.put("clc:type", "");
        props.put("cln:nxuid", "");
        props.put("cln:description", "");
        props.put("cln:original_copie", "");

        map.put("metadatas", props);
        map.put("value", docNum);

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob)NuxeoHelper.runOperation(session, null, GetDocumentData.ID, map));

            Assert.assertEquals(Constants.ENTITY_DOCUMENT, object.get(Constants.ENTITY_TYPE));
            JSONObject metadatas = (JSONObject) object.get(Constants.ENTRY);
            Assert.assertEquals("DocNum01", metadatas.get("clc:titre"));
            Assert.assertEquals("CCTP", metadatas.get("clc:type"));
            Assert.assertEquals(docNum.getId(), metadatas.get("cln:nxuid"));
            Assert.assertEquals("Ceci est un document numérique de test", metadatas.get("cln:description"));
            Assert.assertEquals("COPIE_01", metadatas.get("cln:original_copie"));
        } catch (Exception e) {
            Assert.assertFalse(true);
        }
    }

    @Test
    public void testGetDocumentWithMetadataDossier() {

        DocumentModel dossier = session.createDocumentModel("/EspaceCapi01", "Dossier03", "Dossier");
        dossier.setPropertyValue("dc:title", "Dossier documentaire 03");
        dossier.setPropertyValue("dos:nuid", new Long(987654321));
        dossier.setPropertyValue("dos:titre", "Dossier documentaire 03");
        dossier.setPropertyValue("dos:auteur", "DGA");
        dossier.setPropertyValue("dos:creation", new Date());
        dossier.setPropertyValue("dos:modif", new Date());
        dossier.setPropertyValue("dos:complement", "Ce dossier comporte des éléments de l'espace de capitalisation 01");
        List<String> mentionsList = Lists.newArrayList();
        mentionsList.add("CONFIDENTIEL DEFENSE");
        mentionsList.add("SECRET DEFENSE");
        mentionsList.add("TOP SECRET");
        dossier.setPropertyValue("dos:mentions", (Serializable) mentionsList);
        dossier = session.createDocument(dossier);
        session.save();
        Assert.assertNotNull(dossier);

        Map<String, Object> map = Maps.newHashMap();
        Properties props = new Properties();
        props.put("dos:titre", "");
        props.put("dos:auteur", "");
        props.put("dos:mentions", "");

        map.put("metadatas", props);
        map.put("value", dossier);

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob)NuxeoHelper.runOperation(session, null, GetDocumentData.ID, map));

            Assert.assertEquals(Constants.ENTITY_DOCUMENT, object.get(Constants.ENTITY_TYPE));
            JSONObject metadatas = (JSONObject) object.get(Constants.ENTRY);

            StringList str = new StringList();
            str.add("CONFIDENTIEL DEFENSE");
            str.add("SECRET DEFENSE");
            str.add("TOP SECRET");

            Assert.assertEquals("Dossier documentaire 03", metadatas.get("dos:titre").toString());
            Assert.assertEquals("DGA", metadatas.get("dos:auteur"));
            Assert.assertEquals(str, metadatas.get("dos:mentions"));
        } catch (Exception e) {
            Assert.assertFalse(true);
        }
    }
}