package com.dga.clade.nuxeo.core;

import com.dga.clade.nuxeo.constant.Constants;
import com.dga.clade.nuxeo.core.operation.GetNbDocs;
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
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.webengine.util.ACLUtils;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import javax.inject.Inject;
import java.util.Map;

/**
 * @param
 * @Author mhkimi
 * @Date 25/07/2018
 * @Version 1.0
 * @return
 **/


@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@LocalDeploy("com.dga.clade.nuxeo.core")
public class TestGetNbDocs {
    private static final org.apache.commons.logging.Log log = LogFactory.getLog(TestGetNbDocs.class);

    @Inject
    CoreSession session;

    @Before
    public void createDocs() {
        // Création de la structure de documents servant aux tests
        DocumentModel rootFolder = session.createDocumentModel("/", "rootContainer", "Folder");
        rootFolder.setPropertyValue("dc:title", "root container");
        rootFolder.setPropertyValue("dc:description", "This is a root container");
        rootFolder.setPropertyValue("dc:source", "DGA");
        rootFolder.setPropertyValue("dc:creator","mhkimi");
        session.createDocument(rootFolder);
        session.save();

        DocumentModel file01 = session.createDocumentModel("/rootContainer/", "File01", "File");
        file01.setPropertyValue("dc:title", "File01");
        file01.setPropertyValue("dc:description", "This is a doc child 1");
        file01.setPropertyValue("dc:source", "DGA");
        file01.setPropertyValue("dc:creator","mhkimi");
        session.createDocument(file01);
        session.save();

        DocumentModel file02 = session.createDocumentModel("/rootContainer/", "File02", "File");
        file02.setPropertyValue("dc:title", "File02");
        file02.setPropertyValue("dc:description", "This is a doc child 2");
        file02.setPropertyValue("dc:source", "DGA");
        file02.setPropertyValue("dc:creator","mhkimi");
        session.createDocument(file02);
        session.save();

        DocumentModel Folder = session.createDocumentModel("/rootContainer/", "subContainer", "Folder");
        Folder.setPropertyValue("dc:title", "Sub container");
        Folder.setPropertyValue("dc:description", "This is a sub container");
        Folder.setPropertyValue("dc:source", "DGA");
        Folder.setPropertyValue("dc:creator","mhkimi");
        session.createDocument(Folder);
        session.save();

        DocumentModel Folder1 = session.createDocumentModel("/rootContainer/", "subContainer1", "Folder");
        Folder1.setPropertyValue("dc:title", "Sub container 1");
        Folder1.setPropertyValue("dc:description", "This is a sub container");
        Folder1.setPropertyValue("dc:source", "DGA");
        Folder1.setPropertyValue("dc:creator","mhkimi");
        session.createDocument(Folder1);
        session.save();

        DocumentModel file03 = session.createDocumentModel("/rootContainer/subContainer/", "File03", "File");
        file03.setPropertyValue("dc:title", "File03");
        file03.setPropertyValue("dc:description", "This is a doc child 3");
        file03.setPropertyValue("dc:source", "DGA");
        file03.setPropertyValue("dc:creator","mhkimi");
        session.createDocument(file03);
        session.save();
    }

    @Test
    public void testGetNbDocsFromFolder() {
        DocumentModel folderTodisplay = session.getDocument(new PathRef("/rootContainer"));
        Assert.assertNotNull(folderTodisplay);

        try {
            Map<String, Object> map = Maps.newHashMap();
            map.put("value", folderTodisplay);

            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob)NuxeoHelper.runOperation(session, null, GetNbDocs.ID, map));

            Assert.assertEquals(Constants.ENTITY_DOCUMENTS, object.get(Constants.ENTITY_TYPE));
            Assert.assertNull(object.get("count"));
            Assert.assertEquals(3, object.get("count-doc"));
            Assert.assertEquals(2, object.get("count-folder"));
        }catch(Exception e) {
            Assert.assertFalse(true);
        }
    }

    @Test
    public void testGetNbDocsFromEmptyFolder() {
        DocumentModel folderTodisplay = session.getDocument(new PathRef("/rootContainer/subContainer1"));
        Assert.assertNotNull(folderTodisplay);

        try {
            Map<String, Object> map = Maps.newHashMap();
            map.put("value", folderTodisplay);

            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob)NuxeoHelper.runOperation(session, null, GetNbDocs.ID, map));

            Assert.assertEquals(Constants.ENTITY_DOCUMENTS, object.get(Constants.ENTITY_TYPE));
            Assert.assertNull(object.get("count"));
            Assert.assertEquals(0, object.get("count-doc"));
           Assert.assertEquals(0, object.get("count-folder"));
        }catch(Exception e) {
            Assert.assertFalse(true);
        }
    }

    @Test
    public void testGetNbDocsWithoutPermission() {
        DocumentModel folderTodisplay = session.getDocument(new PathRef("/rootContainer"));
        ACP acp = session.getACP(folderTodisplay.getRef());
        ACL[] acls = acp.getACLs();
        ACL acl=acls[0];
        ACE[] aces = acl.getACEs();
        aces[0]= new ACE("Administrator","",true);
        ACLUtils.removePermission(session, folderTodisplay.getRef(),session.getPrincipal().getName(), SecurityConstants.READ_PROPERTIES);
        Assert.assertNotNull(folderTodisplay);
        Assert.assertTrue(session.hasPermission(session.getPrincipal(),folderTodisplay.getRef(), SecurityConstants.READ_PROPERTIES));

        try {
            Map<String, Object> map = Maps.newHashMap();
            map.put("value", folderTodisplay);
            map.put("metadatas", "");

            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob)NuxeoHelper.runOperation(session, null, GetNbDocs.ID, map));

            Assert.assertEquals(Constants.ENTITY_DOCUMENTS, object.get(Constants.ENTITY_TYPE));
            Assert.assertNull(object.get("count"));
            Assert.assertEquals(3, object.get("count-doc"));
            Assert.assertEquals(2, object.get("count-folder"));
        }catch(Exception e) {
            Assert.assertFalse(true);
        }
    }
}
