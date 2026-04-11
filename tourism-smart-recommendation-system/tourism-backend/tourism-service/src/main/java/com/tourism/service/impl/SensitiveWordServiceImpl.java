package com.tourism.service.impl;

import com.tourism.service.SensitiveWordService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 基于DFA算法的敏感词过滤服务实现
 */
@Slf4j
@Service
public class SensitiveWordServiceImpl implements SensitiveWordService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String REPLACEMENT = "***";
    private volatile Map<Character, Object> sensitiveWordMap = new HashMap<>();

    @PostConstruct
    public void init() {
        refreshSensitiveWords();
    }

    @Override
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) return false;
        text = text.toLowerCase();
        for (int i = 0; i < text.length(); i++) {
            if (checkSensitiveWord(text, i) > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> findSensitiveWords(String text) {
        Set<String> result = new LinkedHashSet<>();
        if (text == null || text.isEmpty()) return result;
        text = text.toLowerCase();
        for (int i = 0; i < text.length(); i++) {
            int length = checkSensitiveWord(text, i);
            if (length > 0) {
                result.add(text.substring(i, i + length));
                i += length - 1;
            }
        }
        return result;
    }

    @Override
    public String filterSensitiveWord(String text) {
        if (text == null || text.isEmpty()) return text;
        String lowerText = text.toLowerCase();
        StringBuilder sb = new StringBuilder(text);
        int offset = 0;
        for (int i = 0; i < lowerText.length(); i++) {
            int length = checkSensitiveWord(lowerText, i);
            if (length > 0) {
                int start = i + offset;
                sb.replace(start, start + length, REPLACEMENT);
                offset += REPLACEMENT.length() - length;
                i += length - 1;
            }
        }
        return sb.toString();
    }

    @Override
    @Scheduled(fixedRate = 300000) // 每5分钟刷新一次
    public void refreshSensitiveWords() {
        try {
            List<String> words = jdbcTemplate.queryForList(
                    "SELECT word FROM sensitive_word WHERE status = 1", String.class);
            Map<Character, Object> newMap = buildDfaMap(words);
            sensitiveWordMap = newMap;
            log.info("敏感词库已刷新，共加载 {} 个敏感词", words.size());
        } catch (Exception e) {
            log.warn("加载敏感词库失败: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Character, Object> buildDfaMap(List<String> words) {
        Map<Character, Object> root = new HashMap<>();
        for (String word : words) {
            if (word == null || word.trim().isEmpty()) continue;
            word = word.trim().toLowerCase();
            Map<Character, Object> current = root;
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                Object node = current.get(c);
                if (node == null) {
                    Map<Character, Object> newNode = new HashMap<>();
                    current.put(c, newNode);
                    current = newNode;
                } else {
                    current = (Map<Character, Object>) node;
                }
            }
            current.put('\0', null); // 结束标记
        }
        return root;
    }

    @SuppressWarnings("unchecked")
    private int checkSensitiveWord(String text, int beginIndex) {
        Map<Character, Object> current = sensitiveWordMap;
        int matchLength = 0;
        int lastMatchLength = 0;
        for (int i = beginIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            Object node = current.get(c);
            if (node == null) break;
            matchLength++;
            current = (Map<Character, Object>) node;
            if (current.containsKey('\0')) {
                lastMatchLength = matchLength;
            }
        }
        return lastMatchLength;
    }
}
