package com.wepay.kafka.connect.bigquery.write.row;

/*
 * Copyright 2016 WePay, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllResponse;

import com.wepay.kafka.connect.bigquery.config.BigQuerySinkTaskConfig;
import com.wepay.kafka.connect.bigquery.exception.BigQueryConnectException;

import org.apache.kafka.common.metrics.Metrics;
import org.apache.kafka.connect.data.Schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;


/**
 * A simple BigQueryWriter implementation. Sends the request to BigQuery, and throws an exception if
 * any errors occur as a result.
 */
public class SimpleBigQueryWriter extends BigQueryWriter {
  private static final Logger logger = LoggerFactory.getLogger(SimpleBigQueryWriter.class);

  private final BigQuery bigQuery;

  /**
   * @param bigQuery The object used to send write requests to BigQuery.
   * @param retry How many retries to make in the event of a 500/503 error.
   * @param retryWait How long to wait in between retries.
   * @param metrics Metrics for kcbq.
   */
  public SimpleBigQueryWriter(BigQuery bigQuery, int retry, long retryWait, Metrics metrics) {
    super(retry, retryWait, metrics);
    this.bigQuery = bigQuery;
  }

  /**
   * Sends the request to BigQuery, and throws an exception if any errors occur as a result of doing
   * so.
   * @param request The request to send to BigQuery.
   * @param topic The Kafka topic that the row data came from (ignored).
   * @param schemas The unique Schemas for the row data (ignored).
   */
  @Override
  public void performWriteRequest(InsertAllRequest request, String topic, Set<Schema> schemas) {
    InsertAllResponse writeResponse = bigQuery.insertAll(request);
    if (writeResponse.hasErrors()) {
      logger.warn(
          "You may want to enable auto schema updates by specifying"
          + "{}=true in the properties file",
          BigQuerySinkTaskConfig.SCHEMA_UPDATE_CONFIG
      );
      throw new BigQueryConnectException(writeResponse.insertErrors());
    } else {
      logger.debug("table insertion completed with no reported errors");
    }
  }
}
