package com.dga.clade.nuxeo.core;

import com.google.common.collect.Maps;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.test.AutomationFeature;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @param
 * @Author mhkimi
 * @Date 30/08/2018
 * @Version 1.0
 * @return
 **/

@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@Deploy({"org.nuxeo.ecm.automation.server", "org.nuxeo.runtime.jtajca", "org.nuxeo.ecm.core.schema", "org.nuxeo.ecm.core", "org.nuxeo.ecm.platform.dublincore"})
@LocalDeploy({"com.dga.clade.nuxeo.core"})
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
public class TestUpdateCladeListener{
    @Inject
    CoreSession session;

    @Before
    public void initRepo() {
        createEspaceCapi();
        createEspaceBib();
        createDossier();
        createDocsNumerique();
        createNoticeWithoutCLC();
        createNoticeWithoutCLCEnderEspaceBib();
    }
    private void createEspaceCapi() {
        //Création de l'espace Capi EspaceCapi01
        DocumentModel espace = session.createDocumentModel("/", "EspaceCapi01", "EspaceCapi");
        espace.setPropertyValue("dc:title", "Espace capitalisation 01");
        espace.setPropertyValue("dos:titre", "Espace capitalisation 01");
        espace.setPropertyValue("dos:auteur", "DGA");
        espace.setPropertyValue("dos:complement", "Il s'agit de l'espace de capitalisation 01");
        List<String> keywordsList = Lists.newArrayList();
        keywordsList.add("espace");
        keywordsList.add("capitalisation");
        keywordsList.add("numérique");
        espace.setPropertyValue("dos:motscles", (Serializable) keywordsList);
        session.createDocument(espace);

    }

    private void createEspaceBib() {
        //Création de l'espace Capi Bibliothécaire 01
        DocumentModel espace = session.createDocumentModel("/", "EspaceBib01", "EspaceBib");
        espace.setPropertyValue("dc:title", "Espace Bibliothécaire 01");
        espace.setPropertyValue("dos:titre", "Espace Bibliothécaire 01");
        espace.setPropertyValue("dos:auteur", "DGA");
        espace.setPropertyValue("dos:complement", "Il s'agit de l'espace Bibliothécaire 01");
        List<String> keywordsList = Lists.newArrayList();
        keywordsList.add("espace");
        keywordsList.add("Bibliothécaire");
        keywordsList.add("numérique");
        espace.setPropertyValue("dos:motscles", (Serializable) keywordsList);
        session.createDocument(espace);

    }

