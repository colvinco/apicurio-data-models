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

package io.apicurio.datamodels.core.validation.rules.invalid.type;

import io.apicurio.datamodels.core.Constants;
import io.apicurio.datamodels.core.models.common.Schema;
import io.apicurio.datamodels.core.validation.ValidationRuleMetaData;
import io.apicurio.datamodels.openapi.models.OasSchema;

/**
 * Implements the Invalid Schema Array Items rule.
 * @author eric.wittmann@gmail.com
 */
public class OasInvalidSchemaArrayItemsRule extends OasInvalidPropertyTypeValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasInvalidSchemaArrayItemsRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }
    
    /**
     * @see io.apicurio.datamodels.combined.visitors.CombinedAllNodeVisitor#visitSchema(io.apicurio.datamodels.core.models.common.Schema)
     */
    @Override
    public void visitSchema(Schema node) {
        OasSchema schema = (OasSchema) node;
        if (isDefined(schema.items) && !equals(schema.type, "array")) {
            this.report(schema, Constants.PROP_ITEMS, map());
        }
    }

}
