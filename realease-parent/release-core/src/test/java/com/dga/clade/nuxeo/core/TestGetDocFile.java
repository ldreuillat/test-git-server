package com.dga.clade.nuxeo.core;

import com.dga.clade.nuxeo.constant.Constants;
import com.dga.clade.nuxeo.core.operation.GetDocFile;
import com.dga.clade.nuxeo.helper.NuxeoHelper;
import com.google.common.collect.Maps;
import net.sf.json.JSONObject;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import javax.inject.Inject;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @param
 * @Author amerroudj
 * @Date 31/07/2018
 * @Version 1.0
 * @return
 **/

@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@Deploy("org.nuxeo.ecm.automation.server")
@LocalDeploy("com.dga.clade.nuxeo.core")
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)

public class TestGetDocFile {

    @Inject
    CoreSession session;
    @Before
    public void createDocAndChilds() throws OperationException {
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
    }

    @Test
    //test pour récupèrer un fichier principal d'un document
    public void testGetdocFilePrincipal() {

        DocumentModel docNum = session.createDocumentModel("/EspaceCapi01/Dossier01", "DocNum01", "DocNumerique");
        docNum.setPropertyValue("clc:titre", "DocNum01");
        docNum.setPropertyValue("clc:type", "CCTP");
        docNum.setPropertyValue("clc:auteurdoc", "Robert Martin");
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
        docNum = session.createDocument(docNum);
        session.save();

        map.put("value", docNum);
        map.put("contenu",blob0.getFilename() );

        try {
            Blob blob = ((Blob)NuxeoHelper.runOperation(session, null, GetDocFile.ID, map));
            Assert.assertEquals(blob0.getFilename(), blob.getFilename());
            Assert.assertEquals(blob0.getLength(), blob.getLength());


        } catch (Exception e) {
            Assert.assertTrue(false);
        }

    }

    @Test
    //test pour récupèrer un fichier associer d'un document
    public void testGetdocFileAssoc() {

        DocumentModel docNum = session.createDocumentModel("/EspaceCapi01/Dossier01", "DocNum01", "DocNumerique");
        docNum.setPropertyValue("clc:titre", "DocNum01");
        docNum.setPropertyValue("clc:type", "CCTP");
        docNum.setPropertyValue("clc:auteurdoc", "Robert Martin");

        Assert.assertNotNull(docNum);

        Map<String, Object> mapObj = Maps.newHashMap();
        Map<String, Object> map = Maps.newHashMap();
        Map<String, Object> mapObj2 = Maps.newHashMap();
        Map<String, Object> mapObj3 = Maps.newHashMap();
        List<Map<String, Object>> map1 = new ArrayList<Map<String, Object>>();

        File oldFile = FileUtils.getResourceFileFromContext("data/File01.pdf");
        File FileAssoc = FileUtils.getResourceFileFromContext("data/FileAssoc_1.pdf");
        File FileAssoc2 = FileUtils.getResourceFileFromContext("data/FileAssoc_2.pdf");
        Blob blobAssoc = new FileBlob(FileAssoc);
        Blob blobAssoc2 = new FileBlob(FileAssoc2);
        Blob blob0 = new FileBlob(oldFile);

        mapObj.put("nom", blob0.getFilename());
        mapObj.put("contenu", blob0);
        mapObj.put("status", "stat");

        mapObj2.put("nom", blobAssoc.getFilename());
        mapObj2.put("contenu", blobAssoc);
        mapObj2.put("status", "stat");
        map1.add(mapObj2);

        mapObj3.put("nom", blobAssoc2.getFilename());
        mapObj3.put("contenu", blobAssoc2);
        mapObj3.put("status", "stat");
        map1.add(mapObj3);

        docNum.setPropertyValue("clc:fichierprinc", (Serializable) mapObj);
        docNum.setPropertyValue("clc:fichiersassoc", (Serializable) map1);
        docNum = session.createDocument(docNum);
        session.save();

        map.put("value", docNum);
        map.put("contenu",blobAssoc2.getFilename() );

        try {
            Blob blob = (Blob) NuxeoHelper.runOperation(session, null, GetDocFile.ID, map);
            Assert.assertEquals(blobAssoc2.getFilename(), blob.getFilename());
            Assert.assertEquals(blobAssoc2.getLength(), blob.getLength());

        } catch (Exception e) {
            Assert.assertTrue(false);
        }

    }

