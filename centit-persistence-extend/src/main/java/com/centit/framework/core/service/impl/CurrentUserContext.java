package com.centit.framework.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.centit.framework.components.CodeRepositoryUtil;
import com.centit.framework.model.basedata.IUnitInfo;
import com.centit.framework.model.basedata.IUserRole;
import com.centit.framework.model.basedata.IUserUnit;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CurrentUserContext {
    public JSONObject userInfo;
    public String currentUnit;

    public CurrentUserContext(JSONObject userInfo, String currentUnit){
        this.userInfo = userInfo;
        this.currentUnit = StringUtils.isBlank(currentUnit)?
            userInfo.getString("primaryUnit"):currentUnit;
    }

    public IUnitInfo getPrimaryUnit(){
        return CodeRepositoryUtil
            .getUnitInfoByCode(userInfo.getString("primaryUnit"));
    }

    public List<? extends IUserUnit> listUserUnits(){
        return CodeRepositoryUtil
            .listUserUnits(userInfo.getString("userCode"));
    }

    public Map<String, List<IUserUnit>> getRankUnitsMap(){
        List<? extends IUserUnit> userUnits = listUserUnits();
        Map<String, List<IUserUnit>> rankUnits = new HashMap<>(5);
        for(IUserUnit uu : userUnits ){
            List<IUserUnit> rankUnit = rankUnits.get(uu.getUserRank());
            if(rankUnit==null){
                rankUnit = new ArrayList<>(4);
            }
            rankUnit.add(uu);
            rankUnits.put(uu.getUserRank(),rankUnit);
        }
        return rankUnits;
    }

    public Map<String, List<IUserUnit>> getStationUnitsMap(){
        List<? extends IUserUnit> userUnits = listUserUnits();
        Map<String, List<IUserUnit>> stationUnits = new HashMap<>(5);
        for(IUserUnit uu : userUnits ){
            List<IUserUnit> stationUnit = stationUnits.get(uu.getUserStation());
            if(stationUnit==null){
                stationUnit = new ArrayList<>(4);
            }
            stationUnit.add(uu);
            stationUnits.put(uu.getUserStation(),stationUnit);
        }
        return stationUnits;
    }

    public List<? extends IUserRole> listUserRoles() {
        return CodeRepositoryUtil.listUserRoles(userInfo.getString("userCode"));
    }

    public List<IUnitInfo> listSubUnits(){
        return CodeRepositoryUtil.getSubUnits(currentUnit);
    }

    public List<IUnitInfo> listAllSubUnits(){
        List<IUnitInfo> allSubUnits=CodeRepositoryUtil.getAllSubUnits(currentUnit);
        allSubUnits.add(CodeRepositoryUtil
            .getUnitInfoByCode(currentUnit));
        return allSubUnits;
    }
}
