package com.jcen.medpal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcen.medpal.mapper.DepartmentMapper;
import com.jcen.medpal.model.entity.Department;
import com.jcen.medpal.service.DepartmentService;
import org.springframework.stereotype.Service;

/**
 * 科室 Service 实现
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {
}
