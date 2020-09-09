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
package org.apache.camel.quarkus.component.fop.deployment;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;
import org.apache.fop.render.intermediate.IFUtil;
import org.jboss.jandex.IndexView;

class FopProcessor {

    private static final String FEATURE = "camel-fop";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    ReflectiveClassBuildItem registerForReflection(CombinedIndexBuildItem combinedIndex) {
        IndexView index = combinedIndex.getIndex();

                String[] dtos = index.getKnownClasses().stream()
                        .map(ci -> ci.name().toString())
                        .filter(n -> n.endsWith("ElementMapping"))
                        .sorted()
                        .peek(System.out::println)
                        .toArray(String[]::new);

                return new ReflectiveClassBuildItem(false, false, dtos);
//        return new ReflectiveClassBuildItem(false, false, new String[] { "org.apache.fop.fo.ElementMapping",
//                "org.apache.fop.fo.FOElementMapping", "org.apache.fop.render.pdf.extensions.PDFElementMapping",
//                "org.apache.fop.fo.extensions.InternalElementMapping",
//                "org.apache.fop.fo.extensions.OldExtensionElementMapping" });
    }

    @BuildStep
    IndexDependencyBuildItem registerDependencyForIndex() {
        return new IndexDependencyBuildItem("org.jboss.logging", "commons-logging-jboss-logging");
    }

    @BuildStep
    void addDependencies(BuildProducer<IndexDependencyBuildItem> indexDependency) {
        indexDependency.produce(new IndexDependencyBuildItem("org.jboss.logging", "commons-logging-jboss-logging"));
        indexDependency.produce(new IndexDependencyBuildItem("org.apache.xmlgraphics", "fop"));
    }

    @BuildStep
    NativeImageResourceBuildItem initResources() {
        //use regex, once this is implemented https://github.com/quarkusio/quarkus/issues/7033
        return new NativeImageResourceBuildItem(
                "META-INF/services/org.apache.fop.fo.ElementMapping");
    }

    @BuildStep
    public void registerRuntimeInitializedClasses(BuildProducer<RuntimeInitializedClassBuildItem> resource) {
        //org.apache.tika.parser.pdf.PDFParser (https://issues.apache.org/jira/browse/PDFBOX-4548)
        //        resource.produce(new RuntimeInitializedClassBuildItem("org.apache.pdfbox.pdmodel.font.PDType1Font"));
        resource.produce(new RuntimeInitializedClassBuildItem(IFUtil.class.getCanonicalName()));
        //        resource.produce(new RuntimeInitializedClassBuildItem("sun.font.TrueTypeFont"));
        //        resource.produce(new RuntimeInitializedClassBuildItem("sun.font.SunFontManager"));
//        resource.produce(new RuntimeInitializedClassBuildItem("sun.awt.X11FontManager"));
    }

}
