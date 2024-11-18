package com.example.clickhouseclientdemo.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * GPS实体对象
 */
@Data
public class GpsLog {
    /** 主键 */
    @JsonAlias({"gps_id", "gpsId"})
    private BigInteger gpsId;

    /** 公司名称 */
    @JsonAlias({"cmp_name", "cmpName"})
    private String cmpName;

    /** 车队名称 */
    @JsonAlias({"cd_name", "cdName"})
    private String cdName;

    /** 线路ID */
    @JsonAlias({"route_id", "routeId"})
    private BigInteger routeId;

    /** 线路名称 */
    @JsonAlias({"route_name", "routeName"})
    private String routeName;

    /** 车辆ID */
    @JsonAlias({"bus_id", "busId"})
    private BigInteger busId;

    /** 车辆编号 */
    @JsonAlias({"bus_code", "busCode"})
    private String busCode;

    /** 终端ID */
    @JsonAlias({"terminal_id", "terminalId"})
    private BigInteger terminalId;

    /** 服务ID */
    @JsonAlias({"terminal_id", "terminalId"})
    private BigInteger serviceId;

    /** 服务器接收时间 */
    @JsonAlias({"rev_time", "revTime"})
    private LocalDateTime revTime;

    /** 终端时间 */
    @JsonAlias({"terminal_time", "terminalTime"})
    private LocalDateTime terminalTime;

    /** 经度 */
    private String longitude;

    /** 纬度 */
    private String latitude;

    /** 方向 */
    private String direction;

    /** 速度 */
    private BigDecimal speed;

    /** 卫星数 */
    private Short secondaryplanet;

    /** 创建时间 */
    @JsonAlias({"created_time", "createdTime"})
    private LocalDateTime createdTime;

    /** 发送标志 */
    @JsonAlias({"resend_flag", "resendFlag"})
    private Integer resendFlag;

    /** 有效标志 */
    @JsonAlias({"valid_flag", "validFlag"})
    private Integer validFlag;

    /** 公里数 */
    private Long milestotal;
}

