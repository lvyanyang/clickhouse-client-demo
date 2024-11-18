package com.example.clickhouseclientdemo.service;

import com.example.clickhouseclientdemo.utils.ProjectHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class GpsServiceTest {
    @Resource private GpsService gpsService;

    @Test
    void insertCSV() {
        gpsService.insertCSV();
    }

    @Test
    void insertList() {
        var list = ProjectHelper.readCSVToList();
        gpsService.insertList(list);
    }

    @Test
    void insertMap() {
        var listMap = ProjectHelper.readCSVToListMap();
        gpsService.insertListMap(listMap);
    }

    @Test
    void query() {
        gpsService.query();
    }

    @Test
    void queryAsString() {
        gpsService.queryAsString();
    }

    @Test
    void queryAsCSV() {
        gpsService.queryAsCSV();
    }

    @Test
    void queryListMap() {
        gpsService.queryListMap();
    }

    @Test
    void queryCustomMap() {
        gpsService.queryCustomMap();
    }

    @Test
    void queryList() {
        gpsService.queryList();
    }

    @Test
    void queryAsJsonToObject() {
        gpsService.queryAsJsonToObject();
    }
}