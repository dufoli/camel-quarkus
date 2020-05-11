/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.camel.quarkus.component.debezium.postgres.it;

import java.util.Map;

import org.apache.camel.quarkus.testcontainers.ContainerResourceLifecycleManager;
import org.apache.camel.util.CollectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.TestcontainersConfiguration;

public class DebeziumPostgresTestResource implements ContainerResourceLifecycleManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DebeziumPostgresTestResource.class);

    private static final int POSTGRES_PORT = 5432;

    private PostgreSQLContainer<?> postgresContainer;

    @Override
    public Map<String, String> start() {
        LOGGER.info(TestcontainersConfiguration.getInstance().toString());

        try {
            postgresContainer = new PostgreSQLContainer<>("debezium/postgres:11")
                    .withNetworkAliases("postgres")
                    .withUsername("postgres")
                    .withPassword("postgres")
                    .withDatabaseName("postgres")
                    .withInitScript("init.sql");

            postgresContainer.start();

            return CollectionHelper.mapOf(
                    "database.hostname", postgresContainer.getContainerIpAddress(),
                    "database.port",
                    postgresContainer.getMappedPort(POSTGRES_PORT) + "",
                    "url", postgresContainer.getContainerIpAddress() + ":" + postgresContainer.getMappedPort(POSTGRES_PORT)
                            + "/postgres");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        try {
            if (postgresContainer != null) {
                postgresContainer.stop();
            }
        } catch (Exception e) {
            // ignored
        }
    }
}
