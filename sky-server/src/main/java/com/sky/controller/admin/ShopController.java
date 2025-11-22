package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
@Api(tags = "店铺相关接口")
public class ShopController {

    public static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    @ApiOperation("修改店铺状态")
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status) {
        log.info("设置店铺状态：{}", status == 1);
        redisTemplate.opsForValue().set(KEY, status);
        return Result.success();
    }

    @ApiOperation("获取店铺状态")
    @GetMapping("/status")
    public Result<Integer> getStatus() {
        log.info("获取店铺状态");
        Integer shopStatus = (Integer) redisTemplate.opsForValue().get(KEY);
        return Result.success(shopStatus);
    }


}
