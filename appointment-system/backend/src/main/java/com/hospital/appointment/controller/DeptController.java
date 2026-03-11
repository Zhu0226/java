package com.hospital.appointment.controller;

import com.hospital.appointment.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dept")
public class DeptController {

    /**
     * 获取科室列表 (为了快速跑通流程，这里先提供固定数据)
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> listDepts() {
        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> dept1 = new HashMap<>();
        dept1.put("id", 1);
        dept1.put("deptName", "内科");

        Map<String, Object> dept2 = new HashMap<>();
        dept2.put("id", 2);
        dept2.put("deptName", "外科");

        Map<String, Object> dept3 = new HashMap<>();
        dept3.put("id", 3);
        dept3.put("deptName", "儿科");

        list.add(dept1);
        list.add(dept2);
        list.add(dept3);

        return Result.success(list);
    }
}