package com.homeordering.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homeordering.entity.Family;
import org.apache.ibatis.annotations.Mapper;

/**
 * 家庭Mapper接口
 */
@Mapper
public interface FamilyMapper extends BaseMapper<Family> {
}
