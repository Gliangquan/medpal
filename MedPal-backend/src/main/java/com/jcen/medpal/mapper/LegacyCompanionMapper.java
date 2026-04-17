package com.jcen.medpal.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface LegacyCompanionMapper {

    @Select("SELECT COUNT(*) FROM companion WHERE is_delete = 0")
    Long countAllCompanions();

    @Select("SELECT COUNT(*) FROM companion WHERE is_delete = 0 AND real_name_status = 'approved' AND qualification_status = 'approved'")
    Long countApprovedCompanions();

    @Select("SELECT id, companion_name AS companionName, companion_phone AS companionPhone FROM companion WHERE id = #{id} AND is_delete = 0 LIMIT 1")
    Map<String, Object> selectBasicById(@Param("id") Long id);
}