    private void createDossier(){
        //Création de du dossier Dossier01 dans EspaceCapi01
        List<String> keywordsList = Lists.newArrayList();
        keywordsList.add("espace");
        keywordsList.add("capitalisation");
        keywordsList.add("numérique");
        DocumentModel dossier = session.createDocumentModel("/EspaceCapi01", "Dossier01", "Dossier");
        dossier.setPropertyValue("dc:title", "Dossier documentaire 01");
        dossier.setPropertyValue("dos:titre", "Dossier documentaire 01");
        dossier.setPropertyValue("dos:auteur", "DGA");
        dossier.setPropertyValue("dos:creation", new Date());
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
    private void createDocsNumerique() {

        //Création d'un document numérique SANS CLC avec comme parent DOSSIER
        DocumentModel docNum0 = session.createDocumentModel("/EspaceCapi01/Dossier01", "DocNum00", "DocNumerique");
        session.createDocument(docNum0);

        //Création d'un document numérique AVEC CLC avec comme Parent DOSSIER
        DocumentModel docNum = session.createDocumentModel("/EspaceCapi01/Dossier01", "DocNum01", "DocNumerique");
        docNum.setPropertyValue("clc:titre", "DocNum01");
        session.createDocument(docNum);

        //Création d'un document numérique SANS CLC avec comme parent ESPACECAPI
        DocumentModel docNum1 = session.createDocumentModel("/EspaceCapi01", "DocNum02", "DocNumerique");
        session.createDocument(docNum1);

        //Création d'un document numérique AVEC CLC avec comme parent ESPACECAPI
        DocumentModel docNum2 = session.createDocumentModel("/EspaceCapi01", "DocNum03", "DocNumerique");
        docNum2.setPropertyValue("clc:titre", "DocNum03");
        session.createDocument(docNum2);

        //Création d'un document numérique SANS CLC avec comme parent Root
        DocumentModel docNum3 = session.createDocumentModel("/", "DocNum04", "DocNumerique");
        session.createDocument(docNum3);

        //Création d'un document numérique AVEC CLC avec comme parent Root
        DocumentModel docNum4 = session.createDocumentModel("/", "DocNum05", "DocNumerique");
        docNum4.setPropertyValue("clc:titre", "DocNum05");
        session.createDocument(docNum4);

        //Création d'un document numérique SANS CLC avec comme parent EspaceBib
        DocumentModel docNum5 = session.createDocumentModel("/EspaceBib01", "DocNum06", "DocNumerique");
        session.createDocument(docNum5);

        session.save();
    }

    //Création d'un notice SANS CLC sous la racine
    private void createNoticeWithoutCLC() {
        DocumentModel notice = session.createDocumentModel("/", "Notice01", "NoticeBib");
       // notice.setPropertyValue("clc:titre", "Notice01");

        File file = FileUtils.getResourceFileFromContext("data/notice01.txt");

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            // Chargement des données de la notice
            String line;
            ArrayList<Map<String, Object>> dataList = Lists.newArrayList();
            Map<String, Object> dataMap;

            while (!"EXPL".equals(line = bufferedReader.readLine())) {
                dataMap = Maps.newHashMap();
                String[] parts = line.split("=");
                dataMap.put("key", parts[0]);
                dataMap.put("value", parts[1]);
                dataList.add(dataMap);
            }
            notice.setPropertyValue("mxml:data", dataList);

            // Chargement des données d'exemplaires
            ArrayList<Map<String, Object>> expList = Lists.newArrayList();
            Map<String, Object> expMap1 = Maps.newHashMap();
            Map<String, Object> expMap2 = Maps.newHashMap();
            Map<String, Object> expMap3 = Maps.newHashMap();

            while ((line = bufferedReader.readLine()) != null) {
                String[] parts = line.split("=");
                String key = parts[0];
                parts = parts[1].split("@");
                expMap1.put(key, parts[0]);
                expMap2.put(key, parts[1]);
                expMap3.put(key, parts[2]);
            }
            expList.add(expMap1);
            expList.add(expMap2);
            expList.add(expMap3);
            notice.setPropertyValue("mxml:expl", expList);
            session.createDocument(notice);

            session.save();

        } catch (Exception e) {
            Assert.assertFalse(true);
        }
    }

