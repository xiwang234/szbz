package xw.szbz.cn.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import xw.szbz.cn.entity.LifeAIResult;
import xw.szbz.cn.entity.UserSaltInfo;
import xw.szbz.cn.entity.WebUser;
import xw.szbz.cn.model.ApiResponse;
import xw.szbz.cn.model.AuthResponse;
import xw.szbz.cn.model.LifeAIHistoryResponse;
import xw.szbz.cn.model.LifeAIRequest;
import xw.szbz.cn.model.LifeAIResponse;
import xw.szbz.cn.model.PageResponse;
import xw.szbz.cn.model.PasswordResetRequest;
import xw.szbz.cn.model.RandomSaltResponse;
import xw.szbz.cn.model.RefreshTokenRequest;
import xw.szbz.cn.model.RegisterRequest;
import xw.szbz.cn.model.ResetPasswordRequest;
import xw.szbz.cn.model.UserInfoResponse;
import xw.szbz.cn.model.WebLoginRequest;
import xw.szbz.cn.repository.LifeAIResultRepository;
import xw.szbz.cn.repository.UserSaltInfoRepository;
import xw.szbz.cn.service.AuthService;
import xw.szbz.cn.service.DataMaskingService;
import xw.szbz.cn.service.GeminiService;
import xw.szbz.cn.service.LiuRenService;
import xw.szbz.cn.service.RandomSaltService;
import xw.szbz.cn.util.EnhancedJwtUtil;
import xw.szbz.cn.util.FieldEncryptionUtil;
import xw.szbz.cn.util.PromptTemplateUtil;
/**
 * Web应用认证Controller
 * 提供注册、登录、Token刷新、登出等接口
 */
@RestController
@RequestMapping("/api/web-auth")
public class WebAuthController {
    
    @Autowired
    private AuthService authService;

    @Autowired
    private EnhancedJwtUtil jwtUtil;

    @Autowired
    private FieldEncryptionUtil fieldEncryptionUtil;

    @Autowired
    private DataMaskingService maskingService;

    @Autowired
    private RandomSaltService randomSaltService;

    @Autowired
    private LiuRenService liuRenService;

    @Autowired
    private PromptTemplateUtil promptTemplateUtil;

    @Autowired
    private LifeAIResultRepository lifeAIResultRepository;

    @Autowired
    private UserSaltInfoRepository userSaltInfoRepository;

    @Autowired
    private GeminiService geminiService;

    private static final Logger logger = LoggerFactory.getLogger(WebAuthController.class);
    
    /**
     * LifeAI 接口
     * POST /api/web-auth/lifeai
     * 需要登录，提供人生建议和咨询服务
     */
      @PostMapping("/lifeai")
      public ResponseEntity<ApiResponse<LifeAIResponse>> lifeAI(
              @RequestHeader("Authorization") String authHeader,
              @RequestBody LifeAIRequest request) {

          try {

                // 1. 从 JWT Token 中获取用户信息
                // JWT Filter 已经验证过 Token 有效性
                String token = extractToken(authHeader);
                String encryptedUserId = jwtUtil.getEncryptedUserIdFromToken(token);

                logger.info("LifeAI 请求，encryptedUserId: {}", encryptedUserId);
                // 2. 获取用户详细信息并检查状态
                WebUser user;
                try {
                    user = authService.getUserByEncryptedId(encryptedUserId);
                } catch (Exception e) {
                    logger.error("获取用户信息失败, encryptedUserId: {}", encryptedUserId, e);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("用户信息获取失败"));
                }

                // 3. 检查用户账户状态
                if (!user.getActive()) {
                    logger.warn("用户账户已被禁用, userId: {}", encryptedUserId);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("账户已被禁用"));
                }