    @Test
    //test pour récupèrer  un fichier dans un document de type dossier
    public void testGetdocFileIfDocIsFolder() {
        DocumentModel dossier = session.getDocument(new PathRef("/EspaceCapi01/Dossier01"));
        dossier = session.createDocument(dossier);
        Assert.assertNotNull(dossier);

        DocumentModel docNum = session.createDocumentModel("/EspaceCapi01/Dossier01", "DocNum01", "DocNumerique");
        docNum.setPropertyValue("clc:titre", "DocNum01");
        docNum.setPropertyValue("clc:type", "CCTP");
        docNum.setPropertyValue("clc:auteurdoc", "Robert Martin");
        Assert.assertNotNull(docNum);

        Map<String, Object> mapObj = Maps.newHashMap();
        Map<String, Object> map = Maps.newHashMap();

        File oldFile = FileUtils.getResourceFileFromContext("data/File01.pdf");
        Blob blob0 = new FileBlob(oldFile);

        mapObj.put("nom", blob0.getFilename());
        mapObj.put("contenu", blob0);
        mapObj.put("status", "stat");

        docNum.setPropertyValue("clc:fichierprinc", (Serializable) mapObj);
        docNum = session.createDocument(docNum);
        session.save();

        map.put("value", dossier);
        map.put("contenu",blob0.getFilename() );

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob) NuxeoHelper.runOperation(session, null, GetDocFile.ID, map));
            Assert.assertEquals(Constants.ENTITY_EXCEPTION, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals("500", object.get(Constants.STATUS));
            Assert.assertEquals("la la récuperation du fichier est impossible, le document n'est pas conforme  : " + NuxeoHelper.displayDocInfos(dossier, session.getPrincipal().getName()), object.get(Constants.MESSAGE));


        } catch (Exception e) {
            Assert.assertTrue(false);
        }

    }
    @Test
    //test pour récupèrer  un fichier qui n'existe pas dans le document
    public void testGetdocFileNotExist() {

        DocumentModel docNum = session.createDocumentModel("/EspaceCapi01/Dossier01", "DocNum01", "DocNumerique");
        docNum.setPropertyValue("clc:titre", "DocNum01");
        docNum.setPropertyValue("clc:type", "CCTP");
        docNum.setPropertyValue("clc:auteurdoc", "Robert Martin");
        Assert.assertNotNull(docNum);

        Map<String, Object> mapObj = Maps.newHashMap();
        Map<String, Object> map = Maps.newHashMap();

        File oldFile = FileUtils.getResourceFileFromContext("data/File01.pdf");
        File FileAssoc = FileUtils.getResourceFileFromContext("data/FileAssoc_1.pdf");
        Blob blob0 = new FileBlob(oldFile);
        Blob blobAssoc = new FileBlob(FileAssoc);

        mapObj.put("nom", blob0.getFilename());
        mapObj.put("contenu", blob0);
        mapObj.put("status", "stat");

        docNum.setPropertyValue("clc:fichierprinc", (Serializable) mapObj);
        docNum = session.createDocument(docNum);
        session.save();

        map.put("value", docNum);
        map.put("contenu",blobAssoc.getFilename() );

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob) NuxeoHelper.runOperation(session, null, GetDocFile.ID, map));
            Assert.assertEquals(Constants.ENTITY_EXCEPTION, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals("500", object.get(Constants.STATUS));
            Assert.assertEquals("la la récuperation du fichier est impossible, le document ne contient pas le fichier specifier  : " + NuxeoHelper.displayDocInfos(docNum, session.getPrincipal().getName()), object.get(Constants.MESSAGE));


        } catch (Exception e) {
            Assert.assertTrue(false);
        }

    }
}