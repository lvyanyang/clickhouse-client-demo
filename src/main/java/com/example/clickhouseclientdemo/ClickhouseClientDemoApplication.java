package com.example.clickhouseclientdemo;

import com.clickhouse.client.api.Client;
import com.example.clickhouseclientdemo.utils.ClickHouseProperties;
import jakarta.annotation.Resource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.time.temporal.ChronoUnit;

@SpringBootApplication
@EnableConfigurationProperties({ClickHouseProperties.class})
public class ClickhouseClientDemoApplication {
	@Resource private ClickHouseProperties properties;

	/** 构建CK数据库客户端对象 */
    @Bean
    public Client projectClient() {
        String endpoint = properties.getEndpoint();
        String username = properties.getUsername();
        String password = properties.getPassword();
        String database = properties.getDatabase();
        // String timeZone = properties.getTimezone();
        boolean httpCompression = properties.getHttpCompression();
        int asyncInsert = properties.getAsyncInsert();
        return new Client.Builder()
                .addEndpoint(endpoint)
                .setUsername(username)
                .setPassword(password)
                .setDefaultDatabase(database)
                // sets the maximum number of connections to the server at a time
                // this is important for services handling many concurrent requests to ClickHouse
                .setMaxConnections(1000)
                .setLZ4UncompressedBufferSize(1058576)
                .setSocketRcvbuf(500_000)
                .setSocketTcpNodelay(true)
                .setSocketSndbuf(500_000)
                .setClientNetworkBufferSize(500_000)
                .useHttpCompression(httpCompression)
                .setExecutionTimeout(10, ChronoUnit.SECONDS)
                // .useServerTimeZone(false)
                // .setServerTimeZone("Asia/Shanghai")
                // .useTimeZone("Asia/Shanghai")
                .serverSetting("async_insert", String.valueOf(asyncInsert))
                .allowBinaryReaderToReuseBuffers(true) // using buffer pool for binary reader
                .build();
    }

	public static void main(String[] args) {
		SpringApplication.run(ClickhouseClientDemoApplication.class, args);
	}

}
