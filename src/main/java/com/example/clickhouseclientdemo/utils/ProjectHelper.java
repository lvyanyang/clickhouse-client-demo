package com.example.clickhouseclientdemo.utils;

import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.clickhouse.client.api.insert.InsertResponse;
import com.clickhouse.client.api.metadata.TableSchema;
import com.clickhouse.client.api.metrics.ClientMetrics;
import com.clickhouse.client.api.query.QueryResponse;
import com.clickhouse.client.api.query.Records;
import com.clickhouse.data.ClickHouseColumn;
import com.clickhouse.data.ClickHouseDataType;
import com.example.clickhouseclientdemo.entity.GpsLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 项目帮助类
 */
@Slf4j
public class ProjectHelper {
    //csv文件路径
    public static final String CSV_PATH = "/Users/lvyanyang/Desktop/tmp/gpslog_20240816.csv";
    //操作表名
    public static final String GPS_TABLE_NAME = "gpslog";

    /**
     * 输出Insert响应结果日志
     * @param startNanoTime 开始的纳秒数
     * @param response      Insert响应对象
     */
    public static void logInsertStat(long startNanoTime, InsertResponse response) {
        long duration = System.nanoTime() - startNanoTime;
        var serverWriteRows = response.getWrittenRows();
        var elapsed = TimeUnit.NANOSECONDS.toMillis(duration);//单位:毫秒
        var serverTime = TimeUnit.NANOSECONDS.toMillis(response.getServerTime());//单位:毫秒
        log.info("服务器写入行数:{},客户端执行耗时:{}ms,服务器执行耗时:{}ms", serverWriteRows, elapsed, serverTime);
    }

    /**
     * 输出查询响应结果日志
     * @param startNanoTime 开始的纳秒数
     * @param response      查询响应对象
     */
    public static void logQueryStat(long startNanoTime, QueryResponse response) {
        long duration = System.nanoTime() - startNanoTime;
        var serverReadRows = response.getReadRows();
        var resultRows = response.getResultRows();
        var elapsed = TimeUnit.NANOSECONDS.toMillis(duration);//单位:毫秒
        var serverTime = TimeUnit.NANOSECONDS.toMillis(response.getServerTime());//单位:毫秒
        var opDuration = TimeUnit.NANOSECONDS.toMillis(response.getMetrics().getMetric(ClientMetrics.OP_DURATION).getLong());
        log.info("服务器读取行数:{},结果行数:{},opDuration:{},客户端执行耗时:{}ms,服务器执行耗时:{}ms", serverReadRows, resultRows, opDuration, elapsed, serverTime);
    }

    /**
     * 输出查询响应结果日志
     * @param startNanoTime 开始的纳秒数
     * @param response      查询响应对象
     */
    public static void logQueryStat(long startNanoTime, Records response) {
        long duration = System.nanoTime() - startNanoTime;
        var serverReadRows = response.getReadRows();
        var resultRows = response.getResultRows();
        var elapsed = TimeUnit.NANOSECONDS.toMillis(duration);//单位:毫秒
        var serverTime = TimeUnit.NANOSECONDS.toMillis(response.getServerTime());//单位:毫秒
        var opDuration = TimeUnit.NANOSECONDS.toMillis(response.getMetrics().getMetric(ClientMetrics.OP_DURATION).getLong());
        log.info("服务器读取行数:{},结果行数:{},opDuration:{},客户端执行耗时:{}ms,服务器执行耗时:{}ms", serverReadRows, resultRows, opDuration, elapsed, serverTime);
    }

    /**
     * 将下划线方式命名的字符串转换为驼峰式。如果没有包含下划线,则整个字符串转为小写
     * @param name 转换前的下划线大写方式命名的字符串
     */
    public static String toCamelCase(CharSequence name) {
        if (null == name) return null;

        final String name2 = name.toString();
        if (StrUtil.contains(name2, CharUtil.UNDERLINE)) {
            return StrUtil.toCamelCase(name2);
        }
        return name2.toLowerCase();
    }

    /**
     * 将List<Map<String, String>>类型的数据转换为CSV格式的字符串
     * 此方法使用CSVPrinter来处理数据转换，确保输出的CSV格式正确
     * @param list 包含多个Map的列表，每个Map代表CSV中的一行，Map的键为列名，值为列值
     * @return 如果输入列表为空或null，则返回空字符串；否则返回转换后的CSV格式字符串
     */
    public static String listMapToCSVByCSVPrinter(List<Map<String, Object>> list) {
        if (list == null || list.isEmpty()) return "";

        // 获取所有的列名
        List<String> headers = new ArrayList<>(list.getFirst().keySet());
        StringWriter stringWriter = new StringWriter();
        try (CSVPrinter csvPrinter = new CSVPrinter(stringWriter, CSVFormat.DEFAULT.builder().setHeader(headers.toArray(new String[0])).build())) {
            for (Map<String, Object> map : list) {
                List<String> values = new ArrayList<>();
                for (String header : headers) {
                    values.add(map.get(header).toString());
                }
                csvPrinter.printRecord(values);
            }
        } catch (Exception e) {
            log.error("转换List<Map>为CSV失败 {}", e.getMessage(), e);
        }
        return stringWriter.toString();
    }

