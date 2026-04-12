package com.jcen.medpal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcen.medpal.model.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医生 Mapper
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {
}
