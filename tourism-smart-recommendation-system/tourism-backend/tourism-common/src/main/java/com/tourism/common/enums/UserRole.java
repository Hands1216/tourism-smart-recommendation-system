package com.tourism.common.enums;

import lombok.Getter;

/**
 * 用户角色枚举
 *
 * @author 韩东升
 */
@Getter
public enum UserRole {

    /**
     * 普通游客
     */
    USER("user", "普通游客"),

    /**
     * 内容管理员
     */
    CONTENT_ADMIN("content_admin", "内容管理员"),

    /**
     * 系统管理员
     */
    ADMIN("admin", "系统管理员");

    private final String code;
    private final String desc;

    UserRole(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static UserRole getByCode(String code) {
        for (UserRole role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return USER;
    }

    /**
     * 获取角色名称（用于日志记录）
     */
    public static String getRoleName(String code) {
        UserRole role = getByCode(code);
        return role != null ? role.getDesc() : "未知";
    }
}
