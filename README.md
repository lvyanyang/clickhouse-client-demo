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

| **clickhouse数据类型**  | **Java类型**              | **包装类**                       | **备注**                                          |
|---------------------|-------------------------|-------------------------------|-------------------------------------------------|
| Bool                | bool                    | ClickHouseBoolValue           |                                                 |
| Date*               | java.time.LocalDate     | ClickHouseDateValue           |                                                 |
| DateTime*           | java.time.LocalDateTime | ClickHouseDateTimeValue       | or java.time.OffsetDateTime if there's timezone |
| DateTime64*         | java.time.OffsetDateTime | ClickHouseDateTimeValue      |                                                 |
| Enum*               | int                     | ClickHouseEnumValue           |                                                 |
| FixedString         | byte[]                  | ClickHouseStringValue         |                                                 |
| Int8                | byte                    | ClickHouseByteValue           |                                                 |
| UInt8               | short                   | UnsignedByteValue             |                                                 |
| Int16               | short                   | ClickHouseShortValue          |                                                 |
| UInt16              | int                     | UnsignedShortValue            |                                                 |
| Int32               | int                     | ClickHouseIntegerValue        |                                                 |
| UInt32              | long                    | UnsignedIntegerValue          |                                                 |
| Int64               | long                    | ClickHouseLongValue           |                                                 |
| UInt64/Interval*    | BigInteger              | UnsignedLongValue             |                                                 |
| *Int128             | BigInteger              | ClickHouseBigIntegerValue     |                                                 |
| *Int256             | BigInteger              | ClickHouseBigIntegerValue     |                                                 |
| Decimal*            | BigDecimal              | ClickHouseBigDecimalValue     |                                                 |
| Float32             | float                   | ClickHouseFloatValue          |                                                 |
| Float64             | double                  | ClickHouseDoubleValue         |                                                 |
| IPv4                | java.net.Inet4Address   | ClickHouseIpv4Value           |                                                 |
| IPv6                | java.net.Inet6Address   | ClickHouseIpv6Value           |                                                 |
| UUID                | java.util.UUID          | ClickHouseUuidValue           |                                                 |
| Point               | double[2]               | ClickHouseGeoPointValue       |                                                 |
| Ring                | double                  | ClickHouseGeoRingValue        |                                                 |
| Polygon             | double[]                | ClickHouseGeoPolygonValue     |                                                 |
| MultiPolygon        | double                  | ClickHouseGeoMultiPolygonValue |                                                 |
| JSON/Object('json') | java.util.List          | ClickHouseTupleValue          |                                                 |
| String              | String                  | ClickHouseStringValue         | or byte[] when use_binary_string=true           |
| Array               | primitive array         | ClickHouseArrayValue          | or Object array when use_objects_in_array=true  |
| Map                 | java.util.Map           | ClickHouseMapValue            |                                                 |
| Nested              | Object                  | ClickHouseNestedValue         |                                                 |
| Tuple               | java.util.List          | ClickHouseTupleValue          |                                                 |


