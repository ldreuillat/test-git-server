package com.dga.clade.nuxeo.core;

import com.google.common.collect.Maps;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.test.CoreFeature;
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
import java.util.*;

/**
 * Classe de test unitaire des types et schemas des espaces de capitalisation Clade
 * @author ldreuillat
 * @date 31/07/2018
 * @version 1.0
 */
@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.CLASS)
@Deploy({"org.nuxeo.runtime.jtajca", "org.nuxeo.ecm.core.schema", "org.nuxeo.ecm.core", "org.nuxeo.ecm.platform.dublincore"})
@LocalDeploy("com.dga.clade.nuxeo.core")
public class TestEspaceCapi {

    @Inject
    CoreSession session;

    /**
     * Initialisation des données de test
     */
    @Before
    public void initRepo() {
        createEspaceCapi();
        createDocNumerique();
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
        espace.setPropertyValue("dos:motscles", (Serializable)keywordsList);
        espace.setPropertyValue("esc:portail", "Portail DGA armement");
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
        dossier.setPropertyValue("dos:motscles", (Serializable)keywordsList);
        session.createDocument(dossier);

        DocumentModel dossier2 = session.createDocumentModel(espace.getPathAsString(), "Dossier02", "Dossier");
        dossier2.setPropertyValue("dc:title", "Dossier documentaire 02");
        dossier2.setPropertyValue("dos:nuid", new Long(987654321));
        dossier2.setPropertyValue("dos:titre", "Dossier documentaire 02");
        dossier2.setPropertyValue("dos:auteur", "DGA");
        dossier2.setPropertyValue("dos:creation", new Date());
        dossier2.setPropertyValue("dos:modif", new Date());
        dossier2.setPropertyValue("dos:complement", "Ce dossier comporte les autres éléments de l'espace de capitalisation 01");
        keywordsList = Lists.newArrayList();
        keywordsList.add("documents");
        keywordsList.add("numérique");
        keywordsList.add("clade");
        keywordsList.add("nuxeo");
        dossier2.setPropertyValue("dos:motscles", (Serializable)keywordsList);
        session.createDocument(dossier2);
        session.save();
    }

    /**
     * Méthode de création d'un document numérique
     */
    private void createDocNumerique() {
        DocumentModel docNum = session.createDocumentModel("/EspaceCapi01/Dossier01", "DocNum01", "DocNumerique");
        docNum.setPropertyValue("clc:titre", "DocNum01");
        docNum.setPropertyValue("clc:type", "CCTP");
        docNum.setPropertyValue("clc:source", "DGA");
        docNum.setPropertyValue("clc:auteurdoc", "Robert Martin");

        session.createDocument(docNum);
        session.save();
    }

    /**
     * Tests de requêtage sur les métadonnées de l'espace de capitalisation
     */
    @Test
    public void testQueryEspaceCapi() {
        DocumentModelList docs = session.query("SELECT * From Document WHERE dos:motscles = 'nuxeo'");
        Assert.assertFalse(docs.isEmpty());
        Assert.assertEquals(2, docs.size());

        docs = session.query("SELECT * From Document WHERE dos:motscles = 'nuxeo' AND dos:motscles = 'ausy'");
        Assert.assertFalse(docs.isEmpty());
        Assert.assertEquals(1, docs.size());

        docs = session.query("SELECT * From Document WHERE dos:motscles IN ('nuxeo', 'ausy', 'numérique')");
        Assert.assertFalse(docs.isEmpty());
        Assert.assertEquals(3, docs.size());
    }

    /**
     * Test du format (facet et schéma) du type DocNumerique et de l'application du listener
     */
    @Test
    public void testGetDocNum() {
        DocumentModel docNum = session.getDocument(new PathRef("/EspaceCapi01/Dossier01/DocNum01"));
        Assert.assertNotNull(docNum);

        String[] schemas = docNum.getSchemas();
        Assert.assertTrue(Arrays.asList(schemas).contains("cladecommon"));
        Assert.assertTrue(Arrays.asList(schemas).contains("cladenumeric"));

        // Test des métadonnées communes
        Assert.assertEquals("DocNum01", docNum.getPropertyValue("dc:title"));
        String[] contributors = (String[])docNum.getPropertyValue("clc:contributeurs");
        Assert.assertEquals(1, contributors.length);
        Assert.assertEquals("Administrator", contributors[0]);
        Assert.assertNotNull(docNum.getPropertyValue("clc:depot"));
        Assert.assertNotNull(docNum.getPropertyValue("clc:modif"));

        // Test des métadonnées numériques
        Assert.assertEquals(docNum.getId(), docNum.getPropertyValue("cln:nxuid"));
        Assert.assertEquals(docNum.getPathAsString(), docNum.getPropertyValue("cln:nxchemin"));
    }
}
