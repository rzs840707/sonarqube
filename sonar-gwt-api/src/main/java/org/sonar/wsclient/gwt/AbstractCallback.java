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
package org.sonar.wsclient.gwt;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.Widget;
import org.sonar.wsclient.services.Model;

public abstract class AbstractCallback<MODEL extends Model> implements Callback<MODEL> {

  private Widget loadingWidget = null;

  protected AbstractCallback(Widget loadingWidget) {
    this.loadingWidget = loadingWidget;
  }

  protected AbstractCallback() {
  }

  public void onResponse(MODEL result, JavaScriptObject json) {
    hideLoadingWidget();
    doOnResponse(result);
  }

  protected abstract void doOnResponse(MODEL result);

  public final void onTimeout() {
    doOnTimeout();
    hideLoadingWidget();
  }

  public final void onError(int errorCode, String errorMessage) {
    doOnError(errorCode, errorMessage);
    hideLoadingWidget();
  }

  protected void doOnError(int errorCode, String errorMessage) {

  }

  protected void doOnTimeout() {

  }

  private void hideLoadingWidget() {
    if (loadingWidget != null) {
      loadingWidget.removeFromParent();
    }
  }
}