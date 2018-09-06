package com.dga.clade.nuxeo.core;

import com.dga.clade.nuxeo.constant.Constants;
import com.dga.clade.nuxeo.core.operation.AddNewType;
import com.dga.clade.nuxeo.helper.NuxeoHelper;
import com.google.common.collect.Maps;
import net.sf.json.JSONObject;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@Deploy({"org.nuxeo.runtime.jtajca", "org.nuxeo.ecm.core.schema", "org.nuxeo.ecm.core", "org.nuxeo.ecm.automation.server", "org.nuxeo.ecm.platform.dublincore"})
@LocalDeploy("com.dga.clade.nuxeo.core")
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)

public class TestAddNewType {

    @Inject
    CoreSession session;

    @Test
    //test pour ajouter un type avec un nom non valide
    public void testAddNewTypeWithIncorectName() {

        DocumentModel configuration = session.createDocumentModel("/", "Configuration", "Configuration");
        configuration.setPropertyValue("dc:title", "Configuration");
        configuration = session.createDocument(configuration);

        DocumentModel configType1 = session.createDocumentModel(configuration.getPathAsString(), "ConfigurationType1", "ConfigType");
        configType1.setPropertyValue("dc:title", "my first Configuration type");
        configType1.setPropertyValue("cft:type", "MyFirstType");

        List<String> mappingList = Lists.newArrayList();
        mappingList.add("clg:metagen1|String");
        mappingList.add("clg:metagen2|Date");
        mappingList.add("clg:metagen3|Numeric");
        configType1.setPropertyValue("cft:mapping", (Serializable) mappingList);
        session.createDocument(configType1);

        List<String> mappingList1 = Lists.newArrayList();
        mappingList1.add("cln:metagen17|String");
        mappingList1.add("clg:metagen15|Date");
        mappingList1.add("fausseMetadonnée|Numeric");

        Map<String, Object> map = Maps.newHashMap();
        map.put("mapping", mappingList1);
        map.put("type", "myNewType3");
        map.put("repo", "test");
        session.save();

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob) NuxeoHelper.runOperation(session, null, AddNewType.ID, map));
            Assert.assertEquals(com.dga.clade.nuxeo.constant.Constants.ENTITY_EXCEPTION, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals("500", object.get(com.dga.clade.nuxeo.constant.Constants.STATUS));
            Assert.assertEquals("Impossible d'ajouter ce type, le mapping contient des noms des métadonnées non valides", object.get(Constants.MESSAGE));

        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    //test pour ajouter un type avec un type du mapping non valide
    public void testAddNewTypeWithIncorectType() {

        DocumentModel configuration = session.createDocumentModel("/", "Configuration", "Configuration");
        configuration.setPropertyValue("dc:title", "Configuration");
        configuration = session.createDocument(configuration);

        DocumentModel configType1 = session.createDocumentModel(configuration.getPathAsString(), "ConfigurationType1", "ConfigType");
        configType1.setPropertyValue("dc:title", "my first Configuration type");
        configType1.setPropertyValue("cft:type", "MyFirstType");

        List<String> mappingList = Lists.newArrayList();
        mappingList.add("clg:metagen1|String");
        mappingList.add("clg:metagen2|Date");
        mappingList.add("clg:metagen3|Numeric");
        configType1.setPropertyValue("cft:mapping", (Serializable) mappingList);
        session.createDocument(configType1);

        List<String> mappingList1 = Lists.newArrayList();
        mappingList1.add("clg:metagen5|fauxType");
        mappingList1.add("clg:metagen6|Date");
        mappingList1.add("clg:metagen7|Numeric");

        Map<String, Object> map = Maps.newHashMap();
        map.put("mapping", mappingList1);
        map.put("type", "myNewType2");
        map.put("repo", "test");
        session.save();

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob) NuxeoHelper.runOperation(session, null, AddNewType.ID, map));
            Assert.assertEquals(com.dga.clade.nuxeo.constant.Constants.ENTITY_EXCEPTION, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals("500", object.get(com.dga.clade.nuxeo.constant.Constants.STATUS));
            Assert.assertEquals("Impossible d'ajouter ce type, le mapping contient des types de données non valides", object.get(Constants.MESSAGE));

        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    //test pour ajouter un type existant déja dans la configuration
    public void testAddNewTypeWithExistType() {
        DocumentModel configuration = session.createDocumentModel("/", "Configuration", "Configuration");
        configuration.setPropertyValue("dc:title", "Configuration");
        configuration = session.createDocument(configuration);

        DocumentModel configType1 = session.createDocumentModel(configuration.getPathAsString(), "ConfigurationType1", "ConfigType");
        configType1.setPropertyValue("dc:title", "my first Configuration type");
        configType1.setPropertyValue("cft:type", "MyFirstType");

        List<String> mappingList = Lists.newArrayList();
        mappingList.add("clg:metagen1|String");
        mappingList.add("clg:metagen2|Date");
        mappingList.add("clg:metagen3|Numeric");
        configType1.setPropertyValue("cft:mapping", (Serializable) mappingList);
        session.createDocument(configType1);

        List<String> mappingList1 = Lists.newArrayList();
        mappingList1.add("clg:metagen9|String");
        mappingList1.add("clg:metagen11|Date");
        mappingList1.add("clg:metagen13|Numeric");

        Map<String, Object> map = Maps.newHashMap();
        map.put("mapping", mappingList1);
        map.put("type", "MyFirstType");
        map.put("repo", "test");
        session.save();

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob) NuxeoHelper.runOperation(session, null, AddNewType.ID, map));
            Assert.assertEquals(com.dga.clade.nuxeo.constant.Constants.ENTITY_EXCEPTION, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals("500", object.get(com.dga.clade.nuxeo.constant.Constants.STATUS));
            Assert.assertEquals("Impossible d'ajouter ce type, le type existe déja dans la configuration", object.get(Constants.MESSAGE));

        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    //test pour ajouter un type dans une configuration non existante
    public void testAddNewTypeWithNoExistConfigurationObject() {

        List<String> mappingList = Lists.newArrayList();
        mappingList.add("clg:metagen1|String");
        mappingList.add("clg:metagen2|Date");
        mappingList.add("clg:metagen3|Numeric");

        Map<String, Object> map = Maps.newHashMap();
        map.put("mapping", mappingList);
        map.put("type", "myNewType1");
        map.put("repo", "test");
        session.save();

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob) NuxeoHelper.runOperation(session, null, AddNewType.ID, map));
            Assert.assertEquals("le type : myNewType1 a été jouté avec succes", object.get("MESSAGE"));

        } catch (Exception e) {
            Assert.assertFalse(true);
        }
    }

    @Test
    ////test pour ajouter un type dans une configuration
    public void testAddNewTypeWithExistConfigurationObject() {

        DocumentModel configuration = session.createDocumentModel("/", "Configuration", "Configuration");
        configuration.setPropertyValue("dc:title", "Configuration");
        configuration = session.createDocument(configuration);

        DocumentModel configType1 = session.createDocumentModel(configuration.getPathAsString(), "ConfigurationType1", "ConfigType");
        configType1.setPropertyValue("dc:title", "my first Configuration type");
        configType1.setPropertyValue("cft:type", "MyFirstType");

        List<String> mappingList = Lists.newArrayList();
        mappingList.add("clg:metagen1|String");
        mappingList.add("clg:metagen2|Date");
        mappingList.add("clg:metagen3|Numeric");
        configType1.setPropertyValue("cft:mapping", (Serializable) mappingList);
        session.createDocument(configType1);

        List<String> mappingList1 = Lists.newArrayList();
        mappingList1.add("clg:metagen8|String");
        mappingList1.add("clg:metagen10|Date");
        mappingList1.add("clg:metagen14|Numeric");

        Map<String, Object> map = Maps.newHashMap();
        map.put("mapping", mappingList1);
        map.put("type", "myNewType7");
        map.put("repo", "test");

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob) NuxeoHelper.runOperation(session, null, AddNewType.ID, map));
            Assert.assertEquals("le type : myNewType7 a été jouté avec succes", object.get("MESSAGE"));


        } catch (Exception e) {
            Assert.assertFalse(true);
        }
    }
}