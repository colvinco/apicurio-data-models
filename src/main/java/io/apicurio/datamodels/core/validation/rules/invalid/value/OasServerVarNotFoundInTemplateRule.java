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

package io.apicurio.datamodels.core.validation.rules.invalid.value;

import java.util.ArrayList;
import java.util.List;

import io.apicurio.datamodels.compat.NodeCompat;
import io.apicurio.datamodels.core.models.common.ServerVariable;
import io.apicurio.datamodels.core.validation.ValidationRuleMetaData;
import io.apicurio.datamodels.openapi.v3.models.Oas30Server;

/**
 * @author eric.wittmann@gmail.com
 */
public class OasServerVarNotFoundInTemplateRule extends OasInvalidPropertyValueRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public OasServerVarNotFoundInTemplateRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * Parses the given server template for variable names.  For example, a server template might be
     *
     * https://{username}.gigantic-server.com:{port}/{basePath}
     *
     * In this case, this method will return [ "username", "port", "basePath" ]
     *
     * @param serverTemplate
     */
    private List<String> parseServerTemplate(String serverTemplate) {
        List<String> vars = new ArrayList<>();
        if (!hasValue(serverTemplate)) {
            return vars;
        }
        int startIdx = serverTemplate.indexOf("{");
        int endIdx = -1;
        while (startIdx != -1) {
            endIdx = serverTemplate.indexOf('}', startIdx);
            if (endIdx != -1) {
                vars.add(serverTemplate.substring(startIdx + 1, endIdx));
                startIdx = serverTemplate.indexOf('{', endIdx);
            } else {
                startIdx = -1;
            }
        }
        return vars;
    }

    /**
     * @see io.apicurio.datamodels.combined.visitors.CombinedAllNodeVisitor#visitServerVariable(io.apicurio.datamodels.core.models.common.ServerVariable)
     */
    @Override
    public void visitServerVariable(ServerVariable node) {
        String varName = node.getName();
        Oas30Server server = (Oas30Server) node.parent();
        List<String> vars = parseServerTemplate(server.url);
        String[] varArray = NodeCompat.asArray(vars);

        this.reportIfInvalid(isValidEnumItem(varName, varArray), node, null, map("name", varName));
    }

}
