package com.jcen.medpal.exception;

import com.jcen.medpal.common.BaseResponse;
import com.jcen.medpal.common.ErrorCode;
import com.jcen.medpal.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public BaseResponse<?> dataIntegrityViolationExceptionHandler(DataIntegrityViolationException e) {
        log.error("DataIntegrityViolationException", e);
        String message = e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : e.getMessage();
        if (message != null) {
            if (message.contains("user_account") || message.contains("companion_account")) {
                return ResultUtils.error(ErrorCode.PARAMS_ERROR, "账号已被注册");
            }
            if (message.contains("user_phone") || message.contains("companion_phone")) {
                return ResultUtils.error(ErrorCode.PARAMS_ERROR, "手机号已被注册");
            }
            if (message.contains("id_card")) {
                return ResultUtils.error(ErrorCode.PARAMS_ERROR, "身份证号已被注册");
            }
            if (message.contains("cannot be null")) {
                return ResultUtils.error(ErrorCode.PARAMS_ERROR, "提交的信息不完整，请检查后重试");
            }
        }
        return ResultUtils.error(ErrorCode.OPERATION_ERROR, "数据保存失败，请检查输入信息后重试");
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}
