/*
 * Copyright 2007-2024 西安交通信息投资营运有限公司 版权所有
 */

package com.example.clickhouseclientdemo.service;

import com.clickhouse.client.api.Client;
import com.clickhouse.client.api.data_formats.ClickHouseBinaryFormatReader;
import com.clickhouse.client.api.insert.InsertResponse;
import com.clickhouse.client.api.metadata.TableSchema;
import com.clickhouse.client.api.query.GenericRecord;
import com.clickhouse.client.api.query.QueryResponse;
import com.clickhouse.client.api.query.QuerySettings;
import com.clickhouse.client.api.query.Records;
import com.clickhouse.data.ClickHouseFormat;
import com.example.clickhouseclientdemo.entity.GpsLog;
import com.example.clickhouseclientdemo.utils.ClientHelper;
import com.example.clickhouseclientdemo.utils.JsonHelper;
import com.fasterxml.jackson.databind.MappingIterator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class GpsService {
    private final Client client;
    private final TableSchema gpsLogSchema;
    private static final String SQL = "select gps_id,cmp_name,cd_name,route_id,route_name,bus_id,bus_code,terminal_id,service_id,rev_time,terminal_time,longitude,latitude,direction,speed,secondaryplanet,created_time,resend_flag,valid_flag,milestotal " +
            "from gpslog " +
            "order by created_time desc ";

    public GpsService(Client client) {
        this.client = client;
        gpsLogSchema = this.client.getTableSchema(ClientHelper.GPS_TABLE_NAME);
        this.client.register(GpsLog.class, gpsLogSchema);
    }

    /**
     * 将List集合写入ClickHouse
     */
    public void insertList(List<GpsLog> list) {
        long startNanoTime = System.nanoTime();
        log.info("开始将List集合写入到ClickHouse");
        try (InsertResponse response = client.insert(ClientHelper.GPS_TABLE_NAME, list).get(10, TimeUnit.SECONDS)) {
            ClientHelper.logInsertStat(startNanoTime, response);
        } catch (Exception e) {
            log.error("写入List失败 {}", e.getMessage(), e);
        }
    }

    /**
     * 将CSV文件数据写入ClickHouse
     */
    public void insertCSV() {
        long startNanoTime = System.nanoTime();
        log.info("读取文件:{},开始写入", ClientHelper.CSV_PATH);
        try (FileInputStream fileInputStream = new FileInputStream(ClientHelper.CSV_PATH);
             InsertResponse response = client.insert(ClientHelper.GPS_TABLE_NAME, fileInputStream, ClickHouseFormat.CSVWithNames).get(10, TimeUnit.SECONDS)) {
            ClientHelper.logInsertStat(startNanoTime, response);
        } catch (Exception e) {
            log.error("写入CSV失败 {}", e.getMessage(), e);
        }
    }

    /**
     * 将ListMap集合写入ClickHouse
     */
    public void insertListMap(List<Map<String, String>> listMap) {
        long startNanoTime = System.nanoTime();
        var csvString = ClientHelper.listMapToCSVByStringBuilder(listMap);
        InputStream inputStream = new ByteArrayInputStream(csvString.getBytes(StandardCharsets.UTF_8));
        var elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanoTime);
        log.info("将ListMap包装为InputStream,共耗时:{}ms", elapsed);

        log.info("开始将包装好的InputStream写入数据库");
        startNanoTime = System.nanoTime();
        try (InsertResponse response = client.insert(ClientHelper.GPS_TABLE_NAME, inputStream, ClickHouseFormat.CSVWithNames).get(10, TimeUnit.SECONDS)) {
            ClientHelper.logInsertStat(startNanoTime, response);
        } catch (Exception e) {
            log.error("写入ListMap失败 {}", e.getMessage(), e);
        }
    }

    public void query() {
        var sql = "select route_id,route_name from gpslog limit 100";
        log.info("开始查询数据:{}", sql);
        long startNanoTime = System.nanoTime();
        try (QueryResponse response = client.query(sql).get(10, TimeUnit.SECONDS)) {
            ClickHouseBinaryFormatReader reader = client.newBinaryFormatReader(response);
            while (reader.next() != null) {
                log.info("route_id={} route_name={}", reader.getString("route_id"), reader.getString("route_name"));
            }
            ClientHelper.logQueryStat(startNanoTime, response);
        } catch (Exception e) {
            log.error("查询数据失败 {}", e.getMessage(), e);
        }
    }

    public void queryListMap() {
        var sql = SQL + "limit 100";
        log.info("开始查询数据:{}", sql);
        long startNanoTime = System.nanoTime();
        try (Records response = client.queryRecords(sql).get(10, TimeUnit.SECONDS)) {
            ClientHelper.logQueryStat(startNanoTime, response);
            List<Map<String, Object>> listMap = new ArrayList<>();
            for (GenericRecord record : response) {
                Map<String, Object> map = new HashMap<>();
                gpsLogSchema.getColumns().forEach(p -> map.put(p.getColumnName(), record.getObject(p.getColumnName())));
                listMap.add(map);
            }
            listMap.forEach(p -> log.info("{}", JsonHelper.toJsonString(p, false)));
        } catch (Exception e) {
            log.error("查询数据失败 {}", e.getMessage(), e);
        }
    }

    public void queryCustomMap() {
        var sql = SQL + "limit 100";
        log.info("开始查询数据:{}", sql);
        long startNanoTime = System.nanoTime();
        try (QueryResponse response = client.query(sql).get(10, TimeUnit.SECONDS)) {
            ClickHouseBinaryFormatReader reader = client.newBinaryFormatReader(response);
            List<Map<String, Object>> listMap = new ArrayList<>();
            while (reader.next() != null) {
                HashMap<String, Object> map = new HashMap<>();
                gpsLogSchema.getColumns().forEach(column -> {
                    var columnName = column.getColumnName();
                    switch (column.getDataType()) {
                        case DateTime, DateTime32, DateTime64 -> map.put(columnName, reader.getLocalDateTime(columnName));
                        case Date, Date32 -> map.put(columnName, reader.getLocalDate(columnName));
                        case Decimal -> map.put(columnName, reader.getBigDecimal(columnName));
                        case Int64 -> map.put(columnName, reader.getLong(columnName));
                        case Int8 -> map.put(columnName, reader.getByte(columnName));
                        case UInt64 -> map.put(columnName, reader.getBigInteger(columnName));
                        default -> map.put(columnName, reader.getString(columnName));
                    }
                });
                listMap.add(map);
            }
            ClientHelper.logQueryStat(startNanoTime, response);
            listMap.forEach(p -> log.info("{}", JsonHelper.toJsonString(p, false)));
        } catch (Exception e) {
            log.error("查询数据失败 {}", e.getMessage(), e);
        }
    }

    public void queryList() {
        var sql = SQL + "limit 100";
        log.info("开始查询数据:{}", sql);
        long startNanoTime = System.nanoTime();
        try {
            List<GpsLog> list = client.queryAll(sql, GpsLog.class, gpsLogSchema, GpsLog::new);
            log.info("客户端执行耗时:{}ms", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanoTime));
            for (GpsLog record : list) {
                log.info(JsonHelper.toJsonString(record, false));
            }
        } catch (Exception e) {
            log.error("查询数据失败 {}", e.getMessage(), e);
        }
    }

    public void queryAsJsonToObject() {
        var sql = SQL + "limit 100";
        log.info("开始查询数据:{}", sql);
        long startNanoTime = System.nanoTime();
        try (QueryResponse response = client.query(sql, new QuerySettings().setFormat(ClickHouseFormat.JSONEachRow)).get(10,TimeUnit.SECONDS);
             MappingIterator<GpsLog> jsonIter = JsonHelper.getObjectMapper().readerFor(GpsLog.class).readValues(response.getInputStream())) {
            // MappingIterator<JsonNode> jsonIter = JsonHelper.getObjectMapper().readerFor(JsonNode.class).readValues(response.getInputStream()))
            List<GpsLog> list = new ArrayList<>();
            while (jsonIter.hasNext()) {
                GpsLog record = jsonIter.next();
                list.add(record);
            }
            ClientHelper.logQueryStat(startNanoTime, response);
            for (GpsLog record : list) {
                log.info(JsonHelper.toJsonString(record, false));
            }
        } catch (Exception e) {
            log.error("查询数据失败 {}", e.getMessage(), e);
        }
    }

    public void queryAsCSV() {
        String sql = SQL + "limit 100";
        log.info("开始查询数据表\n{}", sql);
        QuerySettings settings = new QuerySettings().setFormat(ClickHouseFormat.CSVWithNames);
        long startNanoTime = System.nanoTime();
        try (QueryResponse response = client.query(sql, settings).get(10, TimeUnit.SECONDS);
             BufferedReader reader = new BufferedReader(new InputStreamReader(response.getInputStream()));
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader)) {
            ClientHelper.logQueryStat(startNanoTime, response);

            log.info(String.join(",", parser.getHeaderNames()));
            for (CSVRecord record : parser) {
                log.info(String.join(",", record.values()));
            }
        } catch (Exception e) {
            log.error("查询数据表失败 {}", e.getMessage(), e);
        }
    }

    public void queryAsString() {
        String line;
        String sql = SQL + "limit 100 format JSONEachRow";
        log.info("开始查询数据表\n{}", sql);
        long startNanoTime = System.nanoTime();
        try (QueryResponse response = client.query(sql).get(10, TimeUnit.SECONDS);
             BufferedReader reader = new BufferedReader(new InputStreamReader(response.getInputStream()))) {
            ClientHelper.logQueryStat(startNanoTime, response);
            while ((line = reader.readLine()) != null) {
                log.info(line);
            }
        } catch (Exception e) {
            log.error("查询数据表失败 {}", e.getMessage(), e);
        }
    }
}

