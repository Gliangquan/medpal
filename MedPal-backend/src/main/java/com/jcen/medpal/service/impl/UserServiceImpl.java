package com.jcen.medpal.service.impl;

import static com.jcen.medpal.constant.UserConstant.USER_LOGIN_STATE;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jcen.medpal.common.ErrorCode;
import com.jcen.medpal.constant.CommonConstant;
import com.jcen.medpal.exception.BusinessException;
import com.jcen.medpal.mapper.LegacyCompanionMapper;
import com.jcen.medpal.mapper.UserMapper;
import com.jcen.medpal.model.dto.user.UserQueryRequest;
import com.jcen.medpal.model.dto.user.WeChatLoginRequest;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.model.enums.UserRoleEnum;
import com.jcen.medpal.model.vo.LoginUserVO;
import com.jcen.medpal.model.vo.UserStatisticsVO;
import com.jcen.medpal.model.vo.UserVO;
import com.jcen.medpal.service.UserService;
import com.jcen.medpal.service.WeChatService;
import com.jcen.medpal.utils.JwtTokenUtils;
import com.jcen.medpal.utils.SqlUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * 用户服务实现
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Service
@Slf4j
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {

    public static final String SALT = "jcenLeung";

    @Resource
    private JwtTokenUtils jwtTokenUtils;

    @Resource
    private WeChatService weChatService;

    @Resource
    private LegacyCompanionMapper legacyCompanionMapper;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String userPhone, String userRole, String userName, String idCard) {
        userAccount = StringUtils.trimToEmpty(userAccount);
        userPassword = StringUtils.trimToEmpty(userPassword);
        checkPassword = StringUtils.trimToEmpty(checkPassword);
        userPhone = StringUtils.trimToEmpty(userPhone);
        userRole = StringUtils.trimToNull(userRole);
        userName = StringUtils.trimToNull(userName);
        idCard = StringUtils.trimToNull(idCard);

        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, userPhone)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请填写完整注册信息");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度不能少于 4 位");
        }
        if (userPassword.length() < 6 || checkPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能少于 6 位");
        }
        if (!userPhone.matches("^1\\d{10}$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入正确的手机号");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        if (StringUtils.isBlank(userRole)) {
            userRole = "patient";
        }
        if ("companion".equals(userRole) && StringUtils.isBlank(idCard)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "陪诊员需要填写身份证号");
        }

        synchronized (userAccount.intern()) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_account", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已被注册");
            }

            QueryWrapper<User> phoneWrapper = new QueryWrapper<>();
            phoneWrapper.eq("user_phone", userPhone);
            long phoneCount = this.baseMapper.selectCount(phoneWrapper);
            if (phoneCount > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号已被注册");
            }

            if ("companion".equals(userRole)) {
                QueryWrapper<User> idCardWrapper = new QueryWrapper<>();
                idCardWrapper.eq("id_card", idCard);
                long idCardCount = this.baseMapper.selectCount(idCardWrapper);
                if (idCardCount > 0) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "身份证号已被注册");
                }
            }

            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setUserPhone(userPhone);
            user.setUserRole(userRole);
            user.setUserName(StringUtils.defaultIfBlank(userName, userAccount));
            user.setStatus(1);
            if ("companion".equals(userRole)) {
                user.setIdCard(idCard);
                user.setRealNameStatus("not_submitted");
                user.setQualificationStatus("not_submitted");
            }
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，请稍后重试");
            }
            User savedUser = this.getById(user.getId());
            ensureLegacyCompanion(savedUser != null ? savedUser : user);
            return user.getId();
        }
    }

    @Override
    @Deprecated
    public LoginUserVO userLogin(String userAccount, String userPassword, String userPhone, HttpServletRequest request) {
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "请使用账号登录或手机号登录");
    }

    @Override
    public LoginUserVO userLoginByAccount(String userAccount, String userPassword, HttpServletRequest request) {
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号格式错误");
        }
        if (userPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_password", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        LoginUserVO loginUserVO = this.getLoginUserVO(user);
        String token = jwtTokenUtils.generateToken(user.getId(), user.getUserAccount());
        loginUserVO.setToken(token);
        return loginUserVO;
    }

    @Override
    public LoginUserVO userLoginByPhone(String userPhone, String userPassword, HttpServletRequest request) {
        if (StringUtils.isAnyBlank(userPhone, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userPhone.length() != 11) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式错误");
        }
        if (userPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_phone", userPhone);
        queryWrapper.eq("user_password", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("user login failed, userPhone cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号或密码错误");
        }
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        LoginUserVO loginUserVO = this.getLoginUserVO(user);
        String token = jwtTokenUtils.generateToken(user.getId(), user.getUserAccount());
        loginUserVO.setToken(token);
        return loginUserVO;
    }

    @Override
    public LoginUserVO userLoginByWeChat(String code, String nickName, String avatarUrl, HttpServletRequest request) {
        if (StringUtils.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "微信登录凭证不能为空");
        }
        Map<String, Object> sessionInfo = weChatService.getSessionKeyOrOpenid(code);
        String openId = sessionInfo == null ? null : (String) sessionInfo.get("openid");
        if (StringUtils.isBlank(openId)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取微信用户信息失败");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mp_open_id", openId);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            user = new User();
            user.setMpOpenId(openId);
            user.setUserAccount("wx_" + openId.substring(0, Math.min(12, openId.length())));
            user.setUserName(StringUtils.defaultIfBlank(nickName, "微信用户"));
            user.setUserAvatar(avatarUrl);
            user.setUserRole("patient");
            user.setUserPassword(DigestUtils.md5DigestAsHex((SALT + openId).getBytes()));
            user.setStatus(1);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建用户失败");
            }
        }
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        LoginUserVO loginUserVO = this.getLoginUserVO(user);
        String token = jwtTokenUtils.generateToken(user.getId(), user.getUserAccount());
        loginUserVO.setToken(token);
        return loginUserVO;
    }

    @Override
    public boolean resetPasswordByPhone(String userPhone, String newPassword, String checkPassword) {
        if (StringUtils.isAnyBlank(userPhone, newPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号和新密码不能为空");
        }
        if (userPhone.length() != 11) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式错误");
        }
        if (newPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码长度不能小于 6 位");
        }
        if (!newPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的新密码不一致");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_phone", userPhone);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "该手机号未注册");
        }
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());
        user.setUserPassword(encryptPassword);
        boolean result = this.updateById(user);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "密码重置失败");
        }
        return true;
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword, String checkPassword) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        if (StringUtils.isBlank(oldPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能为空");
        }
        if (StringUtils.isBlank(newPassword) || newPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码长度不能小于 6 位");
        }
        if (!StringUtils.equals(newPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的新密码不一致");
        }
        if (StringUtils.equals(oldPassword, newPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码不能与原密码相同");
        }
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        String encryptOldPassword = DigestUtils.md5DigestAsHex((SALT + oldPassword).getBytes());
        if (!StringUtils.equals(user.getUserPassword(), encryptOldPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "原密码错误");
        }
        String encryptNewPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());
        user.setUserPassword(encryptNewPassword);
        boolean result = this.updateById(user);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "修改密码失败");
        }
        return true;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        User loginUser = getLoginUserPermitNull(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return loginUser;
    }

    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj instanceof User) {
            User sessionUser = (User) userObj;
            if (sessionUser.getId() != null) {
                User currentUser = this.getById(sessionUser.getId());
                if (currentUser != null) {
                    return currentUser;
                }
            }
        }

        String authorization = request.getHeader("Authorization");
        if (StringUtils.isBlank(authorization) || !StringUtils.startsWithIgnoreCase(authorization, "Bearer ")) {
            return null;
        }
        String token = StringUtils.trimToNull(authorization.substring(7));
        if (StringUtils.isBlank(token) || !Boolean.TRUE.equals(jwtTokenUtils.validateToken(token))) {
            return null;
        }
        Long userId = jwtTokenUtils.getUserIdFromToken(token);
        if (userId == null) {
            return null;
        }
        User currentUser = this.getById(userId);
        if (currentUser != null) {
            request.getSession().setAttribute(USER_LOGIN_STATE, currentUser);
        }
        return currentUser;
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String userPhone = userQueryRequest.getUserPhone();
        String realNameStatus = userQueryRequest.getRealNameStatus();
        String qualificationStatus = userQueryRequest.getQualificationStatus();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        String keyword = userQueryRequest.getKeyword();

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "user_role", userRole);
        queryWrapper.eq(StringUtils.isNotBlank(realNameStatus), "real_name_status", realNameStatus);
        queryWrapper.eq(StringUtils.isNotBlank(qualificationStatus), "qualification_status", qualificationStatus);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "user_profile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "user_name", userName);
        queryWrapper.like(StringUtils.isNotBlank(userAccount), "user_account", userAccount);
        queryWrapper.like(StringUtils.isNotBlank(userPhone), "user_phone", userPhone);
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like("user_name", keyword)
                    .or()
                    .like("user_account", keyword)
                    .or()
                    .like("user_phone", keyword));
        }
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public void ensureLegacyCompanion(User user) {
        if (user == null || user.getId() == null || !"companion".equals(user.getUserRole())) {
            return;
        }
        User legacyUser = new User();
        BeanUtils.copyProperties(user, legacyUser);
        legacyUser.setUserName(StringUtils.defaultIfBlank(user.getUserName(), user.getUserAccount()));
        legacyUser.setRealNameStatus(StringUtils.defaultIfBlank(user.getRealNameStatus(), "not_submitted"));
        legacyUser.setQualificationStatus(StringUtils.defaultIfBlank(user.getQualificationStatus(), "not_submitted"));
        legacyUser.setRating(user.getRating() == null ? 0D : user.getRating());
        legacyUser.setServiceCount(user.getServiceCount() == null ? 0 : user.getServiceCount());
        legacyUser.setTotalIncome(user.getTotalIncome() == null ? 0D : user.getTotalIncome());
        legacyUser.setStatus(user.getStatus() == null ? 1 : user.getStatus());
        Date now = new Date();
        legacyUser.setCreateTime(user.getCreateTime() == null ? now : user.getCreateTime());
        legacyUser.setUpdateTime(user.getUpdateTime() == null ? now : user.getUpdateTime());
        Long count = legacyCompanionMapper.countById(user.getId());
        if (count != null && count > 0) {
            legacyCompanionMapper.updateFromUser(legacyUser);
        } else {
            legacyCompanionMapper.insertFromUser(legacyUser);
        }
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public int batchDeleteUser(List<Long> ids, Boolean softDelete) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        if (softDelete == null || softDelete) {
            return this.baseMapper.update(null, new UpdateWrapper<User>().in("id", ids).set("is_delete", 1));
        } else {
            return this.baseMapper.deleteBatchIds(ids);
        }
    }

    @Override
    public int batchUpdateUser(com.jcen.medpal.model.dto.user.UserBatchUpdateRequest userBatchUpdateRequest) {
        if (userBatchUpdateRequest == null || CollUtil.isEmpty(userBatchUpdateRequest.getIds())) {
            return 0;
        }
        User user = new User();
        if (StringUtils.isNotBlank(userBatchUpdateRequest.getUserName())) {
            user.setUserName(userBatchUpdateRequest.getUserName());
        }
        if (StringUtils.isNotBlank(userBatchUpdateRequest.getUserRole())) {
            user.setUserRole(userBatchUpdateRequest.getUserRole());
        }
        if (userBatchUpdateRequest.getStatus() != null) {
            user.setStatus(userBatchUpdateRequest.getStatus());
        }
        if (StringUtils.isNotBlank(userBatchUpdateRequest.getUserProfile())) {
            user.setUserProfile(userBatchUpdateRequest.getUserProfile());
        }
        return this.baseMapper.update(user, new QueryWrapper<User>().in("id", userBatchUpdateRequest.getIds()));
    }

    @Override
    public String exportUser(com.jcen.medpal.model.dto.user.UserExportRequest userExportRequest) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(userExportRequest.getUserName())) {
            queryWrapper.like("user_name", userExportRequest.getUserName());
        }
        if (StringUtils.isNotBlank(userExportRequest.getUserAccount())) {
            queryWrapper.like("user_account", userExportRequest.getUserAccount());
        }
        if (StringUtils.isNotBlank(userExportRequest.getUserRole())) {
            queryWrapper.eq("user_role", userExportRequest.getUserRole());
        }
        if (userExportRequest.getStatus() != null) {
            queryWrapper.eq("status", userExportRequest.getStatus());
        }
        this.list(queryWrapper);
        return "export_" + System.currentTimeMillis() + ".xlsx";
    }

    @Override
    public int importUser(com.jcen.medpal.model.dto.user.UserImportRequest userImportRequest) {
        return 0;
    }

    @Override
    public UserStatisticsVO getUserStatistics() {
        UserStatisticsVO statistics = new UserStatisticsVO();
        statistics.setTotalUsers(this.count());

        QueryWrapper<User> adminQuery = new QueryWrapper<User>().eq("user_role", "admin");
        statistics.setAdminCount(this.baseMapper.selectCount(adminQuery));

        QueryWrapper<User> userQuery = new QueryWrapper<User>().eq("user_role", "user");
        statistics.setUserCount(this.baseMapper.selectCount(userQuery));

        QueryWrapper<User> banQuery = new QueryWrapper<User>().eq("user_role", "ban");
        statistics.setBanCount(this.baseMapper.selectCount(banQuery));

        QueryWrapper<User> enabledQuery = new QueryWrapper<User>().eq("status", 1);
        statistics.setEnabledCount(this.baseMapper.selectCount(enabledQuery));

        QueryWrapper<User> disabledQuery = new QueryWrapper<User>().eq("status", 0);
        statistics.setDisabledCount(this.baseMapper.selectCount(disabledQuery));

        statistics.setActiveUsers(statistics.getTotalUsers());
        statistics.setNewUsers(0L);
        statistics.setTodayLoginCount(0L);
        statistics.setWeekLoginCount(0L);
        statistics.setMonthLoginCount(0L);
        return statistics;
    }
}
