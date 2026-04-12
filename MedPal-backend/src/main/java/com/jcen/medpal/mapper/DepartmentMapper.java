package com.jcen.medpal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcen.medpal.model.entity.Department;
import org.apache.ibatis.annotations.Mapper;

/**
 * 科室 Mapper
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}
