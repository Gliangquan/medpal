package com.jcen.medpal.model.dto;

import java.io.Serializable;
import lombok.Data;

/**
 * 医院搜索请求
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Data
public class HospitalSearchRequest implements Serializable {

    /**
     * 搜索关键词
     */
    private String keyword;

    private static final long serialVersionUID = 1L;
}
