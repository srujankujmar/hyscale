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
package io.hyscale.controller.directive;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.hyscale.servicespec.commons.model.service.ServiceSpec;

public interface ServiceSpecUpdateTestHandler {

    public String getServiceSpec();
    
    public boolean performValidation(ServiceSpec oldServiceSpec, ServiceSpec updatedServiceSpec);
    
    public ServiceSpec getUpdatedServiceSpec(ObjectNode serviceSpecNode);
    
}
