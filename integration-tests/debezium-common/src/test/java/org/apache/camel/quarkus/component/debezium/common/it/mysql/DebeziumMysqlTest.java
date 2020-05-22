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
package org.apache.camel.quarkus.component.debezium.common.it.mysql;

import java.sql.SQLException;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.camel.quarkus.component.debezium.common.it.AbstractDebeziumTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@QuarkusTestResource(DebeziumMysqlTestResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DebeziumMysqlTest extends AbstractDebeziumTest {

    @Override
    protected String getResourcePath() {
        return "/debezium-mysql";
    }

    @Test
    @Order(1)
    @Override
    public void insert() throws SQLException {

        //receive all empty messages before insert
        receiveResponse("receiveEmptyMessages")
                .then()
                .statusCode(204);

        executeUpdate("INSERT INTO COMPANY (name, city) VALUES ('" + COMPANY_1 + "', '" + CITY_1 + "')");

        receiveResponse()
                .then()
                .statusCode(200)
                .body(Matchers.containsString((COMPANY_1)));
    }
}
