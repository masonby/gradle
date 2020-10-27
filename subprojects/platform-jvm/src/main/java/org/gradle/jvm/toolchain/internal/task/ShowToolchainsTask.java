/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.jvm.toolchain.internal.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.jvm.inspection.JvmInstallationMetadata;
import org.gradle.internal.jvm.inspection.JvmMetadataDetector;
import org.gradle.internal.logging.text.StyledTextOutput;
import org.gradle.internal.logging.text.StyledTextOutputFactory;
import org.gradle.jvm.toolchain.internal.InstallationLocation;
import org.gradle.jvm.toolchain.internal.SharedJavaInstallationRegistry;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ShowToolchainsTask extends DefaultTask {

    private static final Comparator<ReportableToolchain> TOOLCHAIN_COMPARATOR = Comparator
        .<ReportableToolchain, String>comparing(t -> t.metadata.getDisplayName())
        .thenComparing(t -> t.metadata.getLangageVersion());

    private final ToolchainReportRenderer toolchainRenderer = new ToolchainReportRenderer();

    public ShowToolchainsTask() {
        getOutputs().upToDateWhen(element -> false);
    }

    @TaskAction
    public void showToolchains() {
        StyledTextOutput output = getTextOutputFactory().create(getClass());
        toolchainRenderer.setOutput(output);
        output.println();
        List<ReportableToolchain> validToolchains = validToolchains();
        List<ReportableToolchain> invalidToolchains = invalidToolchains();
        validToolchains.forEach(toolchainRenderer::printToolchain);
        toolchainRenderer.printInvalidToolchains(invalidToolchains);
    }

    private List<ReportableToolchain> invalidToolchains() {
        return allReportableToolchains().stream().filter(t -> !isValidToolchain().test(t)).collect(Collectors.toList());
    }

    private List<ReportableToolchain> validToolchains() {
        return allReportableToolchains().stream().filter(isValidToolchain()).sorted(TOOLCHAIN_COMPARATOR).collect(Collectors.toList());
    }

    private Predicate<? super ReportableToolchain> isValidToolchain() {
        return t -> t.metadata.isValidInstallation();
    }

    private List<ReportableToolchain> allReportableToolchains() {
        return getInstallationRegistry().listInstallations().stream()
            .map(this::asReportableToolchain)
            .collect(Collectors.toList());
    }

    private ReportableToolchain asReportableToolchain(InstallationLocation location) {
        JvmInstallationMetadata metadata = getMetadataDetector().getMetadata(location.getLocation());
        return new ReportableToolchain(metadata, location);
    }

    @Inject
    protected SharedJavaInstallationRegistry getInstallationRegistry() {
        throw new UnsupportedOperationException();
    }

    @Inject
    protected JvmMetadataDetector getMetadataDetector() {
        throw new UnsupportedOperationException();
    }

    @Inject
    protected StyledTextOutputFactory getTextOutputFactory() {
        throw new UnsupportedOperationException();
    }


}
