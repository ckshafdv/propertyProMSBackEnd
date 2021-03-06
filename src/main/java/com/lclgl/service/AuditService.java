package com.lclgl.service;

import com.lclgl.dao.AuditMapper;
import com.lclgl.dao.ProInfoMapper;
import com.lclgl.pojo.ProInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cksh
 * @create 2020-12-17 0:04
 */
@Service
public class AuditService {

    @Autowired
    private AuditMapper auditMapper;
    @Autowired
    private ProInfoMapper proInfoMapper;
    @Autowired
    private StaffService staffService;
    @Autowired
    private FileService fileService;

    public Map<String, Object> processAudit(int auditPerson, int auditId, String proName, int staffId, String auditFile, String operation, String suggestion) {
        HashMap<String, Object> map = new HashMap<>();

        HashMap<String, Object> updateInfo = new HashMap<>();
        updateInfo.put("auditId", auditId);
        String auditStatus = null;
        String type = staffService.getStaffType(staffId);
        if ("pass".equals(operation)) {
            auditStatus = "审核通过";
            File file = new File("项目列表/" + proName + "/" + type);
            if (!file.exists()) file.mkdirs();
            String oldPath = "待审核文件/" + proName + "/" + type + "/" + auditFile;
            String newPath = "项目列表/" + proName + "/" + type + "/" + auditFile;
            fileService.moveFile(oldPath, newPath);
        } else if ("acceptAudit".equals(operation)) {
            auditStatus = "审核中";
        } else if ("reject".equals(operation)) {
            auditStatus = "审核未通过";
            File file = new File("回收站/" + proName + "/" + type);
            if (!file.exists()) file.mkdirs();
            String oldPath = "待审核文件/" + proName + "/" + type + "/" + auditFile;
            String newPath = "回收站/" + proName + "/" + type + "/" + auditFile;
            fileService.moveFile(oldPath, newPath);
        }
        updateInfo.put("auditStatus", auditStatus);
        updateInfo.put("suggestion", suggestion);
        int updateAuditStatus = auditMapper.updateAuditStatus(updateInfo);

        if (updateAuditStatus != -1) {
            map.put("status", 1);
            map.put("msg", "修改成功！");
        } else {
            map.put("status", -1);
        }

        return map;
    }

    public Map<String, Object> getAuditInfo(Object staffId) {
        HashMap<String, Object> map = new HashMap<>();
        if (staffId == null) {
            map.put("status", -1);
            map.put("msg", "你没有权限！");
        } else {
            map.put("status", 1);
            map.put("msg", "获取成功！");
            map.put("auditInfo", auditMapper.getAuditInfo((int) staffId));
        }
        return map;
    }

    public Map<String, Object> getUnfinishedPros(int staffId) {
        HashMap<String, Object> map = new HashMap<>();

        ArrayList<ProInfo> proInfos = new ArrayList<>();

        List<Integer> unfinishedProIds = auditMapper.getUnfinishedProIds(staffId);
        for (int proId : unfinishedProIds) {
            ProInfo proInfo = proInfoMapper.getProById(proId);
            proInfos.add(proInfo);
        }

        map.put("pros", proInfos);

        return map;
    }

}
