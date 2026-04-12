package com.jcen.medpal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcen.medpal.mapper.DoctorMapper;
import com.jcen.medpal.model.entity.Doctor;
import com.jcen.medpal.service.DoctorService;
import org.springframework.stereotype.Service;

/**
 * 医生 Service 实现
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Service
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {
}
