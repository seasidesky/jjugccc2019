/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz;

import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Response;
import java.net.URL;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class MovieServiceTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackages(true, Api.class.getPackage())
                .addAsResource("META-INF/microprofile-config.properties");
    }

    @ArquillianResource
    private URL base;

    @Test
    public void testAsManager() throws Exception {
        final WebClient webClient = createWebClient(base);

        final Movie movie = new Movie(1, "The Matrix", "Lana Wachowski");

        final String claims = "{" +
                "  \"sub\":\"Jane Awesome\"," +
                "  \"iss\":\"https://server.example.com\"," +
                "  \"jti\":\"123456789\"," +
                "  \"役割\":[\"部長\",\"ユーザー\"]," +
                "  \"exp\":2552047942" +
                "}";

        final Response response = webClient.reset()
                .path("/api/movies")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + Tokens.asToken(claims))
                .post(movie);
        assertEquals(204, response.getStatus());
    }

    @Test
    public void testNotManager() throws Exception {
        final WebClient webClient = createWebClient(base);

        final Movie movie = new Movie(1, "The Matrix", "Lana Wachowski");

        final String claims = "{" +
                "  \"sub\":\"Jane Awesome\"," +
                "  \"iss\":\"https://server.example.com\"," +
                "  \"役割\":[\"ユーザー\"]," +
                "  \"exp\":2552047942" +
                "}";

        final Response response = webClient.reset()
                .path("/api/movies")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + Tokens.asToken(claims))
                .post(movie);
        assertEquals(403, response.getStatus());
    }

    private static WebClient createWebClient(final URL base) {
        return WebClient.create(base.toExternalForm(), singletonList(new JohnzonProvider<>()),
                singletonList(new LoggingFeature()), null);
    }

}
