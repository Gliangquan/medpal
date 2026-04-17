package com.jcen.medpal.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.medpal.annotation.AuthCheck;
import com.jcen.medpal.common.BaseResponse;
import com.jcen.medpal.common.DeleteRequest;
import com.jcen.medpal.common.ErrorCode;
import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.constant.UserConstant;
import com.jcen.medpal.exception.BusinessException;
import com.jcen.medpal.exception.ThrowUtils;
import com.jcen.medpal.model.dto.user.UserAddRequest;
import com.jcen.medpal.model.dto.user.UserBatchDeleteRequest;
import com.jcen.medpal.model.dto.user.UserBatchUpdateRequest;
import com.jcen.medpal.model.dto.user.UserExportRequest;
import com.jcen.medpal.model.dto.user.UserImportRequest;
import com.jcen.medpal.model.dto.user.UserLoginRequest;
import com.jcen.medpal.model.dto.user.UserQueryRequest;
import com.jcen.medpal.model.dto.user.UserRegisterRequest;
import com.jcen.medpal.model.dto.user.UserResetPasswordRequest;
import com.jcen.medpal.model.dto.user.UserCertificationAuditRequest;
import com.jcen.medpal.model.dto.user.UserUpdateMyRequest;
import com.jcen.medpal.model.dto.user.WeChatLoginRequest;
import com.jcen.medpal.model.dto.user.UserUpdateRequest;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.model.vo.LoginUserVO;
import com.jcen.medpal.model.vo.UserStatisticsVO;
import com.jcen.medpal.model.vo.UserVO;
import com.jcen.medpal.service.UserService;

import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.jcen.medpal.service.impl.UserServiceImpl.SALT;

