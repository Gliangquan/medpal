package com.jcen.medpal.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;

/**
 * 文件上传业务类型枚举
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
public enum FileUploadBizEnum {

    USER_AVATAR("用户头像", "user_avatar"),
    COMPANION_AVATAR("陪诊员头像", "companion_avatar"),
    HOSPITAL_IMAGE("医院图片", "hospital_image"),
    ID_CARD("身份证图片", "id_card"),
    QUALIFICATION("资质证明", "qualification"),
    MEDICAL_RECORD("病历附件", "medical_record"),
    ORDER_ATTACHMENT("订单附件", "order_attachment");

    private final String text;

    private final String value;

    FileUploadBizEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static FileUploadBizEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (FileUploadBizEnum anEnum : FileUploadBizEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
