package com.czh.mianshiha.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;

@Data
@TableName("user_sign_in_record")
public class UserSignInRecord {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    @TableField("user_id")
    private Long userId;
    private Integer year;
    @TableField("sign_days")
    private String signDays;
    @TableField("create_time")
    private Date createTime;
    private String userAccount;
}