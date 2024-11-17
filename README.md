# 使用Java语言并使用ClickHouseClient类库进行数据库的读写

# 测试表
```sql
CREATE TABLE gpslog
(
    `gps_id` UInt64 COMMENT '主键',
    `cmp_name` LowCardinality(String) COMMENT '公司名称',
    `cd_name` LowCardinality(String) COMMENT '车队名称',
    `route_id` UInt64 COMMENT '线路ID',
    `route_name` LowCardinality(String) COMMENT '线路名称',
    `bus_id` UInt64 COMMENT '车辆ID',
    `bus_code` LowCardinality(String) COMMENT '车辆编号',
    `terminal_id` UInt64 COMMENT '终端ID',
    `service_id` UInt64 COMMENT '服务ID',
    `rev_time` DateTime COMMENT '服务器接收时间',
    `terminal_time` DateTime COMMENT '终端时间',
    `longitude` String COMMENT '经度',
    `latitude` String COMMENT '纬度',
    `direction` String COMMENT '方向',
    `speed` Decimal(9, 2) COMMENT '速度',
    `secondaryplanet` UInt8 COMMENT '卫星数',
    `created_time` DateTime COMMENT '创建时间',
    `resend_flag` UInt16 COMMENT '发送标志',
    `valid_flag` UInt16 COMMENT '有效标志',
    `milestotal` UInt32 COMMENT '公里数'
)
ENGINE = MergeTree
PARTITION BY toYYYYMMDD(terminal_time)
PRIMARY KEY (bus_id, terminal_time)
ORDER BY (bus_id, terminal_time)
SETTINGS index_granularity = 8192
COMMENT '公交车辆GPS信息'
```


# 数据类型对应关系
官方文档：https://clickhouse.com/docs/en/integrations/java

| **Data Type**       | **Wrapper Class**              | **Value Class**         | **Remark**                                      |
| ------------------- | ------------------------------ | ----------------------- | ----------------------------------------------- |
| Bool                | ClickHouseBoolValue            | bool                    |                                                 |
| Date*               | ClickHouseDateValue            | java.time.LocalDate     |                                                 |
| DateTime*           | ClickHouseDateTimeValue        | java.time.LocalDateTime | or java.time.OffsetDateTime if there's timezone |
| Enum*               | ClickHouseEnumValue            | int                     |                                                 |
| FixedString         | ClickHouseStringValue          | byte[]                  |                                                 |
| Int8                | ClickHouseByteValue            | byte                    |                                                 |
| UInt8               | UnsignedByteValue              | byte                    | or short when widen_unsigned_types=true         |
| Int16               | ClickHouseShortValue           | short                   |                                                 |
| UInt16              | UnsignedShortValue             | short                   | or int when widen_unsigned_types=true           |
| Int32               | ClickHouseIntegerValue         | int                     |                                                 |
| UInt32              | UnsignedIntegerValue           | int                     | or long when widen_unsigned_types=true          |
| Int64               | ClickHouseLongValue            | long                    |                                                 |
| UInt64/Interval*    | UnsignedLongValue              | long                    | or BigInteger when widen_unsigned_types=true    |
| *Int128             | ClickHouseBigIntegerValue      | BigInteger              |                                                 |
| *Int256             | ClickHouseBigIntegerValue      | BigInteger              |                                                 |
| Decimal*            | ClickHouseBigDecimalValue      | BigDecimal              |                                                 |
| Float32             | ClickHouseFloatValue           | float                   |                                                 |
| Float64             | ClickHouseDoubleValue          | double                  |                                                 |
| IPv4                | ClickHouseIpv4Value            | java.net.Inet4Address   |                                                 |
| IPv6                | ClickHouseIpv6Value            | java.net.Inet6Address   |                                                 |
| UUID                | ClickHouseUuidValue            | java.util.UUID          |                                                 |
| Point               | ClickHouseGeoPointValue        | double[2]               |                                                 |
| Ring                | ClickHouseGeoRingValue         | double                  |                                                 |
| Polygon             | ClickHouseGeoPolygonValue      | double[]                |                                                 |
| MultiPolygon        | ClickHouseGeoMultiPolygonValue | double                  |                                                 |
| JSON/Object('json') | ClickHouseTupleValue           | java.util.List          |                                                 |
| String              | ClickHouseStringValue          | String                  | or byte[] when use_binary_string=true           |
| Array               | ClickHouseArrayValue           | primitive array         | or Object array when use_objects_in_array=true  |
| Map                 | ClickHouseMapValue             | java.util.Map           |                                                 |
| Nested              | ClickHouseNestedValue          | Object                  |                                                 |
| Tuple               | ClickHouseTupleValue           | java.util.List          |                                                 |

All wrapper classes implemented `ClickHouseValue` interface providing the ability to be converted from/to a specific Java type(e.g. via `update(String)` or `asString()`).