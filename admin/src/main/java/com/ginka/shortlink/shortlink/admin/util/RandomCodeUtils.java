package com.ginka.shortlink.shortlink.admin.util;

import java.security.SecureRandom;

// 随机码生成工具类
public final class RandomCodeUtils {
    private static final char[] CHAR_POOL = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
    };

    private static final int CODE_LENGTH = 6;

    // 全局复用SecureRandom实例（避免重复创建开销）
    private static final SecureRandom SECURE_RANDOM;

    static {
        try {
            // 使用强安全算法初始化（优先选择平台默认的强安全实现）
            SECURE_RANDOM = SecureRandom.getInstanceStrong();
        } catch (Exception e) {
            throw new RuntimeException("初始化安全随机数生成器失败", e);
        }
    }

    /**
     * 生成密码学安全的6位字母数字混合随机码
     * @return 6位安全随机字符串
     */
    public static String generateSecureRandomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = SECURE_RANDOM.nextInt(CHAR_POOL.length);
            sb.append(CHAR_POOL[index]);
        }

        return sb.toString();
    }
}
