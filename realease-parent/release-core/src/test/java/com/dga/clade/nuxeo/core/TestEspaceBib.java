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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * Classe de test unitaire des types et schemas des espaces bibliothécaires Clade
 * @author ldreuillat
 * @date 31/07/2018
 * @version 1.0
 */
@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.CLASS)
@Deploy({"org.nuxeo.runtime.jtajca", "org.nuxeo.ecm.core.schema", "org.nuxeo.ecm.core", "org.nuxeo.ecm.platform.dublincore"})
@LocalDeploy("com.dga.clade.nuxeo.core")
public class TestEspaceBib {

    @Inject
    CoreSession session;

    /**
     * Initialisation des données de test
     */
    @Before
    public void initRepo() {
        createNotice();

    }

    /**
     * Méthode de création d'un notice avec ses données standards et d'exemplaire
     */
    private void createNotice() {
        DocumentModel notice = session.createDocumentModel("/", "Notice01", "NoticeBib");
        notice.setPropertyValue("clc:titre", "Notice01");

        File file = FileUtils.getResourceFileFromContext("data/notice01.txt");
        BufferedReader bufferedReader = null;

        try {
            // Chargement des données de la notice
            bufferedReader = new BufferedReader(new FileReader(file));
            StringBuffer stringBuffer = new StringBuffer();
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

            notice = session.createDocument(notice);
            session.save();
            bufferedReader.close();
        } catch(Exception e) {
            Assert.assertFalse(true);
        }
    }

    /**
     * Test du format (facet et schéma) du type NoticeBib et de l'application du listener
     */
    @Test
    public void testGetNotice() {
        DocumentModel notice = session.getDocument(new PathRef("/Notice01"));
        Assert.assertNotNull(notice);

        String[] schemas = notice.getSchemas();
        Assert.assertTrue(Arrays.asList(schemas).contains("marcxml"));
        Assert.assertTrue(Arrays.asList(schemas).contains("cladecommon"));
        Assert.assertTrue(Arrays.asList(schemas).contains("cladepapier"));

        // Test des métadonnées MarcXML
        ArrayList<Map<String, Object>> dataList = (ArrayList<Map<String, Object>>) notice.getPropertyValue("mxml:data");
        Assert.assertEquals(19, dataList.size());

        ArrayList<Map<String, Object>> explList = (ArrayList<Map<String, Object>>) notice.getPropertyValue("mxml:expl");
        Assert.assertEquals(3, explList.size());

        Map<String, Object> explMap1 = explList.get(0);
        Assert.assertEquals(30, explMap1.size());

        // Test des métadonnées communes
        Assert.assertEquals("Notice01", notice.getPropertyValue("dc:title"));
        String[] contributors = (String[])notice.getPropertyValue("clc:contributeurs");
        Assert.assertEquals(1, contributors.length);
        Assert.assertEquals("Administrator", contributors[0]);
        Assert.assertNotNull(notice.getPropertyValue("clc:depot"));
        Assert.assertNotNull(notice.getPropertyValue("clc:modif"));
    }

    /**
     * Test du requêtage sur des documents du type NoticeBib
     */
    @Test
    public void testQueryNotice() {
        // Requête sur les champs standards de la notice
        DocumentModelList docs = session.query("SELECT * FROM NoticeBib WHERE (mxml:data/*1/key='801$b' AND mxml:data/*1/value='Médiathèque Pukapuka')");

        Assert.assertFalse(docs.isEmpty());
        Assert.assertEquals("Notice01", docs.get(0).getPropertyValue("dc:title"));

        docs = session.query("SELECT * FROM NoticeBib WHERE (mxml:data/*1/key='801$b' AND mxml:data/*1/value='Jean Louis')");
        Assert.assertTrue(docs.isEmpty());

        // Requête sur les champs d'exemplaires
        docs = session.query("SELECT * FROM NoticeBib WHERE (mxml:expl/*1/vc='PUKA' AND mxml:expl/*1/vf='00519000162114')");
        Assert.assertFalse(docs.isEmpty());

        docs = session.query("SELECT * FROM NoticeBib WHERE (mxml:expl/*1/v3='41' AND mxml:expl/*1/vj='Presse Pukapuka')");
        Assert.assertTrue(docs.isEmpty());

        docs = session.query("SELECT * FROM NoticeBib WHERE (mxml:data/*1/key='700$b' AND mxml:data/*1/value='Jean Louis') AND (mxml:expl/*1/vc='PUKA' AND mxml:expl/*1/vf='00519000162114')");
        Assert.assertFalse(docs.isEmpty());
    }

    @Test
    public void testSelectNotice() {
        DocumentModelList docs = session.query("SELECT * FROM NoticeBib WHERE dc:title LIKE 'N%'");

    }
}