                // 4. 验证免费体验次数
                Integer freeCount = user.getFreeCount();
                if (freeCount == null || freeCount <= 0) {
                    logger.warn("用户免费次数不足, userId: {}, freeCount: {}", user.getId(), freeCount);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("您的免费体验次数已用完"));
                }

                logger.info("LifeAI 请求，用户: {}, 问题分类: {}, 剩余次数: {}", user.getUsername(), request.getCategory(), freeCount);

                // 5. 参数验证
                validateLifeAIRequest(request);

                // 5. TODO: 业务逻辑待定
                // 这里将来会调用 AI 服务进行处理
                String requestId = java.util.UUID.randomUUID().toString();
                String answer = "no analysis"; // 占位符



                // ===== Step 4: 生成课传信息（根据当前时间排出农历四柱及大六壬天将） =====
                String courseInfo = liuRenService.generateCourseInfo();
                System.out.println("生成课传信息: " + courseInfo);

                // ===== Step 5: 将出生年份转换为干支年份 =====
                String ganZhiYear = liuRenService.convertBirthYearToGanZhi(request.getBirthYear());
                System.out.println("干支年份: " + ganZhiYear);

                // ===== Step 6: 组合干支信息和性别生成birthInfo =====
                String birthInfo = liuRenService.generateBirthInfo(request.getBirthYear(), request.getGender());
                System.out.println("出生信息: " + birthInfo);

                // ===== Step 7: 渲染提示词模板 =====
                String prompt = promptTemplateUtil.renderLiuRenTemplate(
                    courseInfo,
                    request.getQuestion(),
                    request.getBackground(),
                    birthInfo
                );

                logger.info("六壬预测提示词: " + prompt);

                // ===== Step 8: 调用Gemini AI进行预测 =====
                String aiPrediction = geminiService.generateContent(prompt);
                logger.info("六壬预测结果: " + aiPrediction);

                // ===== Step 9: 生成量化数据（支持多语言）=====
                // 获取语言参数，默认为中文
                String language = request.getLanguage();
                if (language == null || language.isEmpty()) {
                    language = "cn"; // 默认中文
                }
                logger.info("使用语言: {}", language);

                String prompt2 = promptTemplateUtil.renderLiuRenResultJsonTemplate(
                    aiPrediction,
                    courseInfo,
                    request.getQuestion(),
                    request.getBackground(),
                    language);
                logger.info("六壬量化prompt: " + prompt2);
                 // When
                answer = geminiService.generateStructuredJson(prompt2);
                logger.info("六壬量化结果: " + answer);


                // 9. 构建响应
                LifeAIResponse response = new LifeAIResponse(
                    requestId,
                    request.getBackground(),
                    request.getQuestion(),
                    request.getBirthYear(),
                    request.getGender(),
                    request.getCategory(),
                    answer,
                    System.currentTimeMillis()
                );

                // 10. 保存结果到数据库
                try {
                    LifeAIResult lifeAIResult = new LifeAIResult();
                    lifeAIResult.setUserId(user.getId());  // 使用真实的数据库 ID
                    lifeAIResult.setQuestion(request.getQuestion());
                    lifeAIResult.setBackground(request.getBackground());
                    lifeAIResult.setResult(answer);
                    lifeAIResult.setBirthdayYear(request.getBirthYear());
                    lifeAIResult.setGender(request.getGender());
                    lifeAIResult.setCategory(request.getCategory());
                    lifeAIResult.setCreateTime(System.currentTimeMillis());

                    lifeAIResultRepository.save(lifeAIResult);
                    logger.info("LifeAI 结果已保存到数据库，用户ID: {}, 记录ID: {}", user.getId(), lifeAIResult.getId());
                } catch (Exception e) {
                    // 保存失败不影响响应，只记录日志
                    logger.error("保存 LifeAI 结果到数据库失败", e);
                }

                // 11. 扣减免费次数
                try {
                    Integer currentFreeCount = user.getFreeCount();
                    if (currentFreeCount != null && currentFreeCount > 0) {
                        user.setFreeCount(currentFreeCount - 1);
                        authService.updateUser(user);
                        logger.info("免费次数已扣减，用户ID: {}, 剩余次数: {}", user.getId(), user.getFreeCount());
                    }
                } catch (Exception e) {
                    // 扣减失败只记录日志，不影响响应
                    logger.error("扣减免费次数失败", e);
                }

                return ResponseEntity.ok(ApiResponse.success(response, "请求成功"));

          } catch (IllegalArgumentException e) {
              return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                  .body(ApiResponse.error(e.getMessage()));
          } catch (Exception e) {
              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                  .body(ApiResponse.error("服务器内部错误：" + e.getMessage()));
          }
      }

      
    /**
     * 用户注册
     * POST /api/web-auth/register
     * 前端需要先将密码进行 SHA256(原密码 + 固定盐) 后再传入
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(
            @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIp(httpRequest);

        try {
            authService.register(request, ipAddress);
            return ResponseEntity.ok(ApiResponse.success(
                "注册成功，请验证邮箱", null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取随机盐（用于登录）
     * GET /api/web-auth/random-salt
     * 需要提供邮箱，返回32位随机字符串，5分钟有效期
     * 随机盐与邮箱绑定，防止恶意攻击
     */
    @GetMapping("/random-salt")
    public ResponseEntity<ApiResponse<RandomSaltResponse>> getRandomSalt(
            @RequestParam String email) {
        try {
            // 参数验证
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("邮箱不能为空"));
            }

            // 生成随机盐
            String randomSalt = randomSaltService.generateRandomSalt();
            Long currentTime = System.currentTimeMillis();
            Long expiresAt = currentTime + (5 * 60 * 1000); // 5分钟后过期
            // 加密邮箱
            String encryptedEmail = fieldEncryptionUtil.encryptEmail(email);
            // 保存到数据库
            UserSaltInfo saltInfo = new UserSaltInfo();
            saltInfo.setEmail(encryptedEmail);
            saltInfo.setSalt(randomSalt);
            saltInfo.setStatus(0); // 0-未使用
            saltInfo.setCreateTime(currentTime);
            saltInfo.setUseTime(expiresAt);

            userSaltInfoRepository.save(saltInfo);

            logger.info("生成随机盐成功，邮箱: {}, 有效期至: {}", email, expiresAt);

            RandomSaltResponse response = new RandomSaltResponse(randomSalt, expiresAt);
            return ResponseEntity.ok(ApiResponse.success(response, "获取随机盐成功"));
        } catch (Exception e) {
            logger.error("获取随机盐失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 用户登录
     * POST /api/web-auth/login
     * 前端需要先调用 /random-salt 获取随机盐
     * 然后将密码进行 SHA256(原密码 + 固定盐 + 随机盐) 后再传入
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody WebLoginRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        try {
            // 1. 验证随机盐
            if (request.getRandomSalt() == null || request.getRandomSalt().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("随机盐不能为空"));
            }

            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("邮箱不能为空"));
            }
            String encryptedEmail = fieldEncryptionUtil.encryptEmail(request.getEmail());
            // 2. 查询数据库验证随机盐
            java.util.Optional<UserSaltInfo> saltInfoOpt = userSaltInfoRepository
                .findByEmailAndSalt(encryptedEmail, request.getRandomSalt());

            if (!saltInfoOpt.isPresent()) {
                logger.warn("随机盐验证失败：随机盐不存在或邮箱不匹配, 邮箱: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("随机盐无效或已过期"));
            }

            UserSaltInfo saltInfo = saltInfoOpt.get();

            // 3. 验证随机盐状态（必须是未使用）
            if (saltInfo.getStatus() != 0) {
                logger.warn("随机盐已被使用, 邮箱: {}, 随机盐: {}", request.getEmail(), request.getRandomSalt());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("随机盐已被使用"));
            }

            // 4. 验证随机盐有效期
            Long currentTime = System.currentTimeMillis();
            if (currentTime > saltInfo.getUseTime()) {
                logger.warn("随机盐已过期, 邮箱: {}, 过期时间: {}", request.getEmail(), saltInfo.getUseTime());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("随机盐已过期，请重新获取"));
            }

            // 5. 随机盐验证通过，执行登录逻辑
            AuthResponse response = authService.login(request, ipAddress, userAgent);

            // 6. 登录成功后，更新随机盐状态为已使用
            saltInfo.setStatus(1);
            userSaltInfoRepository.save(saltInfo);
            logger.info("登录成功，随机盐已标记为已使用, 邮箱: {}", request.getEmail());

            return ResponseEntity.ok(ApiResponse.success(response,"登录成功"));
        } catch (Exception e) {
            logger.error("登录失败, 邮箱: {}", request.getEmail(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 刷新Token
     * POST /api/web-auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        
        String ipAddress = getClientIp(httpRequest);
        
        try {
            AuthResponse response = authService.refreshToken(
                request.getRefreshToken(),
                request.getDeviceId(),
                ipAddress
            );
            return ResponseEntity.ok(ApiResponse.success(response,"Token刷新成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 登出
     * POST /api/web-auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) RefreshTokenRequest request) {
        
        try {
            String accessToken = extractToken(authHeader);
            String refreshToken = request != null ? request.getRefreshToken() : null;
            
            authService.logout(accessToken, refreshToken);
            return ResponseEntity.ok(ApiResponse.success("登出成功", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取当前用户信息
     * GET /api/web-auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {

        try {
            // JWT Filter 已经验证过 Token，直接获取用户信息
            String token = extractToken(authHeader);
            String encryptedUserId = jwtUtil.getEncryptedUserIdFromToken(token);

            // 获取用户详细信息
            WebUser user;
            try {
                user = authService.getUserByEncryptedId(encryptedUserId);
            } catch (Exception e) {
                logger.error("获取用户信息失败, encryptedUserId: {}", encryptedUserId, e);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("用户信息获取失败"));
            }
            
            // 解密并脱敏邮箱
            String plainEmail = fieldEncryptionUtil.decryptEmail(user.getEmail());
            String maskedEmail = maskingService.maskEmail(plainEmail);
            
            // 构建响应
            UserInfoResponse response = new UserInfoResponse();
            response.setEncryptedUserId(encryptedUserId);
            response.setUsername(user.getUsername());
            response.setMaskedEmail(maskedEmail);
            response.setEmailVerified(user.getEmailVerified());
            response.setActive(user.getActive());
            response.setCreateTime(user.getCreateTime());
            response.setLastLoginTime(user.getLastLoginTime());
            
            return ResponseEntity.ok(ApiResponse.success(response,"success"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取用户的 LifeAI 咨询历史（支持分页和类型过滤）
     * GET /api/web-auth/history
     *
     * @param pageNo 页码（从1开始，默认1）
     * @param pageSize 每页大小（默认10）
     * @param type 类型过滤（"all"表示所有类型，其他值按category过滤）
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PageResponse<LifeAIHistoryResponse>>> getHistory(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "all") String type) {

        try {
            // 1. 参数验证
            if (pageNo < 1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("页码必须大于0"));
            }
            if (pageSize < 1 || pageSize > 100) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("每页大小必须在1-100之间"));
            }

            // 2. 从 JWT Token 中获取用户信息
            String token = extractToken(authHeader);
            String encryptedUserId = jwtUtil.getEncryptedUserIdFromToken(token);

            logger.info("查询历史记录，encryptedUserId: {}, pageNo: {}, pageSize: {}, type: {}",
                encryptedUserId, pageNo, pageSize, type);

            // 3. 获取用户详细信息并检查状态
            WebUser user;
            try {
                user = authService.getUserByEncryptedId(encryptedUserId);
            } catch (Exception e) {
                logger.error("获取用户信息失败, encryptedUserId: {}", encryptedUserId, e);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("用户信息获取失败"));
            }

            // 4. 检查用户账户状态
            if (!user.getActive()) {
                logger.warn("用户账户已被禁用, userId: {}", encryptedUserId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("账户已被禁用"));
            }

            // 5. 创建分页对象（Spring Data JPA 的页码从0开始）
            org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(
                    pageNo - 1,
                    pageSize,
                    org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "createTime"
                    )
                );

            // 6. 根据类型查询数据
            org.springframework.data.domain.Page<LifeAIResult> resultPage;
            if ("all".equalsIgnoreCase(type)) {
                // 查询所有类型
                resultPage = lifeAIResultRepository.findByUserIdOrderByCreateTimeDesc(user.getId(), pageable);
            } else {
                // 按指定类型查询
                resultPage = lifeAIResultRepository.findByUserIdAndCategoryOrderByCreateTimeDesc(
                    user.getId(), type, pageable);
            }

            // 7. 转换为响应对象（不包含 user_id）
            java.util.List<LifeAIHistoryResponse> historyList = resultPage.getContent().stream()
                .map(r -> new LifeAIHistoryResponse(
                    r.getId(),
                    r.getQuestion(),
                    r.getBackground(),
                    r.getResult(),
                    r.getBirthdayYear(),
                    r.getGender(),
                    r.getCategory(),
                    r.getCreateTime()
                ))
                .collect(java.util.stream.Collectors.toList());

            // 8. 构建分页响应
            PageResponse<LifeAIHistoryResponse> pageResponse = new PageResponse<>(
                historyList,
                pageNo,
                pageSize,
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                resultPage.hasNext(),
                resultPage.hasPrevious()
            );

            logger.info("查询到 {} 条历史记录（第{}/{}页），用户ID: {}, 类型: {}",
                historyList.size(), pageNo, resultPage.getTotalPages(), user.getId(), type);

            return ResponseEntity.ok(ApiResponse.success(pageResponse, "查询成功"));

        } catch (Exception e) {
            logger.error("查询历史记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 获取用户免费体验次数
     * GET /api/web-auth/freeCount
     */
    @GetMapping("/freeCount")
    public ResponseEntity<ApiResponse<Integer>> getFreeCount(
            @RequestHeader("Authorization") String authHeader) {

        try {
            // 1. 从 JWT Token 中获取用户信息
            String token = extractToken(authHeader);
            String encryptedUserId = jwtUtil.getEncryptedUserIdFromToken(token);

            logger.info("查询免费次数，encryptedUserId: {}", encryptedUserId);

            // 2. 获取用户详细信息
            WebUser user;
            try {
                user = authService.getUserByEncryptedId(encryptedUserId);
            } catch (Exception e) {
                logger.error("获取用户信息失败, encryptedUserId: {}", encryptedUserId, e);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("用户信息获取失败"));
            }

            // 3. 获取免费次数，确保不为负数
            Integer freeCount = user.getFreeCount();
            if (freeCount == null || freeCount < 0) {
                freeCount = 0;
            }

            logger.info("用户免费次数: {}, 用户ID: {}", freeCount, user.getId());

            return ResponseEntity.ok(ApiResponse.success(freeCount, "查询成功"));

        } catch (Exception e) {
            logger.error("查询免费次数失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 获取历史咨询详情
     * GET /api/web-auth/detail
     *
     * @param id 历史记录ID（来自history接口返回的列表）
     * @return 返回该记录的result字段数据
     */
    @GetMapping("/detail")
    public ResponseEntity<ApiResponse<String>> getDetail(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam Long id) {

        try {
            // 1. 参数验证
            if (id == null || id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("记录ID不能为空且必须大于0"));
            }

            // 2. 从 JWT Token 中获取用户信息
            String token = extractToken(authHeader);
            String encryptedUserId = jwtUtil.getEncryptedUserIdFromToken(token);

            logger.info("查询历史详情，encryptedUserId: {}, recordId: {}", encryptedUserId, id);

            // 3. 获取用户详细信息并检查状态
            WebUser user;
            try {
                user = authService.getUserByEncryptedId(encryptedUserId);
            } catch (Exception e) {
                logger.error("获取用户信息失败, encryptedUserId: {}", encryptedUserId, e);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("用户信息获取失败"));
            }

            // 4. 检查用户账户状态
            if (!user.getActive()) {
                logger.warn("用户账户已被禁用, userId: {}", encryptedUserId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("账户已被禁用"));
            }

            // 5. 根据ID查询记录
            java.util.Optional<LifeAIResult> resultOpt = lifeAIResultRepository.findById(id);

            if (!resultOpt.isPresent()) {
                logger.warn("记录不存在, recordId: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("记录不存在"));
            }

            LifeAIResult lifeAIResult = resultOpt.get();

            // 6. 验证记录所属用户（确保只能查看自己的记录）
            if (!lifeAIResult.getUserId().equals(user.getId())) {
                logger.warn("用户尝试访问他人记录, userId: {}, recordUserId: {}, recordId: {}",
                    user.getId(), lifeAIResult.getUserId(), id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("无权访问该记录"));
            }

            // 7. 返回result字段数据
            String result = lifeAIResult.getResult();
            logger.info("成功获取历史详情, userId: {}, recordId: {}, resultLength: {}",
                user.getId(), id, result != null ? result.length() : 0);

            return ResponseEntity.ok(ApiResponse.success(result, "查询成功"));

        } catch (Exception e) {
            logger.error("查询历史详情失败, recordId: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    // ========== 私有辅助方法 ==========

    /**
     * 验证 LifeAI 请求参数
     */
    private void validateLifeAIRequest(LifeAIRequest request) {
        if (request.getBackground() == null || request.getBackground().trim().isEmpty()) {
            throw new IllegalArgumentException("背景描述不能为空");
        }

        if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            throw new IllegalArgumentException("问题不能为空");
        }

        if (request.getBirthYear() == null) {
            throw new IllegalArgumentException("出生年份不能为空");
        }

        // 验证出生年份范围
        int currentYear = java.time.Year.now().getValue();
        if (request.getBirthYear() < 1900 || request.getBirthYear() > currentYear) {
            throw new IllegalArgumentException("出生年份必须在1900-" + currentYear + "之间");
        }

        if (request.getGender() == null || request.getGender().trim().isEmpty()) {
            throw new IllegalArgumentException("性别不能为空");
        }

        // 验证性别格式
        String gender = request.getGender().toLowerCase();
        if (!gender.equals("男") && !gender.equals("女") &&
            !gender.equals("male") && !gender.equals("female")) {
            throw new IllegalArgumentException("性别格式错误，只支持：男/女/male/female");
        }

        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("分类不能为空");
        }

        // 验证字段长度
        if (request.getBackground().length() > 1000) {
            throw new IllegalArgumentException("背景描述不能超过1000字符");
        }

        if (request.getQuestion().length() > 500) {
            throw new IllegalArgumentException("问题不能超过500字符");
        }

        if (request.getCategory().length() > 50) {
            throw new IllegalArgumentException("分类不能超过50字符");
        }
    }

    // ========== 密码重置相关接口 ==========

    /**
     * 接口A：请求密码重置，发送重置邮件
     * POST /api/web-auth/request-reset
     */
    @PostMapping("/request-reset")
    public ResponseEntity<ApiResponse<String>> requestPasswordReset(
            @RequestBody PasswordResetRequest request) {
        try {
            authService.requestPasswordReset(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success(
                "如果该邮箱已注册，重置链接已发送，请在5分钟内完成重置", null
            ));
        } catch (xw.szbz.cn.exception.ServiceException e) {
            // 限流等业务异常直接返回错误信息
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            // 其他异常（如邮箱不存在）统一返回成功提示，不透露信息
            return ResponseEntity.ok(ApiResponse.success(
                "如果该邮箱已注册，重置链接已发送，请在5分钟内完成重置", null
            ));
        }
    }

    /**
     * 接口B：验证重置 Token 有效性
     * GET /api/web-auth/verify-reset-token
     */
    @GetMapping("/verify-reset-token")
    public ResponseEntity<ApiResponse<String>> verifyResetToken(
            @RequestParam String token) {
        try {
            authService.verifyResetToken(token);
            return ResponseEntity.ok(ApiResponse.success("", "token有效"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 接口C：重置密码
     * POST /api/web-auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success("密码重置成功", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ========== 私有辅助方法 ==========

    /**
     * 从请求中获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多级代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    /**
     * 从Authorization header中提取Token
     */
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid authorization header");
    }
}
