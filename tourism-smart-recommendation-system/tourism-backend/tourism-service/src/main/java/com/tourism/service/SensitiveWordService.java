package com.tourism.service;

import java.util.Set;

/**
 * 敏感词过滤服务
 */
public interface SensitiveWordService {

    /**
     * 检查文本是否包含敏感词
     */
    boolean containsSensitiveWord(String text);

    /**
     * 获取文本中的所有敏感词
     */
    Set<String> findSensitiveWords(String text);

    /**
     * 过滤敏感词（替换为*）
     */
    String filterSensitiveWord(String text);

    /**
     * 刷新敏感词库（从数据库重新加载）
     */
    void refreshSensitiveWords();
}
