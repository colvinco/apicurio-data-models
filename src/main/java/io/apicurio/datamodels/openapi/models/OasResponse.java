/*
 * Copyright 2019 Red Hat
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

package io.apicurio.datamodels.openapi.models;

import io.apicurio.datamodels.core.models.ExtensibleNode;
import io.apicurio.datamodels.core.models.IReferenceNode;
import io.apicurio.datamodels.core.visitors.IVisitor;
import io.apicurio.datamodels.openapi.visitors.IOasVisitor;

/**
 * Models an OpenAPI 
 * @author eric.wittmann@gmail.com
 */
public class OasResponse extends ExtensibleNode implements IOasResponseDefinition, IReferenceNode {
    
    private String _name;
    public String $ref;
    public String description;
    
    /**
     * Constructor.
     * @param name
     */
    public OasResponse(String name) {
        this._name = name;
    }
    
    /**
     * Gets the response status code.
     */
    public String getStatusCode() {
        return this.getName();
    }

    /**
     * @see io.apicurio.datamodels.core.models.Node#accept(io.apicurio.datamodels.core.visitors.IVisitor)
     */
    @Override
    public void accept(IVisitor visitor) {
        IOasVisitor viz = (IOasVisitor) visitor;
        viz.visitResponse(this);
    }

    /**
     * @see io.apicurio.datamodels.core.models.common.IDefinition#getName()
     */
    @Override
    public String getName() {
        return this._name;
    }
    
}