    //Création d'un notice SANS CLC sous LEspaceBib 01
    private void createNoticeWithoutCLCEnderEspaceBib() {
        DocumentModel notice = session.createDocumentModel("/EspaceBib01", "Notice01", "NoticeBib");
        // notice.setPropertyValue("clc:titre", "Notice01");

        File file = FileUtils.getResourceFileFromContext("data/notice01.txt");

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            // Chargement des données de la notice
            String line;
            ArrayList<Map<String, Object>> dataList = Lists.newArrayList();
            Map<String, Object> dataMap;

            while (!"EXPL".equals(line = bufferedReader.readLine())) {
                dataMap = Maps.newHashMap();
                String[] parts = line.split("=");
                dataMap.put("key", parts[0]);
                dataMap.put("value", parts[1]);
                dataList.add(dataMap);
            }
            notice.setPropertyValue("mxml:data", dataList);

            // Chargement des données d'exemplaires
            ArrayList<Map<String, Object>> expList = Lists.newArrayList();
            Map<String, Object> expMap1 = Maps.newHashMap();
            Map<String, Object> expMap2 = Maps.newHashMap();
            Map<String, Object> expMap3 = Maps.newHashMap();

            while ((line = bufferedReader.readLine()) != null) {
                String[] parts = line.split("=");
                String key = parts[0];
                parts = parts[1].split("@");
                expMap1.put(key, parts[0]);
                expMap2.put(key, parts[1]);
                expMap3.put(key, parts[2]);
            }
            expList.add(expMap1);
            expList.add(expMap2);
            expList.add(expMap3);
            notice.setPropertyValue("mxml:expl", expList);
            session.createDocument(notice);

            session.save();

        } catch (Exception e) {
            Assert.assertFalse(true);
        }
    }

    @Test
    public void TestUpdateCladeListenerDossierParent() {
        DocumentModel docNum = session.getDocument(new PathRef("/EspaceCapi01/Dossier01/DocNum00"));

        try {
            Assert.assertEquals(docNum.getPropertyValue("clc:titre"), "Dossier documentaire 01");
            Assert.assertEquals(docNum.getPropertyValue("clc:complement"), "Ce dossier comporte les éléments de l'espace de capitalisation 01");
            Assert.assertEquals(docNum.getPropertyValue("clc:auteurdoc"), "DGA");
        } catch (AssertionError assertionError) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void TestUpdateCladeListenerWithCLC() {
        DocumentModel docNum = session.getDocument(new PathRef("/EspaceCapi01/Dossier01/DocNum01"));
        try {
            Assert.assertEquals(docNum.getPropertyValue("clc:titre"), "DocNum01");
            Assert.assertEquals(docNum.getPropertyValue("clc:complement"), null);
            Assert.assertEquals(docNum.getPropertyValue("clc:auteurdoc"), null);

        } catch (AssertionError assertionError) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void TestUpdateCladeListenerEspaceCapiParent(){
        DocumentModel docNum = session.getDocument(new PathRef("/EspaceCapi01/DocNum02"));

        try{
            Assert.assertEquals(docNum.getPropertyValue("clc:titre"), "Espace capitalisation 01");
            Assert.assertEquals(docNum.getPropertyValue("clc:complement"), "Il s'agit de l'espace de capitalisation 01");
            Assert.assertEquals(docNum.getPropertyValue("clc:auteurdoc"), "DGA");

        } catch (AssertionError assertionError) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void TestUpdateCladeListenerEspaceCapiParentWithCLC(){
        DocumentModel docNum = session.getDocument(new PathRef("/EspaceCapi01/DocNum03"));

        try{
            Assert.assertEquals(docNum.getPropertyValue("clc:titre"), "DocNum03");
            Assert.assertEquals(docNum.getPropertyValue("clc:complement"), null);
            Assert.assertEquals(docNum.getPropertyValue("clc:auteurdoc"), null);

        } catch (AssertionError assertionError) {
            Assert.assertTrue(false);
        }
    }
    @Test
    public void TestUpdateCladeListenerRootParent(){
        DocumentModel docNum = session.getDocument(new PathRef("/DocNum04"));

        try{
            Assert.assertEquals(docNum.getPropertyValue("clc:titre"), null);
            Assert.assertEquals(docNum.getPropertyValue("clc:complement"), null);
            Assert.assertEquals(docNum.getPropertyValue("clc:auteurdoc"), null);

        } catch (AssertionError assertionError) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void TestUpdateCladeListenerRootParentWithCLC(){
        DocumentModel docNum = session.getDocument(new PathRef("/DocNum05"));

        try{
            Assert.assertEquals(docNum.getPropertyValue("clc:titre"), "DocNum05");
            Assert.assertEquals(docNum.getPropertyValue("clc:complement"), null);
            Assert.assertEquals(docNum.getPropertyValue("clc:auteurdoc"), null);

        } catch (AssertionError assertionError) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void TestUpdateCladeListenerNotice() {
        DocumentModel noticebib = session.getDocument(new PathRef("/Notice01"));

        try{
            Assert.assertEquals(noticebib.getPropertyValue("clc:titre"), null);

        }catch (AssertionError assertionError) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void TestUpdateCladeListenerEspaceBibParentWithoutCLC(){
        DocumentModel docNum = session.getDocument(new PathRef("/EspaceBib01/DocNum06"));

        try{
            Assert.assertEquals(docNum.getPropertyValue("clc:titre"), null);
            Assert.assertEquals(docNum.getPropertyValue("clc:complement"), null);
            Assert.assertEquals(docNum.getPropertyValue("clc:auteurdoc"), null);

        } catch (AssertionError assertionError) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void TestUpdateCladeListenerNoticeInEspaceBib() {
        DocumentModel noticebib = session.getDocument(new PathRef("/EspaceBib01/Notice01"));

        try{
            Assert.assertEquals(noticebib.getPropertyValue("clc:titre"), null);

        }catch (AssertionError assertionError) {
            Assert.assertTrue(false);
        }
    }


}

