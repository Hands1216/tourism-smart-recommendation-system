package com.tourism.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.model.entity.UserBehavior;
import com.tourism.service.UserBehaviorService;
import com.tourism.service.mapper.UserBehaviorMapper;
import org.springframework.stereotype.Service;

/**
 * 用户行为服务实现
 *
 */
@Service
public class UserBehaviorServiceImpl extends ServiceImpl<UserBehaviorMapper, UserBehavior> implements UserBehaviorService {
}