# gpslog.csv
```sql
gps_id,cmp_name,cd_name,route_id,route_name,bus_id,bus_code,terminal_id,service_id,rev_time,terminal_time,longitude,latitude,direction,speed,secondaryplanet,created_time,resend_flag,valid_flag,milestotal
9095606756,八公司,四车队,8341,8路,101136,171136,100017112,23264,2024-08-16 10:23:02,2024-08-16 10:22:59,108.959112,34.26116,274,25.82,11,2024-08-16 10:23:02,0,0,43161
9095606801,五公司,六车队,5944,6路,121957,181658,100018747,21068,2024-08-16 10:23:02,2024-08-16 10:22:59,108.882584,34.192004,213,0,10,2024-08-16 10:23:02,0,0,73730
9095606813,四公司,二车队,11352,9路,80940,161372,100015907,46265,2024-08-16 10:23:02,2024-08-16 10:22:59,108.864616,34.288904,103,34.71,12,2024-08-16 10:23:02,0,0,48652
9095607034,五公司,六车队,17742,高新7号线,123356,181228,100018860,103266,2024-08-16 10:23:02,2024-08-16 10:22:59,108.806736,34.11364,39,0,12,2024-08-16 10:23:02,0,0,63322
9095607919,二公司,一车队,12,45路,78139,161266,100015691,22,2024-08-16 10:23:07,2024-08-16 10:23:01,108.974432,34.252444,88,22.69,15,2024-08-16 10:23:07,0,0,65366
9095608027,四公司,五车队,11350,223路,67336,161335,100015200,55861,2024-08-16 10:23:07,2024-08-16 10:23:02,108.83104,34.321728,316,0.04,9,2024-08-16 10:23:07,0,0,63594
9095608226,五公司,二车队,13541,271路,115353,181066,100018188,75262,2024-08-16 10:23:07,2024-08-16 10:23:01,108.87176,34.194372,315,0,11,2024-08-16 10:23:07,0,0,54217
9095608317,二公司,五车队,105,102路,100748,171220,100017099,380,2024-08-16 10:23:07,2024-08-16 10:22:59,108.964192,34.26078,77,15.08,15,2024-08-16 10:23:07,0,0,6817
9095608340,四公司,六车队,11353,24路,115557,180579,100018232,51476,2024-08-16 10:23:07,2024-08-16 10:23:03,108.968248,34.210396,66,31.45,10,2024-08-16 10:23:07,0,0,48664
9095608395,四公司,六车队,11151,12路,97351,170807,100016880,51664,2024-08-16 10:23:07,2024-08-16 10:23:04,108.942168,34.219488,0,20.35,9,2024-08-16 10:23:07,0,0,43897
9095608626,四公司,六车队,11341,106路,85548,161430,100016170,46271,2024-08-16 10:23:07,2024-08-16 10:23:05,108.8854,34.261884,357,0,8,2024-08-16 10:23:07,0,0,38635
9095608687,四公司,五车队,11150,222路,116559,181577,100018334,56461,2024-08-16 10:23:07,2024-08-16 10:23:04,108.796176,34.305628,182,35.45,13,2024-08-16 10:23:07,0,0,38604
9095608690,二公司,五车队,121,40路,172957,186010,64625,317285,2024-08-16 10:23:07,2024-08-16 10:23:04,108.825944,34.247092,251,0,10,2024-08-16 10:23:07,0,0,56362
9095608846,四公司,二车队,11142,104路,116985,180552,100018434,45663,2024-08-16 10:23:07,2024-08-16 10:23:04,108.86248,34.279932,287,0,9,2024-08-16 10:23:07,0,0,80502
9095608917,三公司,七车队,6543,28路,103543,171220,100017222,21666,2024-08-16 10:23:07,2024-08-16 10:23:04,108.927688,34.27118,97,0.04,15,2024-08-16 10:23:07,0,0,27224
9095609229,八公司,五车队,9343,262路,65735,161204,100015000,24262,2024-08-16 10:23:07,2024-08-16 10:23:05,109.070136,34.31818,38,0.43,10,2024-08-16 10:23:07,0,0,67447
9095609293,三公司,三车队,6142,27路,112976,181248,100017973,21071,2024-08-16 10:23:07,2024-08-16 10:23:01,108.989664,34.263836,0,0,13,2024-08-16 10:23:07,0,0,161
9095609812,三公司,四车队,5945,308路,112958,181172,100017942,21069,2024-08-16 10:23:12,2024-08-16 10:23:06,109.031352,34.274596,252,0.26,14,2024-08-16 10:23:12,0,0,42542
9095610077,五公司,四车队,9941,178路,17136,156138,100013446,30060,2024-08-16 10:23:12,2024-08-16 10:23:06,108.991784,34.169252,101,19.58,8,2024-08-16 10:23:12,0,0,60536
9095610276,七公司,三车队,8546,527路,65152,165588,100017564,23874,2024-08-16 10:23:12,2024-08-16 10:23:07,108.918688,34.240836,158,5.91,10,2024-08-16 10:23:12,0,0,8428
```