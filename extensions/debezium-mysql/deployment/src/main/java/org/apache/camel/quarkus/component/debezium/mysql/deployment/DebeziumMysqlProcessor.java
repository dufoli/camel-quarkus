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
package org.apache.camel.quarkus.component.debezium.mysql.deployment;

import java.util.ArrayList;
import java.util.stream.Collectors;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import org.jboss.jandex.IndexView;

class DebeziumMysqlProcessor {

    private static final String FEATURE = "camel-debezium-mysql";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    ReflectiveClassBuildItem registerForReflection(CombinedIndexBuildItem combinedIndex) {
        IndexView index = combinedIndex.getIndex();

        ArrayList<String> dtos = index.getKnownClasses().stream().map(ci -> ci.name().toString())
                .filter(n -> n.startsWith("org.apache.kafka.connect.json")
                        || n.startsWith("io.debezium.connector.mysql.MysqlConnector")
                        || n.startsWith("io.debezium.embedded.spi"))
                .sorted()
                .collect(Collectors.toCollection(ArrayList::new));

        dtos.add("org.apache.kafka.connect.storage.FileOffsetBackingStore");
        dtos.add("org.apache.kafka.connect.storage.MemoryOffsetBackingStore");

        return new ReflectiveClassBuildItem(false, true, dtos.toArray(new String[dtos.size()]));

    }

    @BuildStep
    void addDependencies(BuildProducer<IndexDependencyBuildItem> indexDependency) {
        indexDependency.produce(new IndexDependencyBuildItem("org.apache.kafka", "connect-json"));
        indexDependency.produce(new IndexDependencyBuildItem("io.debezium", "debezium-connector-mysql"));
        indexDependency.produce(new IndexDependencyBuildItem("io.debezium", "debezium-embedded"));
    }
}