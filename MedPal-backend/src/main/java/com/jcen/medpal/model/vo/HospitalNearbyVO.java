package com.jcen.medpal.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 附近医院VO，包含距离信息
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Data
public class HospitalNearbyVO implements Serializable {

    private Long id;

    private String hospitalName;

    private String hospitalLevel;

    private String address;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String phone;

    private String introduction;

    private Integer status;

    private LocalDateTime createTime;

    /**
     * 距离（单位：公里）
     */
    private Double distance;

    private static final long serialVersionUID = 1L;
}
