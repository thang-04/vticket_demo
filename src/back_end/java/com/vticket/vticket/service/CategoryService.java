package com.vticket.vticket.service;

import com.google.gson.reflect.TypeToken;
import com.vticket.vticket.config.RedisKey;
import com.vticket.vticket.domain.mysql.entity.Category;
import com.vticket.vticket.domain.mysql.repo.CategoryRepo;
import io.micrometer.common.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.vticket.vticket.utils.CommonUtils.gson;

@Service
public class CategoryService {
    private static final Logger logger = LogManager.getLogger(CategoryService.class);

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private CategoryRepo categoryRepo;

    public List<Category> getAllCategories() {
        long start = System.currentTimeMillis();
        List<Category> listCategories = new ArrayList<>();
        try {
            String key = RedisKey.REDIS_LIST_CATEGORY;
//          listCategories = jwtService.
            String resultRedis = (String) redisService.getRedisSsoUser().opsForValue().get(key);
            if (StringUtils.isEmpty(resultRedis)) {
                // get list by SQL
                List<Category> list = categoryRepo.getAllCategories();
                logger.info("Fetched categories from MySQL: {} categories found.", list.size());

                Category cate;
                if (CollectionUtils.isNotEmpty(list)) {
                    for (Category category : list) {
                        cate = new Category();
                        cate.setCategory_id(category.getCategory_id());
                        cate.setName(category.getName());
                        cate.setDescription(category.getDescription());
                        listCategories.add(cate);
                    }
                }
                // cache redis
                if (CollectionUtils.isNotEmpty(listCategories)) {
                    redisService.getRedisSsoUser().opsForValue().set(key, gson.toJson(listCategories));
                    redisService.getRedisSsoUser().expire(key, 1, TimeUnit.HOURS);
                    logger.info("Stored categories in Redis cache.");
                }
            } else {
                listCategories = (List<Category>) gson.fromJson(resultRedis, new TypeToken<List<Category>>() {
                }.getType());
                logger.info("Fetched categories from Redis cache: {} categories found.", listCategories.size());
            }
            logger.info("getAllCategories|Time taken: {} ms", (System.currentTimeMillis() - start));
            return listCategories;
        } catch (Exception ex) {
                logger.error("getAllCategories|Exception|{}" , ex.getMessage(), ex);
        }
        return new ArrayList<>();
    }

    private Category getCategoryById(Long categoryId) {
        long start = System.currentTimeMillis();

        if (categoryId == null || categoryId <= 0) {
            return null;
        }
        String key =String.format( RedisKey.REDIS_CATEGORY_BY_ID , categoryId);
        try {
            String resultRedis = (String) redisService.getRedisSsoUser().opsForValue().get(key);
            if (StringUtils.isEmpty(resultRedis)) {
                // get list by SQL
                Category category = categoryRepo.getCategoryById(categoryId);
                if (category != null) {
                    // cache redis
                    redisService.getRedisSsoUser().opsForValue().set(key, gson.toJson(category));
                    redisService.getRedisSsoUser().expire(key, 1, TimeUnit.HOURS);
                    logger.info("Stored category in Redis cache with and ID: {} ", categoryId);
                }
                logger.info("getCategoryById|Time taken: {} ms", (System.currentTimeMillis() - start));
                return category;
            } else {
                Category category = gson.fromJson(resultRedis, new TypeToken<List<Category>>() {
                }.getType());
                logger.info("getCategories from Redis cache with and ID: {} |Time taken: {} ms ", categoryId, (System.currentTimeMillis() - start));
                return category;
            }
        } catch (Exception ex) {
            logger.error("getCategoryById|Exception|{}" , ex.getMessage(), ex);
        }
        return null;
    }


}


