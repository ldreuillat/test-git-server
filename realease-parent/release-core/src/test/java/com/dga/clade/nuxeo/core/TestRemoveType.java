package com.dga.clade.nuxeo.core;

import com.dga.clade.nuxeo.constant.Constants;
import com.dga.clade.nuxeo.core.operation.RemoveType;
import com.dga.clade.nuxeo.helper.NuxeoHelper;
import com.google.common.collect.Maps;
import net.sf.json.JSONObject;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import java.util.List;
import java.util.Map;

@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@Deploy({"org.nuxeo.runtime.jtajca", "org.nuxeo.ecm.core.schema", "org.nuxeo.ecm.core", "org.nuxeo.ecm.automation.server", "org.nuxeo.ecm.platform.dublincore"})
@LocalDeploy("com.dga.clade.nuxeo.core")
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)

public class TestRemoveType {

    private static final org.apache.commons.logging.Log log = LogFactory.getLog(TestRemoveType.class);
    @Inject
    CoreSession session;

    @Before
    public void createEspaceConfiguration() {
        DocumentModel configuration = session.createDocumentModel("/", "Configuration", "Configuration");
        configuration.setPropertyValue("dc:title", "Configuration");
        configuration = session.createDocument(configuration);

        DocumentModel configType1 = session.createDocumentModel(configuration.getPathAsString(), "ConfigurationType1", "ConfigType");
        configType1.setPropertyValue("dc:title", "my first Configuration type");
        configType1.setPropertyValue("cft:type", "MyFirstType");

        List<String> mappingList = org.assertj.core.util.Lists.newArrayList();
        mappingList.add("clg:metagen1|String");
        mappingList.add("clg:metagen2|Date");
        mappingList.add("clg:metagen3|Numeric");
        configType1.setPropertyValue("cft:mapping", (Serializable) mappingList);
        configType1 = session.createDocument(configType1);

        DocumentModel configType2 = session.createDocumentModel(configuration.getPathAsString(), "ConfigurationType2", "ConfigType");
        configType2.setPropertyValue("dc:title", "my seconde Configuration type");
        configType2.setPropertyValue("cft:type", "MySecondeType");

        List<String> mappingList2 = org.assertj.core.util.Lists.newArrayList();
        mappingList2.add("clg:metagen5|String");
        mappingList2.add("clg:metagen7|Date");
        mappingList2.add("clg:metagen19|Numeric");
        configType1.setPropertyValue("cft:mapping", (Serializable) mappingList2);
        configType2 = session.createDocument(configType2);

        session.save();
    }

    @Test
    //test pour supprimer un type dans la configuration
    public void testRemoveExistingType() {

        DocumentModel configuration = session.getDocument(new PathRef("/Configuration"));
        configuration = session.createDocument(configuration);
        Assert.assertNotNull(configuration);

        Map<String, Object> map = Maps.newHashMap();
        map.put("type", "MyFirstType");
        map.put("repo", "test");

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob) NuxeoHelper.runOperation(session, null, RemoveType.ID, map));
            Assert.assertEquals("le type : MyFirstType a été supprimé avec succes", object.get("MESSAGE"));

        } catch (Exception e) {
            Assert.assertFalse(true);
        }
    }

    @Test
    //test pour supprimer un type qui n'existe pas dans la configuration
    public void testRemoveNotExistingType() {

        DocumentModel configuration = session.getDocument(new PathRef("/Configuration"));
        configuration = session.createDocument(configuration);
        Assert.assertNotNull(configuration);

        Map<String, Object> map = Maps.newHashMap();
        map.put("type", "typeInexistant");
        map.put("repo", "test");

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob) NuxeoHelper.runOperation(session, null, RemoveType.ID, map));
            Assert.assertEquals(com.dga.clade.nuxeo.constant.Constants.ENTITY_EXCEPTION, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals("500", object.get(com.dga.clade.nuxeo.constant.Constants.STATUS));
            Assert.assertEquals("Impossible de supprimer ce type, le type n'existe pas dans la configuration", object.get(Constants.MESSAGE));

        } catch (Exception e) {
            Assert.assertFalse(true);
        }
    }
}