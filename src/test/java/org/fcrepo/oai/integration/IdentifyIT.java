/**
 * Copyright 2014 DuraSpace, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fcrepo.oai.integration;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.junit.Test;
import org.openarchives.oai._2.IdentifyType;
import org.openarchives.oai._2.OAIPMHtype;
import org.openarchives.oai._2.VerbType;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class IdentifyIT extends AbstractOAIProviderIT {

    @PostConstruct
    public void initTests() throws Exception {
        /* Check and/or add the default Identify response */
        if (!defaultIdentityResponseExists()) {
            IdentifyType id = this.oaiFactory.createIdentifyType();
            id.setRepositoryName("Fedora 4 Test Instance");
            id.setBaseURL(this.serverAddress);

            HttpPost post = new HttpPost(this.serverAddress);
            StringWriter data = new StringWriter();
            this.marshaller.marshal(new JAXBElement<IdentifyType>(new QName("Identify"), IdentifyType.class, id), data);
            post.setEntity(new StringEntity(data.toString()));
            post.addHeader("Content-Type","application/octet-stream");
            post.addHeader("Slug", "oai_identify");
            try {
                HttpResponse resp = this.client.execute(post);
                assertEquals(201, resp.getStatusLine().getStatusCode());
            } finally {
                post.releaseConnection();
            }
        }
    }

    protected boolean defaultIdentityResponseExists() throws IOException {
        HttpGet get = new HttpGet(serverAddress + "/oai_identify/fcr:content");
        try {
            HttpResponse resp = this.client.execute(get);
            return resp.getStatusLine().getStatusCode() == 200;
        } finally {
            get.releaseConnection();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIdentify() throws Exception {
        HttpResponse resp = getOAIPMHResponse(VerbType.IDENTIFY.value(), null, null, null, null, null);
        assertEquals(200, resp.getStatusLine().getStatusCode());
        OAIPMHtype oaipmh =
                ((JAXBElement<OAIPMHtype>) this.unmarshaller.unmarshal(resp.getEntity().getContent())).getValue();
        assertEquals(0, oaipmh.getError().size());
        assertNotNull(oaipmh.getIdentify());
        assertNotNull(oaipmh.getRequest());
        assertEquals(VerbType.IDENTIFY.value(), oaipmh.getRequest().getVerb().value());
        assertEquals("Fedora 4 Test Instance", oaipmh.getIdentify().getRepositoryName());
        assertEquals(serverAddress, oaipmh.getIdentify().getBaseURL());
    }
}
