package xw.szbz.cn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import xw.szbz.cn.model.ApiResponse;
import xw.szbz.cn.service.SecurityManagementService;

import java.util.Map;
import java.util.Set;

/**
 * 安全管理控制器
 * 提供 IP 黑名单管理、安全统计查询等功能
 * 
 * 注意：生产环境请添加管理员权限验证！
 */
@RestController
@RequestMapping("/api/security")
public class SecurityController {

    @Autowired
    private SecurityManagementService securityService;

    /**
     * 查询当前 IP 黑名单
     */
    @GetMapping("/blacklist")
    public ResponseEntity<ApiResponse<Set<String>>> getBlacklist() {
        Set<String> blockedIps = securityService.getBlockedIps();
        return ResponseEntity.ok(ApiResponse.success(blockedIps));
    }

    /**
     * 手动封禁 IP
     * @param ip 要封禁的 IP 地址
     */
    @PostMapping("/block")
    public ResponseEntity<ApiResponse<String>> blockIp(@RequestParam String ip) {
        securityService.blockIp(ip);
        return ResponseEntity.ok(ApiResponse.success("IP " + ip + " 已被封禁"));
    }

    /**
     * 解封 IP
     * @param ip 要解封的 IP 地址
     */
    @PostMapping("/unblock")
    public ResponseEntity<ApiResponse<String>> unblockIp(@RequestParam String ip) {
        securityService.unblockIp(ip);
        return ResponseEntity.ok(ApiResponse.success("IP " + ip + " 已解封"));
    }

    /**
     * 查询 IP 拦截统计
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSecurityStats() {
        Map<String, Object> stats = securityService.getSecurityStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 清空 IP 黑名单（慎用）
     */
    @PostMapping("/clear")
    public ResponseEntity<ApiResponse<String>> clearBlacklist() {
        securityService.clearBlacklist();
        return ResponseEntity.ok(ApiResponse.success("IP 黑名单已清空"));
    }
}
