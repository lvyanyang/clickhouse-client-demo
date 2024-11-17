package com.example.clickhouseclientdemo.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CK数据库连接配置
 */
@Data
@ConfigurationProperties(prefix = "ch")
public class ClickHouseProperties {
    /** 服务器地址,例如:http:// localhost:8123 或者 https:// localhost:8443 */
    private String endpoint;
    /** 账号 */
    private String username;
    /** 密码 */
    private String password;
    /** 数据库名称 */
    private String database;
    /** 时区 */
    private String timezone = "Asia/Shanghai";
    /** 是否启用http压缩 */
    private Boolean httpCompression = true;
    /** 是否启用异步写入 */
    private Integer asyncInsert = 1;
}
