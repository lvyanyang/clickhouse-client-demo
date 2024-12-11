package com.example.clickhouseclientdemo.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PflowSuccessDTO {
    //通信报站生成的唯一id
    @JsonAlias({"arrival_uuid", "arrivalUuid"})
    private String arrivalUuid;

    //流水号 20位平台流水号
    @JsonAlias({"card_sequence", "cardSequence"})
    private String cardSequence;

    //卡号 二维码和住建部卡16位 交通部卡19位
    @JsonAlias({"card_number", "cardNumber"})
    private String cardNumber;

    //交易类型 8451：卡消费 1105：二维码消费
    @JsonAlias({"transaction_type", "transactionType"})
    private String transactionType;

    //票制类型 00：一票制 01：复合消费【分段计费】
    @JsonAlias({"card_sequence", "cardSequence"})
    private String ticketType;

    //卡型 00:自有码 01：微信乘车码 02：支付宝乘车码 03：住建部卡 04:交通部卡
    @JsonAlias({"cardMain_type", "cardMainType"})
    private String cardMainType;

    //卡类型
    @JsonAlias({"cardSub_type", "cardSubType"})
    private String cardSubType;

    //交易前卡余额 单位：分
    @JsonAlias({"card_balance", "cardBalance"})
    private String cardBalance;

    //优惠类型
    @JsonAlias({"discount_type", "discountType"})
    private String discountType;

    //应收金额 单位：分
    @JsonAlias({"ticket_price", "ticketPrice"})
    private String ticketPrice;

    //交易金额 单位：分
    @JsonAlias({"pay_price", "payPrice"})
    private String payPrice;

    //分公司 8位分公司编号
    @JsonAlias({"ykt_company_code", "yktCompanyCode"})
    private String yktCompanyCode;

    //线路号 6位线路编号
    @JsonAlias({"ykt_route_code", "yktRouteCode"})
    private String yktRouteCode;

    //车号 8位车辆号
    @JsonAlias({"ykt_bus_code", "yktBusCode"})
    private String yktBusCode;

    //司售卡号 8位司机编号
    @JsonAlias({"ykt_emp_code", "yktEmpCode"})
    private String yktEmpCode;

    //上车时间 与交易时间重复，建议直接使用此字段上送交易时间格式yyyyMMddHHmmss设备受理卡或码的时间
    @JsonAlias({"ykt_up_time", "yktUpTime"})
    private LocalDateTime yktUpTime;

    //机构id
    @JsonAlias({"cmp_id", "cmpId"})
    private int cmpId;

    //机构名称
    @JsonAlias({"cmp_name", "cmpName"})
    private String cmpName;

    //车队id
    @JsonAlias({"fleet_id", "fleetId"})
    private int fleetId;

    //车队名称
    @JsonAlias({"fleet_name", "fleetName"})
    private String fleetName;

    //线路id
    @JsonAlias({"route_id", "routeId"})
    private int routeId;

    //线路名称
    @JsonAlias({"route_name", "routeName"})
    private String routeName;

    //服务id
    @JsonAlias({"service_id", "serviceId"})
    private int serviceId;

    //路单id
    @JsonAlias({"roadBill_code", "roadBillCode"})
    private String roadBillCode;

    //站点id
    @JsonAlias({"station_id", "stationId"})
    private int stationId;

    //站点名称
    @JsonAlias({"station_name", "stationName"})
    private String stationName;

    @JsonAlias({"emp_id", "empId"})
    private int empId;

    @JsonAlias({"emp_name", "empName"})
    private String empName;

    //补发标识 0：正常 1补发
    @JsonAlias({"resend_flag", "resendFlag"})
    private int resendFlag;

    //车辆id
    @JsonAlias({"bus_id", "busId"})
    private int busId;

    @JsonAlias({"ad_time", "adTime"})
    private LocalDateTime adTime;

    //分区时间
    @JsonAlias({"ptime", "pTime"})
    private LocalDate pTime;

    @JsonAlias({"ctime", "cTime"})
    private LocalDateTime cTime;

    //原始文件名
    @JsonAlias({"file_name", "fileName"})
    private String fileName;

    //匹配结果类型
    /**
     1:只有一个报站
     3:刷卡时间和报站时间一模一样
     4:报站为终点站
     5:两个报站取最小的匹配
     6:两个报站取最大的匹配
     11:最终匹配1小时之内的数据
     12：最终匹配缓存命中
     13：最终匹配缓存未命中，查询数据库匹配
     */
    @JsonAlias({"match_flag", "matchFlag"})
    private int matchFlag;
}
