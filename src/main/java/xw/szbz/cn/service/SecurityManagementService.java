package xw.szbz.cn.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xw.szbz.cn.config.SecurityFilter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 安全管理服务
 * 提供 IP 黑名单管理和安全统计功能
 */
@Service
public class SecurityManagementService {

    @Autowired
    private SecurityFilter securityFilter;

    /**
     * 获取当前封禁的 IP 列表
     */
    public Set<String> getBlockedIps() {
        return securityFilter.getBlockedIps();
    }

    /**
     * 手动封禁 IP
     */
    public void blockIp(String ip) {
        securityFilter.manualBlockIp(ip);
    }

    /**
     * 解封 IP
     */
    public void unblockIp(String ip) {
        securityFilter.unblockIp(ip);
    }

    /**
     * 清空黑名单
     */
    public void clearBlacklist() {
        securityFilter.clearBlacklist();
    }

    /**
     * 获取安全统计信息
     */
    public Map<String, Object> getSecurityStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("blockedIpCount", securityFilter.getBlockedIps().size());
        stats.put("blockedIps", securityFilter.getBlockedIps());
        stats.put("blockCounts", securityFilter.getBlockCounts());
        return stats;
    }
}
