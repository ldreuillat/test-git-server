package com.dga.clade.nuxeo.core;

import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
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
import java.io.Serializable;
import java.util.List;

/**
 * Classe de test unitaire des types et schemas des données de configuration
 * @author ldreuillat
 * @date 31/07/2018
 * @version 1.0
 */
@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.CLASS)
@Deploy({"org.nuxeo.runtime.jtajca", "org.nuxeo.ecm.core.schema", "org.nuxeo.ecm.core", "org.nuxeo.ecm.platform.dublincore"})
@LocalDeploy("com.dga.clade.nuxeo.core")
public class TestConfiguration {

    @Inject
    CoreSession session;

    /**
     * Initialisation des données de test
     */
    @Before
    public void initRepo() {
        DocumentModel configuration = session.createDocumentModel("/", "Configuration", "Configuration");
        configuration.setPropertyValue("dc:title", "Configuration");
        configuration = session.createDocument(configuration);

        DocumentModel configType1 = session.createDocumentModel(configuration.getPathAsString(), "ConfigurationBateau", "ConfigType");
        configType1.setPropertyValue("dc:title", "Configuration bateau");
        configType1.setPropertyValue("cft:type", "Bateau");

        List<String>  mappingList = Lists.newArrayList();
        mappingList.add("clg:metagen1|String");
        mappingList.add("clg:metagen2|Date");
        mappingList.add("clg:metagen3|Numeric");
        configType1.setPropertyValue("cft:mapping", (Serializable)mappingList);
        session.createDocument(configType1);

        session.save();
    }

    @Test
    public void testConfigurationType() {
        DocumentModel configuration = session.getDocument(new PathRef("/Configuration"));
        Assert.assertNotNull(configuration);

        DocumentModel configType1 = session.getDocument(new PathRef("/Configuration/ConfigurationBateau"));
        Assert.assertNotNull(configType1);
        Assert.assertEquals("Bateau", configType1.getPropertyValue("cft:type"));
    }
}
