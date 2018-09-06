package com.dga.clade.nuxeo.core;

import com.dga.clade.nuxeo.constant.Constants;
import com.dga.clade.nuxeo.core.operation.CopyDocument;
import com.dga.clade.nuxeo.core.operation.MoveDocument;
import com.dga.clade.nuxeo.helper.NuxeoHelper;
import com.google.common.collect.Maps;
import net.sf.json.JSONObject;
import org.apache.commons.logging.LogFactory;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.*;
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
@LocalDeploy("com.dga.clade.nuxeo.core")
public class TestCopyDocument {

    private static final org.apache.commons.logging.Log log = LogFactory.getLog(MoveDocument.class);
    @Inject
    CoreSession session;

    @Before
    public void initRepo() {
        // Initialisation de la structure des espaces et dossiers servants aux tests
        DocumentModel espace = session.createDocumentModel("/", "EspaceCapi01", "EspaceCapi");
        espace.setPropertyValue("dc:title", "Espace capitalisation 01");
        espace.setPropertyValue("dos:nuid", new Long(000000001));
        espace.setPropertyValue("dos:titre", "Espace capitalisation 01");
        espace.setPropertyValue("dos:auteur", "DGA");
        espace.setPropertyValue("dos:creation", new Date());
        espace.setPropertyValue("dos:modif", new Date());
        espace.setPropertyValue("dos:complement", "Il s'agit de l'espace de capitalisation 01");
        espace.setPropertyValue("esc:portail", "Portail DGA global");
        espace = session.createDocument(espace);

        DocumentModel dossierSrc01 = session.createDocumentModel(espace.getPathAsString(), "DossierSource01", "Dossier");
        dossierSrc01.setPropertyValue("dc:title", "Dossier source 01");
        dossierSrc01.setPropertyValue("dos:nuid", new Long(123456789));
        dossierSrc01.setPropertyValue("dos:titre", "Dossier source 01");
        dossierSrc01.setPropertyValue("dos:auteur", "DGA");
        dossierSrc01.setPropertyValue("dos:creation", new Date());
        dossierSrc01.setPropertyValue("dos:modif", new Date());
        dossierSrc01.setPropertyValue("dos:complement", "Ce dossier est un dossier source de copies de documents");
        dossierSrc01 = session.createDocument(dossierSrc01);

        DocumentModel dossierSrc02 = session.createDocumentModel(dossierSrc01.getPathAsString(), "DossierSource02", "Dossier");
        dossierSrc02.setPropertyValue("dc:title", "Dossier source 02");
        dossierSrc02.setPropertyValue("dos:nuid", new Long(123456789));
        dossierSrc02.setPropertyValue("dos:titre", "Dossier source 02");
        dossierSrc02.setPropertyValue("dos:auteur", "DGA");
        dossierSrc02.setPropertyValue("dos:creation", new Date());
        dossierSrc02.setPropertyValue("dos:modif", new Date());
        dossierSrc02.setPropertyValue("dos:complement", "Ce dossier est un sous dossier source de copies de documents");
        dossierSrc02 = session.createDocument(dossierSrc02);

        DocumentModel dossier2 = session.createDocumentModel(espace.getPathAsString(), "DossierDestination01", "Dossier");
        dossier2.setPropertyValue("dc:title", "Dossier destination 01");
        dossier2.setPropertyValue("dos:nuid", new Long(987654321));
        dossier2.setPropertyValue("dos:titre", "Dossier destination 01");
        dossier2.setPropertyValue("dos:auteur", "DGA");
        dossier2.setPropertyValue("dos:creation", new Date());
        dossier2.setPropertyValue("dos:modif", new Date());
        dossier2.setPropertyValue("dos:complement", "Ce dossier sert de destination aux copies de documents");
        session.createDocument(dossier2);

        // Initialisation des documents de test
        DocumentModel docNum01 = session.createDocumentModel(dossierSrc01.getPathAsString(), "DocNum01", "DocNumerique");
        docNum01.setPropertyValue("clc:titre", "DocNum01");
        docNum01.setPropertyValue("clc:type", "CCTP");
        docNum01.setPropertyValue("clc:source", "DGA");
        docNum01.setPropertyValue("clc:auteurdoc", "Robert Martin");
        session.createDocument(docNum01);

        DocumentModel docNum02 = session.createDocumentModel(dossierSrc02.getPathAsString(), "DocNum02", "DocNumerique");
        docNum02.setPropertyValue("clc:titre", "DocNum02");
        docNum02.setPropertyValue("clc:type", "Notice");
        docNum02.setPropertyValue("clc:source", "AUSY");
        docNum02.setPropertyValue("clc:auteurdoc", "Pierre Legrand");
        session.createDocument(docNum02);

        DocumentModel docNum03 = session.createDocumentModel(dossierSrc02.getPathAsString(), "DocNum03", "DocNumerique");
        docNum03.setPropertyValue("clc:titre", "DocNum03");
        docNum03.setPropertyValue("clc:type", "Avis");
        docNum03.setPropertyValue("clc:source", "AUSY");
        docNum03.setPropertyValue("clc:auteurdoc", "Eric Dupuis");
        session.createDocument(docNum03);

        session.save();

    }

