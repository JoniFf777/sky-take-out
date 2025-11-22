package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    private final DishMapper dishMapper;

    private final DishFlavorMapper dishFlavorMapper;

    private final SetmealDishMapper setmealDishMapper;

    @Transactional
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        // 向菜品表插入1条数据
        dishMapper.insert(dish);

        // 获取插入到菜品表数据的id
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()){
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId);
            }
            // 向菜品口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());


        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());

    }

    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {

        for (Long id : ids){
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE){
                // 菜品状态为启用，则不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }

        }

        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && !setmealIds.isEmpty()){
            // 菜品被套餐引用，则不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

//        for (Long id : ids){
//            dishMapper.deleteById(id);
//            // 删除菜品关联的口味数据
//            dishFlavorMapper.deleteByDishId(id);
//        }
        // sql delete from dish where id in (?,?,?)
        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);

    }

    @Override
    public DishVO getByIdWithFlavor(Long id) {

        // 查询菜品数据
        Dish dish = dishMapper.getById(id);
        // 根据id查询口味数据
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);
        // 组装DishVO并返回
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    @Override
    public List<Dish> list(Long categoryId) {
        log.info("根据分类id查询菜品：{}", categoryId);

        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();

        return dishMapper.list(dish);

    }

    /**
     * 修改菜品基本信息和口味信息
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        // 修改菜品数据
        dishMapper.update(dish);

        // 删除原有的口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        // 重新插入新的口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()){
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishDTO.getId());
            }
            dishFlavorMapper.insertBatch(flavors);
        }




    }
}
