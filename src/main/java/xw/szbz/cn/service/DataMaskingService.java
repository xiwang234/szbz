package xw.szbz.cn.service;

import org.springframework.stereotype.Service;

/**
 * 数据脱敏服务
 * 提供邮箱、手机号等敏感信息的脱敏处理
 */
@Service
public class DataMaskingService {
    
    /**
     * 邮箱脱敏
     * 规则：保留首字母和@后的域名，中间用***代替
     * 示例：user@example.com -> u***@example.com
     * 
     * @param email 明文邮箱
     * @return 脱敏后的邮箱
     */
    public String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            // 无效邮箱格式，直接返回***
            return "***";
        }
        
        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex);
        
        // 如果本地部分太短，至少保留1个字符
        if (localPart.length() <= 1) {
            return localPart.charAt(0) + "***" + domainPart;
        } else if (localPart.length() == 2) {
            return localPart.charAt(0) + "*" + domainPart;
        } else {
            // 保留首字母，其余用***代替
            return localPart.charAt(0) + "***" + domainPart;
        }
    }
    
    /**
     * 手机号脱敏
     * 规则：保留前3位和后4位，中间用****代替
     * 示例：13812345678 -> 138****5678
     * 
     * @param phone 明文手机号
     * @return 脱敏后的手机号
     */
    public String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }
        
        // 移除所有非数字字符
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        
        if (cleanPhone.length() < 7) {
            // 太短的手机号，全部脱敏
            return "***";
        } else if (cleanPhone.length() <= 10) {
            // 中国大陆手机号（11位）或其他较短号码
            int prefixLen = 3;
            int suffixLen = 4;
            if (cleanPhone.length() < 7) {
                prefixLen = 1;
                suffixLen = 1;
            }
            return cleanPhone.substring(0, prefixLen) + "****" + 
                   cleanPhone.substring(cleanPhone.length() - suffixLen);
        } else {
            // 国际号码，保留前3位和后4位
            return cleanPhone.substring(0, 3) + "****" + 
                   cleanPhone.substring(cleanPhone.length() - 4);
        }
    }
    
    /**
     * 身份证号脱敏
     * 规则：保留前6位和后4位，中间用****代替
     * 示例：110101199001011234 -> 110101********1234
     * 
     * @param idCard 明文身份证号
     * @return 脱敏后的身份证号
     */
    public String maskIdCard(String idCard) {
        if (idCard == null || idCard.isEmpty()) {
            return idCard;
        }
        
        String cleanIdCard = idCard.trim();
        
        if (cleanIdCard.length() == 18) {
            // 18位身份证
            return cleanIdCard.substring(0, 6) + "********" + cleanIdCard.substring(14);
        } else if (cleanIdCard.length() == 15) {
            // 15位身份证
            return cleanIdCard.substring(0, 6) + "*****" + cleanIdCard.substring(11);
        } else {
            // 格式不对，全部脱敏
            return "***";
        }
    }
    
    /**
     * 姓名脱敏
     * 规则：保留姓，名字用*代替
     * 示例：张三 -> 张*, 张三丰 -> 张**
     * 
     * @param name 明文姓名
     * @return 脱敏后的姓名
     */
    public String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        
        String trimmedName = name.trim();
        
        if (trimmedName.length() == 1) {
            return trimmedName;
        } else if (trimmedName.length() == 2) {
            return trimmedName.charAt(0) + "*";
        } else {
            // 保留姓氏，其余用*代替
            StringBuilder masked = new StringBuilder();
            masked.append(trimmedName.charAt(0));
            for (int i = 1; i < trimmedName.length(); i++) {
                masked.append('*');
            }
            return masked.toString();
        }
    }
    
    /**
     * 银行卡号脱敏
     * 规则：保留前4位和后4位，中间用****代替
     * 示例：6222021234567890123 -> 6222************0123
     * 
     * @param cardNumber 明文银行卡号
     * @return 脱敏后的银行卡号
     */
    public String maskBankCard(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return cardNumber;
        }
        
        // 移除空格和分隔符
        String cleanCard = cardNumber.replaceAll("[\\s-]", "");
        
        if (cleanCard.length() < 8) {
            return "***";
        } else if (cleanCard.length() <= 16) {
            return cleanCard.substring(0, 4) + "****" + 
                   cleanCard.substring(cleanCard.length() - 4);
        } else {
            // 长卡号
            return cleanCard.substring(0, 4) + "************" + 
                   cleanCard.substring(cleanCard.length() - 4);
        }
    }
    
    /**
     * 通用脱敏（适用于不确定类型的敏感信息）
     * 规则：保留前后各20%，中间用***代替
     * 
     * @param data 明文数据
     * @return 脱敏后的数据
     */
    public String maskGeneric(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        
        int length = data.length();
        
        if (length <= 3) {
            return "***";
        }
        
        int showLength = Math.max(1, (int) (length * 0.2));
        
        return data.substring(0, showLength) + "***" + 
               data.substring(length - showLength);
    }
}
