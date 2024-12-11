package com.example.clickhouseclientdemo.service;

import com.example.clickhouseclientdemo.entity.PflowSuccessDTO;
import com.example.clickhouseclientdemo.utils.ProjectHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
public class GpsServiceTest {
    @Resource private GpsService gpsService;

    @Test
    void insertMapStr() {
        List<Map<String, Object>>  list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("id",100);
        map.put("name","麻子2");
        map.put("dt","2024-08-22 14:23:56");
        list.add(map);
        var csvString = ProjectHelper.listMapToCSVByCSVPrinter(list);
        gpsService.insertCSVString(csvString);
    }

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


    @Test
    void insertPflowSuccessDTO() {
        PflowSuccessDTO pflowSuccessDTO = new PflowSuccessDTO();

        pflowSuccessDTO.setArrivalUuid("5");
        pflowSuccessDTO.setBusId(143152);
        pflowSuccessDTO.setEmpId(129286);
        pflowSuccessDTO.setEmpName("马峰");
        pflowSuccessDTO.setCardSequence("20241209005152426228");
        pflowSuccessDTO.setCardNumber("7100010015171845");
        pflowSuccessDTO.setTransactionType("8451");
        pflowSuccessDTO.setTicketType("00");
        pflowSuccessDTO.setCardMainType("03");
        pflowSuccessDTO.setCardSubType("0100");
        pflowSuccessDTO.setCardBalance("3890");
        pflowSuccessDTO.setDiscountType("00");
        pflowSuccessDTO.setTicketPrice("200");
        pflowSuccessDTO.setPayPrice("100");
        pflowSuccessDTO.setYktCompanyCode("71000111");
        pflowSuccessDTO.setYktRouteCode("06110");
        pflowSuccessDTO.setYktBusCode("191672");
        pflowSuccessDTO.setYktEmpCode("01002598");
        pflowSuccessDTO.setCmpId(541);
        pflowSuccessDTO.setCmpName("四公司");
        pflowSuccessDTO.setFleetId(12541);
        pflowSuccessDTO.setFleetName("公交四公司一车队");
        pflowSuccessDTO.setRouteId(11343);
        pflowSuccessDTO.setRouteName("611路");
        pflowSuccessDTO.setMatchFlag((byte) 18);
        pflowSuccessDTO.setServiceId(51683);
        pflowSuccessDTO.setRoadBillCode("241129120917191672");
        pflowSuccessDTO.setStationId(47257);
        pflowSuccessDTO.setStationName("土门市场");
        pflowSuccessDTO.setResendFlag((byte) 0);
        pflowSuccessDTO.setFileName("20241209000000000000000000158776.MODIFIED");
        pflowSuccessDTO.setYktUpTime(LocalDateTime.parse("20241129202123", DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        pflowSuccessDTO.setAdTime(LocalDateTime.parse("20241129201724", DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        pflowSuccessDTO.setPTime(LocalDate.parse("20241129", DateTimeFormatter.ofPattern("yyyyMMdd")));

        var list = List.of(pflowSuccessDTO);
        gpsService.insertPflowSuccessDTO(list);
    }

}