    /**
     * 将List<Map<String, String>>类型的列表转换为CSV格式的字符串
     * 此方法使用StringBuilder来构建CSV字符串，以减少字符串拼接过程中的内存消耗
     */
    public static String listMapToCSVByStringBuilder(List<Map<String, String>> list) {
        var delimiter = ",";
        StringBuilder csvBuilder = new StringBuilder();
        if (list != null && !list.isEmpty()) {
            // 写入列名
            Map<String, String> firstRecord = list.getFirst();
            csvBuilder.append(String.join(delimiter, firstRecord.keySet())).append("\n");

            // 写入数据行
            for (Map<String, String> record : list) {
                csvBuilder.append(String.join(delimiter, record.values())).append("\n");
            }
        }
        return csvBuilder.toString();
    }

    /**
     * 加载并解析CSV记录,并把记录转为GpsLog集合
     */
    public static List<GpsLog> readCSVToList() {
        List<GpsLog> list = new ArrayList<>();
        readCSVCore((record, headerNames) -> {
            GpsLog item = new GpsLog();
            // 硬编码给每个字段赋值
            // item.setGpsId(new BigInteger(record.get("gps_id")));
            // item.setCmpName(record.get("cmp_name"));
            // item.setCdName(record.get("cd_name"));
            // item.setRouteId(new BigInteger(record.get("route_id")));
            // item.setRouteName(record.get("route_name"));
            // item.setBusId(new BigInteger(record.get("bus_id")));
            // item.setBusCode(record.get("bus_code"));
            // item.setTerminalId(new BigInteger(record.get("terminal_id")));
            // item.setServiceId(new BigInteger(record.get("service_id")));
            // item.setRevTime(DateUtil.parseLocalDateTime(record.get("rev_time")));
            // item.setTerminalTime(DateUtil.parseLocalDateTime(record.get("terminal_time")));
            // item.setLongitude(record.get("longitude"));
            // item.setLatitude(record.get("latitude"));
            // item.setDirection(record.get("direction"));
            // item.setSpeed(new BigDecimal(record.get("speed")));
            // item.setSecondaryplanet(Short.parseShort(record.get("secondaryplanet")));
            // item.setCreatedTime(DateUtil.parseLocalDateTime(record.get("created_time")));
            // item.setResendFlag(Integer.parseInt(record.get("resend_flag")));
            // item.setValidFlag(Integer.parseInt(record.get("valid_flag")));
            // item.setMilestotal(Long.parseLong(record.get("milestotal")));
            headerNames.forEach(p -> {
                ReflectUtil.setFieldValue(item, ProjectHelper.toCamelCase(p), record.get(p));
            });
            list.add(item);
        });
        log.info("List集合数据行数:{}", list.size());
        return list;
    }

    /**
     * 加载并解析CSV记录,并把记录转为Map集合
     */
    public static List<Map<String, String>> readCSVToListMap() {
        List<Map<String, String>> list = new ArrayList<>();
        readCSVCore((record, headerNames) -> {
            Map<String, String> item = new HashMap<>();
            headerNames.forEach(p -> item.put(p, record.get(p)));
            list.add(item);
        });
        log.info("List<Map>集合数据行数:{}", list.size());
        return list;
    }

    /**
     * 解析CSV核心逻辑
     * @param consumer 指定遍历CSV集合时每项的实际操作函数
     */
    private static void readCSVCore(CSVConsumer consumer) {
        long startNanoTime = System.nanoTime();
        log.info("读取文件:{},开始解析CSV", CSV_PATH);
        try (FileInputStream fileInputStream = new FileInputStream(CSV_PATH);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader)) {
            List<String> headerNames = parser.getHeaderNames();
            for (CSVRecord record : parser) {
                consumer.accept(record, headerNames);
            }
            var elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanoTime);
            log.info("解析CSV成功,共耗时:{}ms", elapsed);
        } catch (Exception e) {
            log.error("解析CSV失败 {}", e.getMessage(), e);
        }
    }

    private final static Class<ClickHouseColumn> ClickHouseColumnClass = ClickHouseColumn.class;
    private static Field ClickHouseColumnField;

    /**
     * 处理DateTime类型的列的默认时区
     * @param schema      表架构
     * @param timeZone    设置的时区
     * @param columnNames 需要设置的列名,是表的列名,而不是对象的属性名称集合
     */
    public static void processTableSchemaLocalDateTimeZone(TableSchema schema, String timeZone, List<String> columnNames) {
        if (ClickHouseColumnField == null) {
            try {
                ClickHouseColumnField = ClickHouseColumnClass.getDeclaredField("timeZone");
            } catch (NoSuchFieldException e) {
                log.error("获取ClickHouse列对象timeZone字段时出错:{}", e.getMessage(), e);
            }
        }
        for (String columnName : columnNames) {
            ClickHouseColumn col = schema.getColumnByName(columnName);
            if (col == null) {
                log.warn("无效的列名,columnName={}", columnName);
                continue;
            }
            if (col.getDataType() != ClickHouseDataType.DateTime
                    && col.getDataType() != ClickHouseDataType.DateTime32
                    && col.getDataType() != ClickHouseDataType.DateTime64) {
                log.warn("此列不是DateTime类型,不进行处理,columnName={}", columnName);
                continue;
            }
            if (!ClickHouseColumnField.canAccess(col)) ClickHouseColumnField.setAccessible(true);
            try {
                ClickHouseColumnField.set(col, TimeZone.getTimeZone(timeZone));
            } catch (IllegalAccessException e) {
                log.error("设置ClickHouse列对象timeZone字段时出错:{} columnName={},timeZone={}", e.getMessage(), columnName, timeZone, e);
            }
        }
    }

    /**
     * CSV解析消费接口
     */
    @FunctionalInterface
    public interface CSVConsumer {
        void accept(CSVRecord record, List<String> headerNames);
    }
}
