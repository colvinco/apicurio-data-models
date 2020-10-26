/*
 * Copyright 2020 Red Hat
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

package io.apicurio.datamodels.core.validation.rules.invalid.format;

import io.apicurio.datamodels.asyncapi.models.AaiCorrelationId;
import io.apicurio.datamodels.core.Constants;
import io.apicurio.datamodels.core.validation.ValidationRule;
import io.apicurio.datamodels.core.validation.ValidationRuleMetaData;

/**
 * Implements the Invalid Correlation ID Location Rule
 * @author eric.wittmann@gmail.com
 */
public class AaiInvalidCorrelationIdLocationRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public AaiInvalidCorrelationIdLocationRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }
    
    /**
     * @see io.apicurio.datamodels.combined.visitors.CombinedAllNodeVisitor#visitCorrelationId(io.apicurio.datamodels.asyncapi.models.AaiCorrelationId)
     */
    @Override
    public void visitCorrelationId(AaiCorrelationId node) {
        if (hasValue(node.location)) {
            this.reportIfInvalid(this.isValidAsyncApiExpression(node.location), node, Constants.PROP_LOCATION, map(node.getName()));
        }
    }

}