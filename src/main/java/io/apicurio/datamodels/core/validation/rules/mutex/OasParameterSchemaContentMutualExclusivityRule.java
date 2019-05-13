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

package io.apicurio.datamodels.core.validation.rules.mutex;

import io.apicurio.datamodels.core.Constants;
import io.apicurio.datamodels.core.models.common.IParameterDefinition;
import io.apicurio.datamodels.core.models.common.Parameter;
import io.apicurio.datamodels.core.validation.ValidationRule;
import io.apicurio.datamodels.core.validation.ValidationRuleMetaData;
import io.apicurio.datamodels.openapi.v3.models.Oas30Parameter;

/**
 * Implements the Parameter Schema/Content Mutual Exclusivity Rule.
 * @author eric.wittmann@gmail.com
 */
public class OasParameterSchemaContentMutualExclusivityRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasParameterSchemaContentMutualExclusivityRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    private boolean hasContent(Oas30Parameter contentParent) {
        return contentParent.getMediaTypes().size() > 0;
    }
    
    /**
     * @see io.apicurio.datamodels.combined.visitors.CombinedAllNodeVisitor#visitParameter(io.apicurio.datamodels.core.models.common.Parameter)
     */
    @Override
    public void visitParameter(Parameter node) {
        this.reportIf(hasValue(node.schema) && hasContent((Oas30Parameter) node), node, Constants.PROP_SCHEMA, map());
    }
    
    /**
     * @see io.apicurio.datamodels.combined.visitors.CombinedAllNodeVisitor#visitParameterDefinition(io.apicurio.datamodels.core.models.common.IParameterDefinition)
     */
    @Override
    public void visitParameterDefinition(IParameterDefinition node) {
        visitParameter((Parameter) node);
    }

}
