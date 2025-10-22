package com.homeordering.mapper;

import com.homeordering.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper {
    
    /**
     * 根据openid查询用户
     */
    User selectByOpenid(@Param("openid") String openid);
    
    /**
     * 根据uuid查询用户
     */
    User selectByUuid(@Param("uuid") String uuid);
    
    /**
     * 插入用户
     */
    int insert(User user);
    
    /**
     * 更新用户信息
     */
    int update(User user);
}
