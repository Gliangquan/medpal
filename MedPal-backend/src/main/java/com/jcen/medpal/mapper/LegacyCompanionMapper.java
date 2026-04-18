package com.jcen.medpal.mapper;

import com.jcen.medpal.model.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

@Mapper
public interface LegacyCompanionMapper {

    @Select("SELECT COUNT(*) FROM companion WHERE is_delete = 0")
    Long countAllCompanions();

    @Select("SELECT COUNT(*) FROM companion WHERE is_delete = 0 AND real_name_status = 'approved' AND qualification_status = 'approved'")
    Long countApprovedCompanions();

    @Select("SELECT id, companion_name AS companionName, companion_phone AS companionPhone FROM companion WHERE id = #{id} AND is_delete = 0 LIMIT 1")
    Map<String, Object> selectBasicById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM companion WHERE id = #{id} LIMIT 1")
    Long countById(@Param("id") Long id);

    @Insert("INSERT INTO companion (id, companion_account, companion_password, companion_name, companion_avatar, companion_phone, id_card, id_card_front, id_card_back, qualification_cert, qualification_type, work_years, specialties, service_area, profile_photo, real_name_status, qualification_status, rating, service_count, total_income, status, mp_open_id, create_time, update_time, is_delete) " +
            "VALUES (#{id}, #{userAccount}, #{userPassword}, #{userName}, #{userAvatar}, #{userPhone}, #{idCard}, #{idCardFront}, #{idCardBack}, #{qualificationCert}, #{qualificationType}, #{workYears}, #{specialties}, #{serviceArea}, #{userAvatar}, #{realNameStatus}, #{qualificationStatus}, #{rating}, #{serviceCount}, #{totalIncome}, #{status}, #{mpOpenId}, #{createTime}, #{updateTime}, 0)")
    int insertFromUser(User user);

    @Update("UPDATE companion SET companion_account = #{userAccount}, companion_password = #{userPassword}, companion_name = #{userName}, companion_avatar = #{userAvatar}, companion_phone = #{userPhone}, id_card = #{idCard}, id_card_front = #{idCardFront}, id_card_back = #{idCardBack}, qualification_cert = #{qualificationCert}, qualification_type = #{qualificationType}, work_years = #{workYears}, specialties = #{specialties}, service_area = #{serviceArea}, profile_photo = #{userAvatar}, real_name_status = #{realNameStatus}, qualification_status = #{qualificationStatus}, rating = #{rating}, service_count = #{serviceCount}, total_income = #{totalIncome}, status = #{status}, mp_open_id = #{mpOpenId}, update_time = #{updateTime}, is_delete = 0 WHERE id = #{id}")
    int updateFromUser(User user);
}
