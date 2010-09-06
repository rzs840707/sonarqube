/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.wsclient.unmarshallers;

import org.json.simple.JSONObject;
import org.sonar.wsclient.services.Favourite;

public class FavouriteUnmarshaller extends AbstractUnmarshaller<Favourite> {

  @Override
  protected Favourite parse(JSONObject json) {
    return new Favourite()
        .setId(JsonUtils.getInteger(json, "id"))
        .setKey(JsonUtils.getString(json, "key"))
        .setName(JsonUtils.getString(json, "name"))
        .setScope(JsonUtils.getString(json, "scope"))
        .setQualifier(JsonUtils.getString(json, "qualifier"))
        .setLanguage(JsonUtils.getString(json, "lang"));
  }
}
