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
package org.sonar.plugins.core.purges;

import org.apache.commons.lang.time.DateUtils;
import org.sonar.core.purge.AbstractPurge;
import org.sonar.api.batch.PurgeContext;
import org.sonar.api.database.DatabaseSession;
import org.sonar.api.database.model.Snapshot;
import org.sonar.api.resources.Resource;

import javax.persistence.Query;
import java.util.Date;
import java.util.List;

/**
 * @since 1.11
 */
public class PurgeEntities extends AbstractPurge {

  private static final int MINIMUM_AGE_IN_HOURS = -12;

  public PurgeEntities(DatabaseSession session) {
    super(session);
  }

  public void purge(PurgeContext context) {
    final Date beforeDate = DateUtils.addHours(new Date(), MINIMUM_AGE_IN_HOURS);
    Query query = getSession().createQuery("SELECT s.id FROM " + Snapshot.class.getSimpleName() + " s WHERE s.last=false AND scope=:scope AND s.createdAt<:date");
    query.setParameter("scope", Resource.SCOPE_ENTITY);
    query.setParameter("date", beforeDate);
    List<Integer> snapshotIds = query.getResultList();

    deleteSnapshotData(snapshotIds);
  }
}
