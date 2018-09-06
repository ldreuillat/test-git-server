package com.dga.clade.nuxeo.core;

import com.dga.clade.nuxeo.constant.Constants;
import com.dga.clade.nuxeo.core.operation.AutocompleteMetadata;
import com.dga.clade.nuxeo.helper.NuxeoHelper;
import com.google.common.collect.Maps;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.OperationException;
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

public class TestAutocompleteMetadata {

    @Inject
    CoreSession session;

    private DocumentModel firstDocument;
    private DocumentModel secondeDocument;
    private DocumentModel thirdDocument;
    private DocumentModel finalDocument;
    private DocumentModel myDocument;
    private DocumentModel otherDocument;

    @Before
    public void createDocAndChilds() throws OperationException {

        finalDocument = session.createDocumentModel("/", "Doc04", "File");
        finalDocument.setPropertyValue("dc:title", "demarches");
        finalDocument.setPropertyValue("dc:description", "Description 4");
        finalDocument.setPropertyValue("dc:language", "FR");
        finalDocument.setPropertyValue("dc:nature", "File");
        finalDocument.setPropertyValue("dc:source", "Ausy");
        finalDocument.setPropertyValue("dc:publisher", "pcmener");
        finalDocument = session.createDocument(finalDocument);

        thirdDocument = session.createDocumentModel("/", "Doc03", "Folder");
        thirdDocument.setPropertyValue("dc:title", "Dossier de traitement");
        thirdDocument.setPropertyValue("dc:description", "ma Description");
        thirdDocument.setPropertyValue("dc:language", "FR");
        thirdDocument.setPropertyValue("dc:nature", "Folder");
        thirdDocument.setPropertyValue("dc:source", "Ausy");
        thirdDocument.setPropertyValue("dc:publisher", "pcmener");
        thirdDocument = session.createDocument(thirdDocument);

        secondeDocument = session.createDocumentModel("/", "Doc02", "File");
        secondeDocument.setPropertyValue("dc:title", "Mon Developpement");
        secondeDocument.setPropertyValue("dc:description", "description 2");
        secondeDocument.setPropertyValue("dc:language", "FR");
        secondeDocument.setPropertyValue("dc:nature", "File");
        secondeDocument.setPropertyValue("dc:source", "Ausy");
        secondeDocument.setPropertyValue("dc:publisher", "amerroudj");
        secondeDocument = session.createDocument(secondeDocument);
        session.save();

        firstDocument = session.createDocumentModel("/", "Doc01", "Folder");
        firstDocument.setPropertyValue("dc:title", "mon premier Document");
        firstDocument.setPropertyValue("dc:description", "une desCription test");
        firstDocument = session.createDocument(firstDocument);
        session.save();


        myDocument = session.createDocumentModel("/", "Doc19", "Folder");
        myDocument.setPropertyValue("dc:title", "mon document test ");
        myDocument.setPropertyValue("dc:description", "Cette Description du doc");
        myDocument = session.createDocument(myDocument);
        session.save();

        otherDocument = session.createDocumentModel("/", "Doc21", "Folder");
        otherDocument.setPropertyValue("dc:title", "deuxieme test ");
        otherDocument.setPropertyValue("dc:description", "cela est une description");
        otherDocument = session.createDocument(otherDocument);
        session.save();

    }

    @Test
    //test pour retourner les valeurs d'autocompletion sans casse et sans specification du maximum.
    public void testAutocompletionMetadata() {

        Map<String, Object> map = Maps.newHashMap();

        map.put("metaname", "dc:title");
        map.put("value", "Dos");
        map.put("type", "Folder");
        map.put("repo", "test");

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob) NuxeoHelper.runOperation(session, null, AutocompleteMetadata.ID, map));

            Assert.assertEquals(Constants.ENTITY_METADATAS, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals(1, object.get(Constants.COUNT));

            JSONObject entries = (JSONObject)object.get(Constants.ENTRIES);
            JSONArray valuesArray = (JSONArray)entries.get("value");
            String values = valuesArray.toString();
            Assert.assertTrue(values.contains("Dossier de traitement"));
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

    }

    @Test
    //test pour retourner les valeurs d'autocompletion en spécifiant le champ casse et sans specification du maximum.
    public void testAutocompletionMetadataWithCasse() {

        Map<String, Object> map = Maps.newHashMap();
        Boolean casse = false;

        map.put("metaname", "dc:title");
        map.put("value", "D");
        map.put("type", "Folder");
        map.put("casesensitive", casse);
        map.put("repo", "test");

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob) NuxeoHelper.runOperation(session, null, AutocompleteMetadata.ID, map));

            Assert.assertEquals(Constants.ENTITY_METADATAS, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals(4, object.get(Constants.COUNT));
            JSONObject entries = (JSONObject)object.get(Constants.ENTRIES);
            JSONArray valuesArray = (JSONArray)entries.get("value");
            String values = valuesArray.toString();
            Assert.assertTrue(values.contains("Dossier de traitement"));
            Assert.assertTrue(values.contains("mon premier Document"));
            Assert.assertTrue(values.contains("mon document test"));
            Assert.assertTrue(values.contains("deuxieme test"));
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

    }

    //test pour retourner les valeurs d'autocompletion sans specification du casse et en specifiant le maximum.
    @Test
    public void testAutocompletionMetadataWithMax() {

        Map<String, Object> map = Maps.newHashMap();
        Integer maximum = 2;

        map.put("metaname", "dc:description");
        map.put("value", "c");
        map.put("type", "Folder");
        map.put("max", maximum);
        map.put("repo", "test");

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob) NuxeoHelper.runOperation(session, null, AutocompleteMetadata.ID, map));

            Assert.assertEquals(Constants.ENTITY_METADATAS, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals(2, object.get(Constants.COUNT));

        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    //test pour retourner les valeurs d'autocompletion avec specification du casse et du maximum.
    @Test
    public void testAutocompletionMetadataWithMaxAndCasse() {

        Map<String, Object> map = Maps.newHashMap();
        Integer maximum = 3;
        Boolean casse = false;


        map.put("metaname", "dc:description");
        map.put("value", "c");
        map.put("type", "Folder");
        map.put("max", maximum);
        map.put("casesensitive", casse);
        map.put("repo", "test");

        try {
            JSONObject object = NuxeoHelper.getJSONObjectFromBlob((Blob) NuxeoHelper.runOperation(session, null, AutocompleteMetadata.ID, map));

            Assert.assertEquals(Constants.ENTITY_METADATAS, object.get(Constants.ENTITY_TYPE));
            Assert.assertEquals(3, object.get(Constants.COUNT));

        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }
}