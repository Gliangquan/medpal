package com.jcen.medpal.service.impl;

import static com.jcen.medpal.constant.UserConstant.USER_LOGIN_STATE;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jcen.medpal.common.ErrorCode;
import com.jcen.medpal.constant.CommonConstant;
import com.jcen.medpal.exception.BusinessException;
import com.jcen.medpal.mapper.UserMapper;
import com.jcen.medpal.model.dto.user.UserQueryRequest;
import com.jcen.medpal.model.dto.user.WeChatLoginRequest;
import com.jcen.medpal.service.WeChatService;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.model.enums.UserRoleEnum;
import com.jcen.medpal.model.vo.LoginUserVO;
import com.jcen.medpal.model.vo.UserVO;
import com.jcen.medpal.service.UserService;
import com.jcen.medpal.utils.JwtTokenUtils;
import com.jcen.medpal.utils.SqlUtils;
import java.util.ArrayList;
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

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "jcenLeung";

    @Resource
    private JwtTokenUtils jwtTokenUtils;

    @Resource
    private WeChatService weChatService;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String userPhone, String userRole, String userName, String idCard) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, userPhone)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 6 || checkPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (userPhone.length() != 11) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式错误");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        
        // 设置默认角色
        if (StringUtils.isBlank(userRole)) {
            userRole = "patient";
        }
        
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_account", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 手机号不能重复
            QueryWrapper<User> phoneWrapper = new QueryWrapper<>();
            phoneWrapper.eq("user_phone", userPhone);
            long phoneCount = this.baseMapper.selectCount(phoneWrapper);
            if (phoneCount > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号已被注册");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setUserPhone(userPhone);
            user.setUserRole(userRole);
            user.setUserName(userName);
            if ("companion".equals(userRole)) {
                user.setIdCard(idCard);
                user.setRealNameStatus("not_submitted");
                user.setQualificationStatus("not_submitted");
            }
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    @Deprecated
    public LoginUserVO userLogin(String userAccount, String userPassword, String userPhone, HttpServletRequest request) {
        // 已废弃，使用 userLoginByAccount 或 userLoginByPhone
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "请使用账号登录或手机号登录");
    }

    @Override
    public LoginUserVO userLoginByAccount(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号格式错误");
        }
        if (userPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_password", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        LoginUserVO loginUserVO = this.getLoginUserVO(user);
        // 4. 生成 JWT Token
        String token = jwtTokenUtils.generateToken(user.getId(), user.getUserAccount());
        loginUserVO.setToken(token);
        return loginUserVO;
    }

    @Override
    public LoginUserVO userLoginByPhone(String userPhone, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userPhone, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userPhone.length() != 11) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式错误");
        }
        if (userPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_phone", userPhone);
        queryWrapper.eq("user_password", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userPhone cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        LoginUserVO loginUserVO = this.getLoginUserVO(user);
        // 4. 生成 JWT Token
        String token = jwtTokenUtils.generateToken(user.getId(), user.getUserAccount());
        loginUserVO.setToken(token);
        return loginUserVO;
    }

    @Override
    public LoginUserVO userLoginByWeChat(String code, String nickName, String avatarUrl, HttpServletRequest request) {
        // 1. 校验 code
        if (StringUtils.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "微信登录凭证不能为空");
        }

        // 2. 调用微信接口获取 openid
        Map<String, Object> weChatInfo = weChatService.getSessionKeyOrOpenid(code);
        String openid = (String) weChatInfo.get("openid");
        String unionid = (String) weChatInfo.get("unionid");

        if (StringUtils.isBlank(openid)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取微信用户信息失败");
        }

        // 3. 查询用户是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mp_open_id", openid);
        User user = this.baseMapper.selectOne(queryWrapper);

        boolean isNewUser = false;
        // 4. 用户不存在，创建新用户
        if (user == null) {
            user = new User();
            user.setMpOpenId(openid);
            user.setUnionId(unionid);
            user.setUserRole("user");
            user.setStatus(1);
            user.setUserAccount("wechat_" + openid.substring(0, 8));
            user.setUserPassword(DigestUtils.md5DigestAsHex((SALT + openid).getBytes()));

            if (StringUtils.isNotBlank(nickName)) {
                user.setUserName(nickName);
            }
            if (StringUtils.isNotBlank(avatarUrl)) {
                user.setUserAvatar(avatarUrl);
            }

            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建用户失败");
            }
            isNewUser = true;
        } else {
            // 更新用户头像和昵称
            if (StringUtils.isNotBlank(nickName) && StringUtils.isBlank(user.getUserName())) {
                user.setUserName(nickName);
            }
            if (StringUtils.isNotBlank(avatarUrl) && StringUtils.isBlank(user.getUserAvatar())) {
                user.setUserAvatar(avatarUrl);
            }
            if (user.getUserName() == null) {
                user.setUserName("微信用户");
            }
            this.updateById(user);
        }

        // 5. 记录登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        LoginUserVO loginUserVO = this.getLoginUserVO(user);

        // 6. 生成 JWT Token
        String token = jwtTokenUtils.generateToken(user.getId(), user.getUserAccount());
        loginUserVO.setToken(token);
        loginUserVO.setIsNewUser(isNewUser);
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
        if (newPassword.length() < 6 || checkPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码长度不能小于 6 位");
        }
        if (!StringUtils.equals(newPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的新密码不一致");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_phone", userPhone);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "该手机号未注册");
        }
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", user.getId()).set("user_password", encryptPassword);
        boolean updated = this.update(updateWrapper);
        if (!updated) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "密码重置失败");
        }
        return true;
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录（Session）
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        
        // 如果 Session 中没有，尝试从 Token 中获取
        if (currentUser == null || currentUser.getId() == null) {
            String token = getTokenFromRequest(request);
            if (StringUtils.isNotBlank(token) && jwtTokenUtils.validateToken(token)) {
                Long userId = jwtTokenUtils.getUserIdFromToken(token);
                currentUser = this.getById(userId);
                if (currentUser != null) {
                    return currentUser;
                }
            }
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 从请求中获取 Token
     *
     * @param request 请求
     * @return Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 先从 Authorization header 中获取
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        // 如果 header 中没有，尝试从查询参数中获取（用于图片等资源的跨域请求）
        String tokenParam = request.getParameter("token");
        if (StringUtils.isNotBlank(tokenParam)) {
            return tokenParam;
        }
        return null;
    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录（Session）
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        
        // 如果 Session 中没有，尝试从 Token 中获取
        if (currentUser == null || currentUser.getId() == null) {
            String token = getTokenFromRequest(request);
            if (StringUtils.isNotBlank(token) && jwtTokenUtils.validateToken(token)) {
                Long userId = jwtTokenUtils.getUserIdFromToken(token);
                currentUser = this.getById(userId);
                if (currentUser != null) {
                    return currentUser;
                }
            }
            return null;
        }
        
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
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
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userPhone = userQueryRequest.getUserPhone();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String realNameStatus = userQueryRequest.getRealNameStatus();
        String qualificationStatus = userQueryRequest.getQualificationStatus();
        String keyword = userQueryRequest.getKeyword();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "union_id", unionId);
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mp_open_id", mpOpenId);
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
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public int batchDeleteUser(List<Long> ids, Boolean softDelete) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        if (softDelete == null || softDelete) {
            // 软删除
            return this.baseMapper.update(null, new UpdateWrapper<User>().in("id", ids).set("is_delete", 1));
        } else {
            // 硬删除
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
        // 构建查询条件
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
        List<User> users = this.list(queryWrapper);
        // 这里简化处理，实际应该生成真实的文件
        // 返回导出文件的URL或Base64编码
        return "export_" + System.currentTimeMillis() + ".xlsx";
    }

    @Override
    public int importUser(com.jcen.medpal.model.dto.user.UserImportRequest userImportRequest) {
        // 这里简化处理，实际应该解析文件内容
        // 返回导入的用户数量
        return 0;
    }

    @Override
    public com.jcen.medpal.model.vo.UserStatisticsVO getUserStatistics() {
        com.jcen.medpal.model.vo.UserStatisticsVO statistics = new com.jcen.medpal.model.vo.UserStatisticsVO();
        
        // 总用户数
        statistics.setTotalUsers(this.count());
        
        // 各角色用户数
        QueryWrapper<User> adminQuery = new QueryWrapper<User>().eq("user_role", "admin");
        statistics.setAdminCount(this.baseMapper.selectCount(adminQuery));
        
        QueryWrapper<User> userQuery = new QueryWrapper<User>().eq("user_role", "user");
        statistics.setUserCount(this.baseMapper.selectCount(userQuery));
        
        QueryWrapper<User> banQuery = new QueryWrapper<User>().eq("user_role", "ban");
        statistics.setBanCount(this.baseMapper.selectCount(banQuery));
        
        // 账户状态统计
        QueryWrapper<User> enabledQuery = new QueryWrapper<User>().eq("status", 1);
        statistics.setEnabledCount(this.baseMapper.selectCount(enabledQuery));
        
        QueryWrapper<User> disabledQuery = new QueryWrapper<User>().eq("status", 0);
        statistics.setDisabledCount(this.baseMapper.selectCount(disabledQuery));
        
        // 活跃用户数（最近7天）- 简化处理
        statistics.setActiveUsers(statistics.getTotalUsers());
        statistics.setNewUsers(0L);
        statistics.setTodayLoginCount(0L);
        statistics.setWeekLoginCount(0L);
        statistics.setMonthLoginCount(0L);
        
        return statistics;
    }
}
