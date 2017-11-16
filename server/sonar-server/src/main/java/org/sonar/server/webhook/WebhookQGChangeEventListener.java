/*
 * SonarQube
 * Copyright (C) 2009-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.webhook;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.elasticsearch.action.search.SearchResponse;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.rules.RuleType;
import org.sonar.api.utils.System2;
import org.sonar.core.util.stream.MoreCollectors;
import org.sonar.db.DbClient;
import org.sonar.db.DbSession;
import org.sonar.db.component.AnalysisPropertyDto;
import org.sonar.db.component.BranchDto;
import org.sonar.db.component.ComponentDto;
import org.sonar.db.component.SnapshotDto;
import org.sonar.db.qualitygate.QualityGateConditionDto;
import org.sonar.server.es.Facets;
import org.sonar.server.es.SearchOptions;
import org.sonar.server.issue.IssueQuery;
import org.sonar.server.issue.index.IssueIndex;
import org.sonar.server.qualitygate.ShortLivingBranchQualityGate;
import org.sonar.server.qualitygate.changeevent.QGChangeEvent;
import org.sonar.server.qualitygate.changeevent.QGChangeEventListener;
import org.sonar.server.qualitygate.changeevent.Trigger;
import org.sonar.server.rule.index.RuleIndex;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Collections.singletonList;
import static org.sonar.core.util.stream.MoreCollectors.toSet;

public class WebhookQGChangeEventListener implements QGChangeEventListener {
  private final WebHooks webhooks;
  private final WebhookPayloadFactory webhookPayloadFactory;
  private final IssueIndex issueIndex;
  private final DbClient dbClient;
  private final System2 system2;

  public WebhookQGChangeEventListener(WebHooks webhooks, WebhookPayloadFactory webhookPayloadFactory, IssueIndex issueIndex, DbClient dbClient, System2 system2) {
    this.webhooks = webhooks;
    this.webhookPayloadFactory = webhookPayloadFactory;
    this.issueIndex = issueIndex;
    this.dbClient = dbClient;
    this.system2 = system2;
  }

  @Override
  public void onChanges(Trigger trigger, Collection<QGChangeEvent> changeEvents) {
    if (changeEvents.isEmpty()) {
      return;
    }

    List<QGChangeEvent> branchesWithWebhooks = changeEvents.stream()
      .filter(changeEvent -> webhooks.isEnabled(changeEvent.getProjectConfiguration()))
      .collect(MoreCollectors.toList());
    if (branchesWithWebhooks.isEmpty()) {
      return;
    }

    try (DbSession dbSession = dbClient.openSession(false)) {
      branchesWithWebhooks.forEach(event -> callWebhook(dbSession, event));
    }
  }

  private void callWebhook(DbSession dbSession, QGChangeEvent event) {
    webhooks.sendProjectAnalysisUpdate(
      event.getProjectConfiguration(),
      new WebHooks.Analysis(event.getShortBranch().getUuid(), event.getLatestAnalysis().getUuid(), null),
      () -> buildWebHookPayload(dbSession, event.getShortBranchComponent(), event.getShortBranch(), event.getLatestAnalysis()));
  }

  private WebhookPayload buildWebHookPayload(DbSession dbSession, ComponentDto branch, BranchDto shortBranch, SnapshotDto analysis) {
    Map<String, String> analysisProperties = dbClient.analysisPropertiesDao().selectBySnapshotUuid(dbSession, analysis.getUuid())
      .stream()
      .collect(Collectors.toMap(AnalysisPropertyDto::getKey, AnalysisPropertyDto::getValue));
    ProjectAnalysis projectAnalysis = new ProjectAnalysis(
      new Project(branch.getMainBranchProjectUuid(), branch.getKey(), branch.name()),
      null,
      new Analysis(analysis.getUuid(), analysis.getCreatedAt()),
      new Branch(false, shortBranch.getKey(), Branch.Type.SHORT),
      createQualityGate(branch, issueIndex),
      null,
      analysisProperties);
    return webhookPayloadFactory.create(projectAnalysis);
  }

  private QualityGate createQualityGate(ComponentDto branch, IssueIndex issueIndex) {
    SearchResponse searchResponse = issueIndex.search(IssueQuery.builder()
      .projectUuids(singletonList(branch.getMainBranchProjectUuid()))
      .branchUuid(branch.uuid())
      .mainBranch(false)
      .resolved(false)
      .checkAuthorization(false)
      .build(),
      new SearchOptions().addFacets(RuleIndex.FACET_TYPES));
    LinkedHashMap<String, Long> typeFacet = new Facets(searchResponse, system2.getDefaultTimeZone())
      .get(RuleIndex.FACET_TYPES);

    Set<QualityGate.Condition> conditions = ShortLivingBranchQualityGate.CONDITIONS.stream()
      .map(c -> toCondition(typeFacet, c))
      .collect(toSet(ShortLivingBranchQualityGate.CONDITIONS.size()));

    return new QualityGate(valueOf(ShortLivingBranchQualityGate.ID), ShortLivingBranchQualityGate.NAME, qgStatusFrom(conditions), conditions);
  }

  private static QualityGate.Condition toCondition(LinkedHashMap<String, Long> typeFacet, ShortLivingBranchQualityGate.Condition c) {
    long measure = getMeasure(typeFacet, c);
    QualityGate.EvaluationStatus status = measure > 0 ? QualityGate.EvaluationStatus.ERROR : QualityGate.EvaluationStatus.OK;
    return new QualityGate.Condition(status, c.getMetricKey(),
      toOperator(c),
      c.getErrorThreshold(), c.getWarnThreshold(), c.isOnLeak(),
      valueOf(measure));
  }

  private static QualityGate.Operator toOperator(ShortLivingBranchQualityGate.Condition c) {
    String operator = c.getOperator();
    switch (operator) {
      case QualityGateConditionDto.OPERATOR_GREATER_THAN:
        return QualityGate.Operator.GREATER_THAN;
      case QualityGateConditionDto.OPERATOR_LESS_THAN:
        return QualityGate.Operator.LESS_THAN;
      case QualityGateConditionDto.OPERATOR_EQUALS:
        return QualityGate.Operator.EQUALS;
      case QualityGateConditionDto.OPERATOR_NOT_EQUALS:
        return QualityGate.Operator.NOT_EQUALS;
      default:
        throw new IllegalArgumentException(format("Unsupported Condition operator '%s'", operator));
    }
  }

  private static QualityGate.Status qgStatusFrom(Set<QualityGate.Condition> conditions) {
    if (conditions.stream().anyMatch(c -> c.getStatus() == QualityGate.EvaluationStatus.ERROR)) {
      return QualityGate.Status.ERROR;
    }
    return QualityGate.Status.OK;
  }

  private static long getMeasure(LinkedHashMap<String, Long> typeFacet, ShortLivingBranchQualityGate.Condition c) {
    String metricKey = c.getMetricKey();
    switch (metricKey) {
      case CoreMetrics.BUGS_KEY:
        return getValueForRuleType(typeFacet, RuleType.BUG);
      case CoreMetrics.VULNERABILITIES_KEY:
        return getValueForRuleType(typeFacet, RuleType.VULNERABILITY);
      case CoreMetrics.CODE_SMELLS_KEY:
        return getValueForRuleType(typeFacet, RuleType.CODE_SMELL);
      default:
        throw new IllegalArgumentException(format("Unsupported metric key '%s' in hardcoded quality gate", metricKey));
    }
  }

  private static long getValueForRuleType(Map<String, Long> facet, RuleType ruleType) {
    Long res = facet.get(ruleType.name());
    if (res == null) {
      return 0L;
    }
    return res;
  }
}
