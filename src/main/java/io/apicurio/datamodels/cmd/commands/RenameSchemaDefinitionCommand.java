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

package io.apicurio.datamodels.cmd.commands;

import java.util.ArrayList;
import java.util.List;

import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.asyncapi.models.AaiMessage;
import io.apicurio.datamodels.asyncapi.models.AaiSchema;
import io.apicurio.datamodels.cmd.AbstractCommand;
import io.apicurio.datamodels.cmd.util.ModelUtils;
import io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter;
import io.apicurio.datamodels.compat.JsonCompat;
import io.apicurio.datamodels.compat.LoggerCompat;
import io.apicurio.datamodels.compat.NodeCompat;
import io.apicurio.datamodels.core.models.Document;
import io.apicurio.datamodels.core.models.NodePath;
import io.apicurio.datamodels.core.models.common.IDefinition;
import io.apicurio.datamodels.core.models.common.IPropertySchema;
import io.apicurio.datamodels.core.models.common.Schema;
import io.apicurio.datamodels.core.util.VisitorUtil;
import io.apicurio.datamodels.core.visitors.TraverserDirection;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema.Oas30AnyOfSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema.Oas30NotSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema.Oas30OneOfSchema;

/**
 * A command used to rename a schema definition, along with all references to it.
 * @author eric.wittmann@gmail.com
 */
public abstract class RenameSchemaDefinitionCommand extends AbstractCommand {

    public String _oldName;
    public String _newName;
    public List<NodePath> _references;
    public List<NodePath> _messageReferences;
    
    RenameSchemaDefinitionCommand() {
    }
    
    RenameSchemaDefinitionCommand(String oldName, String newName) {
        this._oldName = oldName;
        this._newName = newName;
    }

    /**
     * @see io.apicurio.datamodels.cmd.ICommand#execute(io.apicurio.datamodels.core.models.Document)
     */
    @Override
    public void execute(Document document) {
        LoggerCompat.info("[RenameSchemaDefinitionCommand] Executing.");
        this._references = new ArrayList<>();
        this._messageReferences = new ArrayList<>();
        if (this._renameSchemaDefinition(document, this._oldName, this._newName)) {
            String oldRef = this._nameToReference(this._oldName);
            String newRef = this._nameToReference(this._newName);
            SchemaRefFinder schemaFinder = new SchemaRefFinder(oldRef);
            SchemaRefFinder.ReferenceHolder referenceHolder = schemaFinder.findIn(document);
            for (Schema schema : referenceHolder.getSchemas()) {
                this._references.add(Library.createNodePath(schema));
                schema.$ref = newRef;
            }
            for (AaiMessage aaiMessage : referenceHolder.getMessages()) {
                this._messageReferences.add(Library.createNodePath(aaiMessage));
                JsonCompat.setPropertyString(aaiMessage.payload, "$ref", newRef);
            }
        }
    }
    
    /**
     * @see io.apicurio.datamodels.cmd.ICommand#undo(io.apicurio.datamodels.core.models.Document)
     */
    @Override
    public void undo(Document document) {
        LoggerCompat.info("[RenameSchemaDefinitionCommand] Reverting.");
        if (this._renameSchemaDefinition(document, this._newName, this._oldName)) {
            String oldRef = this._nameToReference(this._oldName);
            if (ModelUtils.isDefined(this._references)) {
                this._references.forEach(ref -> {
                    Schema schema = (Schema) ref.resolve(document);
                    schema.$ref = oldRef;
                });
            }
            if (ModelUtils.isDefined(this._messageReferences)) {
                this._messageReferences.forEach(ref -> {
                    AaiMessage aaiMessage = (AaiMessage) ref.resolve(document);
                    JsonCompat.setPropertyString(aaiMessage.payload, "$ref", oldRef);
                });
            }
        }
    }
    
    /**
     * Convert a simple name to a reference.  This will be different for 2.0 vs. 3.0
     * data models.
     */
    protected abstract String _nameToReference(String name);

    /**
     * Called to actually change the name of the schema definition.  This impl will vary
     * depending on the OAI data model version.  Returns true if the rename actually happened.
     */
    protected abstract boolean _renameSchemaDefinition(Document document, String fromName, String toName);
    
    private static class SchemaRefFinder extends CombinedVisitorAdapter {

        private String _reference;
        private ReferenceHolder referenceHolder = new ReferenceHolder();

        /**
         * Constructor.
         */
        public SchemaRefFinder(String reference) {
            this._reference = reference;
        }

        public ReferenceHolder findIn(Document document) {
            VisitorUtil.visitTree(document, this, TraverserDirection.down);
            return this.referenceHolder;
        }

        protected boolean _accept(Schema schema) {
            return ModelUtils.isDefined(schema.$ref) && NodeCompat.equals(schema.$ref, this._reference);
        }

        protected boolean _accept(AaiMessage message) {
            return ModelUtils.isDefined(message.payload) && NodeCompat.equals(JsonCompat.getPropertyString(message.payload, "$ref"), this._reference);
        }

