package com.dga.clade.nuxeo.helper;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;

/**
 * Classe de test de base du repository Nuxeo
 */
@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
public class TestNuxeoRespository {

    @Inject
    protected CoreSession session;

    @Test
    public void testGetSession() {
        Assert.assertNotNull(session);
    }

    @Test
    public void testGetDocument() {
        Assert.assertNotNull(session.getDocument(new PathRef("/default-domain")));
    }

    @Test
    public void testCreateDocument() {
        // Create a Document
        DocumentModel parent = session.getDocument(new PathRef("/default-domain"));
        Assert.assertNotNull(parent);

        DocumentModel newDoc = session.createDocumentModel(parent.getPathAsString(), "DOC01", "File");
        newDoc.setPropertyValue("dc:title", "DOC01");
        newDoc.setPropertyValue("dc:description", "This is document #1");

        newDoc = session.createDocument(newDoc);

        // Retrieve the created Document
        DocumentModelList docs = session.getChildren(parent.getRef(), "File");
        Assert.assertNotNull(docs);
        Assert.assertEquals(1, docs.size());

        DocumentModel retrievedDoc = docs.get(0);
        Assert.assertEquals(newDoc.getPropertyValue("dc:title"), retrievedDoc.getPropertyValue("dc:title"));
        Assert.assertEquals(newDoc.getId(), retrievedDoc.getId());
    }
}
