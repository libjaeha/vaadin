/*
 * Copyright 2000-2016 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.client.connectors.grid;

import java.util.Set;

import com.vaadin.client.ServerConnector;
import com.vaadin.client.data.SelectionModel;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.data.DataCommunicatorConstants;
import com.vaadin.shared.data.selection.SelectionServerRpc;
import com.vaadin.shared.ui.Connect;

import elemental.json.JsonObject;

/**
 * Client side connector for grid single selection model.
 *
 * @author Vaadin Ltd.
 *
 * @since 8.0
 */
@Connect(com.vaadin.ui.components.grid.SingleSelectionModel.class)
public class SingleSelectionModelConnector extends AbstractExtensionConnector {

    @Override
    protected void extend(ServerConnector target) {
        getParent().getWidget()
                .setSelectionModel(new SelectionModel<JsonObject>() {

                    @Override
                    public void select(JsonObject item) {
                        getRpcProxy(SelectionServerRpc.class).select(
                                item.getString(DataCommunicatorConstants.KEY));
                    }

                    @Override
                    public void deselect(JsonObject item) {
                        getRpcProxy(SelectionServerRpc.class).deselect(
                                item.getString(DataCommunicatorConstants.KEY));
                    }

                    @Override
                    public Set<JsonObject> getSelectedItems() {
                        throw new UnsupportedOperationException(
                                "Selected item not known on the client side");
                    }

                    @Override
                    public boolean isSelected(JsonObject item) {
                        return SelectionModel.isItemSelected(item);
                    }

                });
    }

    @Override
    public GridConnector getParent() {
        return (GridConnector) super.getParent();
    }

}
