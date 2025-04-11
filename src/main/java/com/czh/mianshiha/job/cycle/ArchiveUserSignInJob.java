package com.czh.mianshiha.job.cycle;

import com.czh.mianshiha.constant.RedisConstant;
import com.czh.mianshiha.mapper.UserSignInRecordMapper;
import com.czh.mianshiha.model.entity.UserSignInRecord;
import com.czh.mianshiha.service.UserService;
import org.redisson.api.RBitSet;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.BitSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户签到记录归档任务
 */
@Component
@Slf4j
@Async
public class ArchiveUserSignInJob {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private UserSignInRecordMapper userSignInRecordMapper;

    @Resource
    private UserService userService;

    /**
     * 每年1月1日5点执行
     */
    @Scheduled(cron = "0 0 5 1 1 ?")
    public void archiveLastYearSignInRecords() {
        int targetYear = LocalDate.now().getYear();
        String pattern = RedisConstant.USER_SIGN_IN_REDIS_KEY_PREFIX + ":" + targetYear + ":*";

        // 获取所有匹配的Key
        RKeys keys = redissonClient.getKeys();
        Set<String> keysToProcess = keys.getKeysStreamByPattern(pattern).collect(Collectors.toSet());
        if (keysToProcess.isEmpty()) {
            log.info("No sign in records to archive for year {}", targetYear);
            return;
        }

        keysToProcess.forEach(key -> {
            try {
                // 解析用户ID
                String[] parts = key.split(":");
                long userId = Long.parseLong(parts[parts.length - 1]);
                
                // 处理位图数据
                RBitSet bitSet = redissonClient.getBitSet(key);
                BitSet javaBitSet = bitSet.asBitSet();

                // 转换为签到日期列表
                String signDays = convertBitSetToDays(javaBitSet, targetYear);

                // 持久化到数据库
                UserSignInRecord record = new UserSignInRecord();
                record.setUserId(userId);
                record.setYear(targetYear);
                record.setUserAccount(userService.getById(userId).getUserAccount());
                record.setSignDays(signDays);
                userSignInRecordMapper.insert(record);

                // 删除Redis Key（保留30天过渡期）
                bitSet.expire(30, TimeUnit.DAYS);
                log.info("Archived sign in records for user: {}, year: {}", userId, targetYear);
            } catch (Exception e) {
                log.error("Error processing key: {}", key, e);
            }
        });
    }

    /**
     * 将位集合转换为逗号分隔的日期字符串
     */
    private String convertBitSetToDays(BitSet bitSet, int year) {
        StringBuilder sb = new StringBuilder();
        int index = bitSet.nextSetBit(0);
        while (index >= 0) {
            sb.append(index + 1).append(","); // 转换为1-based day
            index = bitSet.nextSetBit(index + 1);
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
    }
}