/**
 * Copyright 2019 Pramati Prism, Inc.
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
package io.hyscale.generator.services.builder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Sets;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.NormalizationUtil;
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.generator.services.exception.ManifestErrorCodes;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.plugin.framework.util.GsonSnippetConvertor;
import io.hyscale.servicespec.commons.model.service.Port;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.openapi.models.V1ContainerPort;
import io.kubernetes.client.openapi.models.V1ServicePort;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

public class DefaultPortsBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPortsBuilder.class);

    public static List<ManifestSnippet> generatePortsManifest(List<Port> portList, String podSpecOwner,
                                                              List<ManifestSnippet> manifestSnippetList) throws HyscaleException {
        if (portList != null && !portList.isEmpty()) {
            Set<V1ContainerPort> v1ContainerPorts = Sets.newHashSet();
            Set<V1ServicePort> v1ServicePorts = Sets.newHashSet();
            portList.stream().filter(port -> port != null && StringUtils.isNotBlank(port.getPort())).forEach(each -> {
                V1ContainerPort v1ContainerPort = new V1ContainerPort();
                V1ServicePort v1ServicePort = new V1ServicePort();
                String[] portAndProtocol = each.getPort().split("/");
                String protocol = DefaultPortsBuilder.ServiceProtocol.TCP.name();
                
                if (portAndProtocol.length > 1) {
                    protocol = DefaultPortsBuilder.ServiceProtocol.fromString(portAndProtocol[1]).name();
                }
                String portName = NormalizationUtil.normalize(portAndProtocol[0] + ManifestGenConstants.NAME_DELIMITER + protocol);
                v1ContainerPort.setProtocol(protocol);
                v1ServicePort.setProtocol(protocol);
                int portValue = Integer.parseInt(portAndProtocol[0]);
                v1ContainerPort.setContainerPort(portValue);
                v1ContainerPort.setName(portName);
                logger.debug("Processing ports {}.", v1ContainerPort.getName());
                v1ContainerPorts.add(v1ContainerPort);
                v1ServicePort.setName(portName);
                logger.debug("Processing ports {}.", v1ServicePort.getName());
                v1ServicePort.setPort(portValue);
                v1ServicePort.setTargetPort(new IntOrString(portValue));
                v1ServicePorts.add(v1ServicePort);
                logger.debug("Fetched container and service ports ");
            });
            try {
                manifestSnippetList.add(buildServicePortsSnippet(v1ServicePorts));
                manifestSnippetList.add(buildContainerPortsSnippet(v1ContainerPorts, podSpecOwner));
                logger.info("Successfully completed processing ports");
            } catch (Exception e) {
                HyscaleException ex = new HyscaleException(e, ManifestErrorCodes.ERROR_WHILE_CREATING_MANIFEST);
                logger.error("Error while generating Manifest Files", ex);
                throw ex;
            }
        }
        return manifestSnippetList;
    }

    private static ManifestSnippet buildContainerPortsSnippet(Set<V1ContainerPort> containerPorts, String podSpecOwner)
            throws JsonProcessingException {
        ManifestSnippet manifestSnippet = new ManifestSnippet();
        manifestSnippet.setKind(podSpecOwner);
        manifestSnippet.setPath("spec.template.spec.containers[0].ports");
        manifestSnippet.setSnippet(GsonSnippetConvertor.serialize(containerPorts));
        return manifestSnippet;
    }


    private static ManifestSnippet buildServicePortsSnippet(Set<V1ServicePort> servicePorts)
            throws JsonProcessingException {
        ManifestSnippet manifestSnippet = new ManifestSnippet();
        manifestSnippet.setKind(ManifestResource.SERVICE.getKind());
        manifestSnippet.setPath("spec.ports");
        manifestSnippet.setSnippet(GsonSnippetConvertor.serialize(servicePorts));
        return manifestSnippet;
    }

    public enum ServiceProtocol {
        TCP("tcp"), UDP("udp");

        private final String protocolString;

        ServiceProtocol(String protocolString) {
            this.protocolString = protocolString;
        }

        public static DefaultPortsBuilder.ServiceProtocol fromString(String protocol) {
            if (StringUtils.isBlank(protocol)) {
                return TCP;
            }
            for (DefaultPortsBuilder.ServiceProtocol serviceProtocol : DefaultPortsBuilder.ServiceProtocol.values()) {
                if (serviceProtocol.getProtocolString().equalsIgnoreCase(protocol)) {
                    return serviceProtocol;
                }
            }
            return TCP;
        }

        public String getProtocolString() {
            return protocolString;
        }
    }
}