    @Test
    public void testNonFolderishDestination() {
        DocumentModel fileToCopy = session.getDocument(new PathRef("/EspaceCapi01/DossierSource01/DocNum01"));
        Assert.assertNotNull(fileToCopy);

        DocumentModel destination = session.getDocument(new PathRef("/EspaceCapi01/DossierSource01/DossierSource02/DocNum02"));
        Assert.assertNotNull(destination);

        try {
            Map<String, Object> map = Maps.newHashMap();
            map.put("destination", destination);
            map.put("value", fileToCopy);
            Properties newMetadatas = new Properties();
            newMetadatas.put("clc:titre", "");
            map.put("metadatas", newMetadatas);

            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob)NuxeoHelper.runOperation(session, null, CopyDocument.ID, map));
            Assert.assertEquals(Constants.ENTITY_EXCEPTION, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals("500", object.get(Constants.STATUS));
            Assert.assertEquals("La destination doit être de type conteneur : DocNum02 [DocNumerique] User : Administrator", object.get(Constants.MESSAGE));
        }catch(Exception e) {
            Assert.assertFalse(true);
        }
    }

    @Test
    public void testSameDestination() {
        DocumentModel fileToCopy = session.getDocument(new PathRef("/EspaceCapi01/DossierSource01/DocNum01"));
        Assert.assertNotNull(fileToCopy);

        DocumentModel destination = session.getDocument(new PathRef("/EspaceCapi01/DossierSource01"));
        Assert.assertNotNull(destination);

        try {
            Map<String, Object> map = Maps.newHashMap();
            map.put("destination", destination);
            map.put("value", fileToCopy);

            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob)NuxeoHelper.runOperation(session, null, CopyDocument.ID, map));
            Assert.assertEquals(Constants.ENTITY_EXCEPTION, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals("500", object.get(Constants.STATUS));
            Assert.assertEquals("Le dossier destination ne doit pas etre le parent de la source : DossierSource01 [Dossier] User : Administrator", object.get(Constants.MESSAGE));
        }catch(Exception e) {
            Assert.assertFalse(true);
        }
    }

    @Test
    public void testCopySimpleDoc() {

        DocumentModel fileToCopy = session.getDocument(new PathRef("/EspaceCapi01/DossierSource01/DocNum01"));
        Assert.assertNotNull(fileToCopy);

        DocumentModel destination = session.getDocument(new PathRef("/EspaceCapi01/DossierDestination01"));
        Assert.assertNotNull(destination);

        Properties props = new Properties();
        props.put("clc:titre", "");

        Map<String, Object> map = Maps.newHashMap();
        map.put("metadatas", props);
        map.put("destination", destination);
        map.put("value", fileToCopy);

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob)NuxeoHelper.runOperation(session, null, CopyDocument.ID, map));

            Assert.assertEquals(Constants.ENTITY_DOCUMENT, object.get(Constants.ENTITY_TYPE));
            JSONObject metadatas = (JSONObject)object.get(Constants.ENTRY);
            Assert.assertEquals("DocNum01", metadatas.get("clc:titre"));
            Assert.assertEquals("/EspaceCapi01/DossierDestination01/DocNum01", metadatas.get("path"));
            Assert.assertEquals(destination.getId(), metadatas.get("parentRef"));
        }catch(Exception e) {
            Assert.assertFalse(true);
        }
    }
    @Test
    public void testCopyDossier() {

        DocumentModel fileToCopy = session.getDocument(new PathRef("/EspaceCapi01/DossierSource01"));
        Assert.assertNotNull(fileToCopy);

        DocumentModel destination = session.getDocument(new PathRef("/EspaceCapi01/DossierDestination01"));
        Assert.assertNotNull(destination);

        Properties props = new Properties();
        props.put("dos:titre", "");

        Map<String, Object> map = Maps.newHashMap();
        map.put("metadatas", props);
        map.put("destination", destination);
        map.put("value", fileToCopy);

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob)NuxeoHelper.runOperation(session, null, CopyDocument.ID, map));

            Assert.assertEquals(Constants.ENTITY_DOCUMENT, object.get(Constants.ENTITY_TYPE));
            JSONObject metadatas = (JSONObject)object.get(Constants.ENTRY);
            Assert.assertEquals("Dossier source 01", metadatas.get("dos:titre"));
            Assert.assertEquals("/EspaceCapi01/DossierDestination01/DossierSource01", metadatas.get("path"));
            Assert.assertEquals(destination.getId(), metadatas.get("parentRef"));

            // Vérification de la copie complète du dossier dans la destination
            DocumentModelList docs = session.query("SELECT * FROM Document WHERE ecm:path STARTSWITH '/EspaceCapi01/DossierDestination01/DossierSource01' ORDER BY ecm:path");
            Assert.assertEquals(4, docs.size());
            Assert.assertEquals("/EspaceCapi01/DossierDestination01/DossierSource01/DocNum01", docs.get(0).getPathAsString());
            Assert.assertEquals("/EspaceCapi01/DossierDestination01/DossierSource01/DossierSource02", docs.get(1).getPathAsString());
            Assert.assertEquals("/EspaceCapi01/DossierDestination01/DossierSource01/DossierSource02/DocNum02", docs.get(2).getPathAsString());
            Assert.assertEquals("/EspaceCapi01/DossierDestination01/DossierSource01/DossierSource02/DocNum03", docs.get(3).getPathAsString());
        }catch(Exception e) {
            Assert.assertFalse(true);
        }
    }
}