        protected void processSchema(Schema schema) {
            if (this._accept(schema)) {
                this.referenceHolder.addSchema(schema);
            }
        }

        protected void processMessage(AaiMessage message) {
            if (this._accept(message)) {
                this.referenceHolder.addMessage(message);
            }
        }
        
        /**
         * @see io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter#visitSchema(io.apicurio.datamodels.core.models.common.Schema)
         */
        @Override
        public void visitSchema(Schema node) {
            this.processSchema(node);
        }
        
        /**
         * @see io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter#visitSchemaDefinition(io.apicurio.datamodels.core.models.common.IDefinition)
         */
        @Override
        public void visitSchemaDefinition(IDefinition node) {
            this.processSchema((Schema) node);
        }
        
        /**
         * @see io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter#visitPropertySchema(io.apicurio.datamodels.core.models.common.IPropertySchema)
         */
        @Override
        public void visitPropertySchema(IPropertySchema node) {
            this.processSchema((Schema) node);
        }
        
        /**
         * @see io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter#visitAdditionalPropertiesSchema(io.apicurio.datamodels.openapi.models.OasSchema)
         */
        @Override
        public void visitAdditionalPropertiesSchema(OasSchema node) {
            this.processSchema(node);
        }
        
        /**
         * @see io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter#visitAllOfSchema(io.apicurio.datamodels.openapi.models.OasSchema)
         */
        @Override
        public void visitAllOfSchema(OasSchema node) {
            this.processSchema(node);
        }
        
        /**
         * @see io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter#visitItemsSchema(io.apicurio.datamodels.openapi.models.OasSchema)
         */
        @Override
        public void visitItemsSchema(OasSchema node) {
            this.processSchema(node);
        }
        
        /**
         * @see io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter#visitAnyOfSchema(io.apicurio.datamodels.openapi.v3.models.Oas30Schema.Oas30AnyOfSchema)
         */
        @Override
        public void visitAnyOfSchema(Oas30AnyOfSchema node) {
            this.processSchema(node);
        }
        
        /**
         * @see io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter#visitOneOfSchema(io.apicurio.datamodels.openapi.v3.models.Oas30Schema.Oas30OneOfSchema)
         */
        @Override
        public void visitOneOfSchema(Oas30OneOfSchema node) {
            this.processSchema(node);
        }
        
        /**
         * @see io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter#visitNotSchema(io.apicurio.datamodels.openapi.v3.models.Oas30Schema.Oas30NotSchema)
         */
        @Override
        public void visitNotSchema(Oas30NotSchema node) {
            this.processSchema(node);
        }

        /**
         * @see io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter#visitAllOfSchema(io.apicurio.datamodels.asyncapi.models.AaiSchema)
         */
        @Override
        public void visitAllOfSchema(AaiSchema node) {
            this.processSchema(node);
        }

        /**
         * @see io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter#visitOneOfSchema(io.apicurio.datamodels.asyncapi.models.AaiSchema)
         */
        @Override
        public void visitOneOfSchema(AaiSchema node) {
            this.processSchema(node);
        }

        /**
         * @see io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter#visitAnyOfSchema(io.apicurio.datamodels.asyncapi.models.AaiSchema)
         */
        @Override
        public void visitAnyOfSchema(AaiSchema node) {
            this.processSchema(node);
        }

        /**
         * @see io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter#visitNotSchema(io.apicurio.datamodels.asyncapi.models.AaiSchema)
         */
        @Override
        public void visitNotSchema(AaiSchema node) {
            this.processSchema(node);
        }

        /**
         * @see io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter#visitItemsSchema(io.apicurio.datamodels.asyncapi.models.AaiSchema)
         */
        @Override
        public void visitItemsSchema(AaiSchema node) {
            this.processSchema(node);
        }

        /**
         * @see io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter#visitAdditionalPropertiesSchema(io.apicurio.datamodels.asyncapi.models.AaiSchema)
         */
        @Override
        public void visitAdditionalPropertiesSchema(AaiSchema node) {
            this.processSchema(node);
        }

        /**
         * Payloads from AaiMessage are not visitable (typed as {@link Object}) and therefore need a bit of manipulation
         * to retrieve their $references
         * @see io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter#visitMessage(io.apicurio.datamodels.asyncapi.models.AaiMessage)
         * @see io.apicurio.datamodels.asyncapi.models.AaiMessage#payload
         */
        @Override
        public void visitMessage(AaiMessage node) {
            this.processMessage(node);
        }

        private static class ReferenceHolder {

            private List<Schema> schemas = new ArrayList<>();
            private List<AaiMessage> messages = new ArrayList<>();

            /**
             * @return the schemas
             */
            public List<Schema> getSchemas() {
                return schemas;
            }

            /**
             * @return the aaiMessages
             */
            public List<AaiMessage> getMessages() {
                return messages;
            }

            public void addSchema(Schema schema) {
                this.schemas.add(schema);
            }

            public void addMessage(AaiMessage aaiMessage) {
                this.messages.add(aaiMessage);
            }

        }
    }

}