/**
 * 用户接口
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    private static final Set<String> CERT_AUDIT_STATUS_SET = new HashSet<>(Arrays.asList("approved", "rejected"));

    @Resource
    private UserService userService;

    // region 登录相关

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String userPhone = userRegisterRequest.getUserPhone();
        String userRole = userRegisterRequest.getUserRole();
        String userName = userRegisterRequest.getUserName();
        String idCard = userRegisterRequest.getIdCard();
        
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, userPhone)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 如果是陪诊员，需要身份证
        if ("companion".equals(userRole) && StringUtils.isBlank(idCard)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "陪诊员需要提供身份证号");
        }
        
        long result = userService.userRegister(userAccount, userPassword, checkPassword, userPhone, userRole, userName, idCard);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String loginType = userLoginRequest.getLoginType();
        String userPassword = userLoginRequest.getUserPassword();
        
        if (StringUtils.isBlank(loginType) || StringUtils.isBlank(userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "登录类型和密码不能为空");
        }
        
        LoginUserVO loginUserVO;
        if ("phone".equals(loginType)) {
            // 手机号登录
            String userPhone = userLoginRequest.getUserPhone();
            if (StringUtils.isBlank(userPhone)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号不能为空");
            }
            loginUserVO = userService.userLoginByPhone(userPhone, userPassword, request);
        } else if ("account".equals(loginType)) {
            // 账号登录
            String userAccount = userLoginRequest.getUserAccount();
            if (StringUtils.isBlank(userAccount)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能为空");
            }
            loginUserVO = userService.userLoginByAccount(userAccount, userPassword, request);
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "登录类型不支持");
        }
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 手机号重置密码
     *
     * @param userResetPasswordRequest 重置密码请求
     * @return 是否成功
     */
    @PostMapping("/reset-password")
    public BaseResponse<Boolean> resetPassword(@RequestBody UserResetPasswordRequest userResetPasswordRequest) {
        if (userResetPasswordRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.resetPasswordByPhone(
                userResetPasswordRequest.getUserPhone(),
                userResetPasswordRequest.getNewPassword(),
                userResetPasswordRequest.getCheckPassword());
        return ResultUtils.success(result);
    }

    /**
     * 微信小程序登录
     *
     * @param weChatLoginRequest 微信登录请求
     * @param request
     * @return 登录用户信息
     */
    @PostMapping("/login/wechat")
    public BaseResponse<LoginUserVO> userLoginByWeChat(@RequestBody WeChatLoginRequest weChatLoginRequest,
            HttpServletRequest request) {
        if (weChatLoginRequest == null || StringUtils.isBlank(weChatLoginRequest.getCode())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "微信登录凭证不能为空");
        }
        LoginUserVO loginUserVO = userService.userLoginByWeChat(
                weChatLoginRequest.getCode(),
                weChatLoginRequest.getNickName(),
                weChatLoginRequest.getAvatarUrl(),
                request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    // endregion

    // region 增删改查

    /**
     * 创建用户
     *
     * @param userAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 默认密码 12345678
        String defaultPassword = "12345678";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + defaultPassword).getBytes());
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
            HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
        BaseResponse<User> response = getUserById(id, request);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest,
            HttpServletRequest request) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    /**
     * 分页获取用户封装列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest,
            HttpServletRequest request) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtils.success(userVOPage);
    }

    // endregion

    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest
     * @param request
     * @return
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
            HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        User currentUser = userService.getById(loginUser.getId());
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setUserPhone(StringUtils.trimToNull(user.getUserPhone()));
        user.setUserEmail(StringUtils.trimToNull(user.getUserEmail()));
        validateUniqueContact(loginUser.getId(), user.getUserPhone(), user.getUserEmail());
        validateUploadedFileUrl(user.getUserAvatar(), "头像");
        validateUploadedFileUrl(user.getIdCardFront(), "身份证正面");
        validateUploadedFileUrl(user.getIdCardBack(), "身份证反面");
        validateUploadedFileUrl(user.getQualificationCert(), "资质证明");
        boolean isCompanion = "companion".equals(loginUser.getUserRole());
        if (isCompanion) {
            boolean submitRealNameMaterial =
                    hasTextChanged(userUpdateMyRequest.getIdCard(), currentUser.getIdCard())
                            || hasTextChanged(userUpdateMyRequest.getIdCardFront(), currentUser.getIdCardFront())
                            || hasTextChanged(userUpdateMyRequest.getIdCardBack(), currentUser.getIdCardBack());
            boolean submitQualificationMaterial =
                    hasTextChanged(userUpdateMyRequest.getQualificationType(), currentUser.getQualificationType())
                            || hasTextChanged(userUpdateMyRequest.getQualificationCert(), currentUser.getQualificationCert())
                            || hasTextChanged(userUpdateMyRequest.getSpecialties(), currentUser.getSpecialties())
                            || hasTextChanged(userUpdateMyRequest.getServiceArea(), currentUser.getServiceArea())
                            || hasNumberChanged(userUpdateMyRequest.getWorkYears(), currentUser.getWorkYears());
            if (submitRealNameMaterial) {
                user.setRealNameStatus("pending");
            }
            if (submitQualificationMaterial) {
                user.setQualificationStatus("pending");
            }
        } else {
            // 普通患者不允许写入陪诊员认证字段
            user.setIdCard(null);
            user.setIdCardFront(null);
            user.setIdCardBack(null);
            user.setQualificationType(null);
            user.setQualificationCert(null);
            user.setWorkYears(null);
            user.setSpecialties(null);
            user.setServiceArea(null);
            user.setRealNameStatus(null);
            user.setQualificationStatus(null);
        }
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    private boolean hasTextChanged(String newValue, String oldValue) {
        if (StringUtils.isBlank(newValue)) {
            return false;
        }
        return !StringUtils.equals(StringUtils.trim(newValue), StringUtils.trim(oldValue));
    }

    private boolean hasNumberChanged(Integer newValue, Integer oldValue) {
        return newValue != null && !newValue.equals(oldValue);
    }

    private void validateUploadedFileUrl(String url, String fieldName) {
        if (StringUtils.isBlank(url)) {
            return;
        }
        String lower = url.trim().toLowerCase();
        boolean isTempPath = lower.startsWith("http://tmp/")
                || lower.startsWith("https://tmp/")
                || lower.startsWith("wxfile://")
                || lower.startsWith("file://")
                || lower.startsWith("blob:");
        if (isTempPath) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, fieldName + "未完成上传，请先上传文件");
        }
    }

    private void validateUniqueContact(Long currentUserId, String userPhone, String userEmail) {
        if (StringUtils.isNotBlank(userPhone)) {
            QueryWrapper<User> phoneWrapper = new QueryWrapper<>();
            phoneWrapper.eq("user_phone", userPhone).ne("id", currentUserId);
            long phoneCount = userService.count(phoneWrapper);
            if (phoneCount > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号已被其他账号使用");
            }
        }
        if (StringUtils.isNotBlank(userEmail)) {
            QueryWrapper<User> emailWrapper = new QueryWrapper<>();
            emailWrapper.eq("user_email", userEmail).ne("id", currentUserId);
            long emailCount = userService.count(emailWrapper);
            if (emailCount > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱已被其他账号使用");
            }
        }
    }

    // region 批量操作

    /**
     * 批量删除用户
     *
     * @param userBatchDeleteRequest
     * @param request
     * @return
     */
    @PostMapping("/batch-delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> batchDeleteUser(@RequestBody UserBatchDeleteRequest userBatchDeleteRequest,
            HttpServletRequest request) {
        if (userBatchDeleteRequest == null || userBatchDeleteRequest.getIds() == null || userBatchDeleteRequest.getIds().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.batchDeleteUser(userBatchDeleteRequest.getIds(), userBatchDeleteRequest.getSoftDelete());
        return ResultUtils.success(result);
    }

    /**
     * 批量更新用户
     *
     * @param userBatchUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/batch-update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> batchUpdateUser(@RequestBody UserBatchUpdateRequest userBatchUpdateRequest,
            HttpServletRequest request) {
        if (userBatchUpdateRequest == null || userBatchUpdateRequest.getIds() == null || userBatchUpdateRequest.getIds().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.batchUpdateUser(userBatchUpdateRequest);
        return ResultUtils.success(result);
    }

    // endregion

    // region 导入导出

    /**
     * 导出用户数据
     *
     * @param userExportRequest
     * @param request
     * @return
     */
    @PostMapping("/export")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> exportUser(@RequestBody UserExportRequest userExportRequest,
            HttpServletRequest request) {
        if (userExportRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String result = userService.exportUser(userExportRequest);
        return ResultUtils.success(result);
    }

    /**
     * 导入用户数据
     *
     * @param userImportRequest
     * @param request
     * @return
     */
    @PostMapping("/import")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> importUser(@RequestBody UserImportRequest userImportRequest,
            HttpServletRequest request) {
        if (userImportRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.importUser(userImportRequest);
        return ResultUtils.success(result);
    }

    // endregion

    // region 陪诊员相关

    /**
     * 获取陪诊员列表
     *
     * @param current 当前页码
     * @param size 每页大小
     * @return
     */
    @GetMapping("/companion/list")
    public BaseResponse<Page<UserVO>> listCompanions(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") long current,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") long size) {
        Page<User> page = new Page<>(current, size);
        Page<User> result = userService.lambdaQuery()
                .eq(User::getUserRole, "companion")
                .eq(User::getStatus, 1)
                .eq(User::getRealNameStatus, "approved")
                .eq(User::getQualificationStatus, "approved")
                .orderByDesc(User::getRating)
                .page(page);
        
        // 转换为 UserVO
        Page<UserVO> voPage = new Page<>(current, size);
        voPage.setTotal(result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            // 公开陪诊员列表不返回认证材料明细
            vo.setIdCard(null);
            vo.setIdCardFront(null);
            vo.setIdCardBack(null);
            vo.setQualificationCert(null);
            return vo;
        }).collect(java.util.stream.Collectors.toList()));
        
        return ResultUtils.success(voPage);
    }

    /**
     * 获取陪诊员详情
     *
     * @param id 陪诊员ID
     * @return
     */
    @GetMapping("/companion/{id}")
    public BaseResponse<UserVO> getCompanionDetail(@org.springframework.web.bind.annotation.PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null || !"companion".equals(user.getUserRole())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "陪诊员不存在");
        }
        if (!"approved".equals(user.getRealNameStatus()) || !"approved".equals(user.getQualificationStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "陪诊员尚未完成认证");
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        vo.setIdCard(null);
        vo.setIdCardFront(null);
        vo.setIdCardBack(null);
        vo.setQualificationCert(null);
        return ResultUtils.success(vo);
    }

    /**
     * 审核陪诊员实名认证 / 资质认证
     */
    @PostMapping("/companion/certification/audit")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> auditCompanionCertification(@RequestBody UserCertificationAuditRequest auditRequest,
            HttpServletRequest request) {
        if (auditRequest == null || auditRequest.getUserId() == null || auditRequest.getUserId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户参数错误");
        }

        String realNameStatus = auditRequest.getRealNameStatus();
        String qualificationStatus = auditRequest.getQualificationStatus();
        boolean hasRealNameAudit = StringUtils.isNotBlank(realNameStatus);
        boolean hasQualificationAudit = StringUtils.isNotBlank(qualificationStatus);
        if (!hasRealNameAudit && !hasQualificationAudit) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请至少审核一项认证");
        }
        if (hasRealNameAudit && !CERT_AUDIT_STATUS_SET.contains(realNameStatus)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "实名认证审核状态非法");
        }
        if (hasQualificationAudit && !CERT_AUDIT_STATUS_SET.contains(qualificationStatus)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "资质认证审核状态非法");
        }

        User companion = userService.getById(auditRequest.getUserId());
        if (companion == null || !"companion".equals(companion.getUserRole())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "陪诊员不存在");
        }

        if ("approved".equals(realNameStatus)) {
            if (StringUtils.isAnyBlank(companion.getIdCard(), companion.getIdCardFront(), companion.getIdCardBack())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "实名认证材料不完整，无法审核通过");
            }
        }
        if ("approved".equals(qualificationStatus)) {
            if (StringUtils.isAnyBlank(companion.getQualificationType(), companion.getQualificationCert())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "资质认证材料不完整，无法审核通过");
            }
        }

        User updateUser = new User();
        updateUser.setId(companion.getId());
        if (hasRealNameAudit) {
            updateUser.setRealNameStatus(realNameStatus);
        }
        if (hasQualificationAudit) {
            updateUser.setQualificationStatus(qualificationStatus);
        }
        boolean result = userService.updateById(updateUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion

    // region 统计

    /**
     * 获取用户统计信息
     *
     * @param request
     * @return
     */
    @GetMapping("/statistics")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserStatisticsVO> getUserStatistics(HttpServletRequest request) {
        UserStatisticsVO result = userService.getUserStatistics();
        return ResultUtils.success(result);
    }

    // endregion
}
