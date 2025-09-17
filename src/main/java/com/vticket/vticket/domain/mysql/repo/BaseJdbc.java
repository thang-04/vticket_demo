package com.vticket.vticket.domain.mysql.repo;

import com.vticket.vticket.utils.CommonUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class BaseJdbc {

    private static final Logger LOGGER = Logger.getLogger(BaseJdbc.class);
    
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public <T> List<T> findAll(Class<T> className) {
        try {
            String arr[] = className.getName().replaceAll("BO", "").split("\\.");
            String tableNameBO = arr[arr.length - 1];
            String tableName = convertObjectNameToMysqlName(tableNameBO);
            String sql = "SELECT * FROM " + tableName;
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper(className));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    
    public <T> List<T> findByProperties(Class<T> className, Object... pairs) {
        try {
            String arr[] = className.getName().replaceAll("BO", "").split("\\.");
            String tableNameBO = arr[arr.length - 1];
            String tableName = "";
            for (int i = 0; i < tableNameBO.length(); i++) {
                char c = tableNameBO.charAt(i);
                if (Character.isUpperCase(c) && i > 0) {
                    tableName += "_" + c;
                } else {
                    tableName += c;
                }
            }

            String sql = "SELECT * FROM " + tableName.toLowerCase() + " WHERE 1 = 1";
            Map<String, Object> mapParam = new HashMap<>();
            String orderNumber = "";
            if (pairs != null) {
                int index = 0;
                String tempFieldName = "";
                for (Object obj : pairs) {
                    if (index % 2 == 0) {
                        String fieldName = (String) obj;
                        if (fieldName.contains("order_number")) {
                            orderNumber = fieldName;
                        } else {
                            sql += " AND " + fieldName + " = :" + fieldName;
                            tempFieldName = fieldName;
                        }
                    } else {
                        mapParam.put(tempFieldName, obj);
                    }
                    index++;
                }
            }
            if (CommonUtils.isNotEmpty(orderNumber)) {
                sql = sql + " ORDER BY " + orderNumber;
            }
            return jdbcTemplate.query(sql, mapParam, new BeanPropertyRowMapper(className));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public <T> List<T> findByProperties(String schema, Class<T> className, Object... pairs) {
        try {
            String arr[] = className.getName().replaceAll("BO", "").split("\\.");
            String tableNameBO = arr[arr.length - 1];
            String tableName = "";
            for (int i = 0; i < tableNameBO.length(); i++) {
                char c = tableNameBO.charAt(i);
                if (Character.isUpperCase(c) && i > 0) {
                    tableName += "_" + c;
                } else {
                    tableName += c;
                }
            }

            String sql = "SELECT * FROM " + schema + "." + tableName.toLowerCase() + " WHERE 1 = 1";
            Map<String, Object> mapParam = new HashMap<>();
            String orderNumber = "";
            if (pairs != null) {
                int index = 0;
                String tempFieldName = "";
                for (Object obj : pairs) {
                    if (index % 2 == 0) {
                        String fieldName = (String) obj;
                        if (fieldName.contains("order_number")) {
                            orderNumber = fieldName;
                        } else {
                            sql += " AND " + fieldName + " = :" + fieldName;
                            tempFieldName = fieldName;
                        }
                    } else {
                        mapParam.put(tempFieldName, obj);
                    }
                    index++;
                }
            }
            if (CommonUtils.isNotEmpty(orderNumber)) {
                sql = sql + " ORDER BY " + orderNumber;
            }
            return jdbcTemplate.query(sql, mapParam, new BeanPropertyRowMapper(className));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public <T> List<T> findByProperty(Class<T> className, String condition, Object conditionValue, String orderBy) {
        try {
            String arr[] = className.getName().replaceAll("BO", "").split("\\.");
            String tableNameBO = arr[arr.length - 1];
            String tableName = "";
            for (int i = 0; i < tableNameBO.length(); i++) {
                char c = tableNameBO.charAt(i);
                if (Character.isUpperCase(c) && i > 0) {
                    tableName += "_" + c;
                } else {
                    tableName += c;
                }
            }
            String sql = "SELECT * FROM " + tableName.toLowerCase() + " WHERE " + condition + " = :condition";
            if (CommonUtils.isNotEmpty(orderBy)) {
                sql += " ORDER BY " + orderBy;
            }
            Map<String, Object> mapParam = new HashMap<>();
            mapParam.put("condition", conditionValue);
            return jdbcTemplate.query(sql, mapParam, new BeanPropertyRowMapper(className));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public <T> List<T> findByProperty(Class<T> className, String condition, Object conditionValue, String orderBy, String schema) {
        try {
            String arr[] = className.getName().replaceAll("BO", "").split("\\.");
            String tableNameBO = arr[arr.length - 1];
            String tableName = "";
            for (int i = 0; i < tableNameBO.length(); i++) {
                char c = tableNameBO.charAt(i);
                if (Character.isUpperCase(c) && i > 0) {
                    tableName += "_" + c;
                } else {
                    tableName += c;
                }
            }
            String sql = "SELECT * FROM " + schema + "." + tableName.toLowerCase() + " WHERE " + condition + " = :condition";
            if (CommonUtils.isNotEmpty(orderBy)) {
                sql += " ORDER BY " + orderBy;
            }
            Map<String, Object> mapParam = new HashMap<>();
            mapParam.put("condition", conditionValue);
            return jdbcTemplate.query(sql, mapParam, new BeanPropertyRowMapper(className));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public String findProperty(String tableName, String fieldName, String condition, String conditionValue) {
        try {
            String sql = "SELECT " + fieldName + " FROM " + tableName + " WHERE " + condition + " = :conditionValue";
            Map<String, Object> mapParam = new HashMap<>();
            mapParam.put("conditionValue", conditionValue);
            return jdbcTemplate.queryForObject(sql, mapParam, String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public <T> T get(Class<T> className, Object columnValue) {
        try {
            String arr[] = className.getName().replaceAll("BO", "").split("\\.");
            String tableNameBO = arr[arr.length - 1];
            String tableName = "";
            for (int i = 0; i < tableNameBO.length(); i++) {
                char c = tableNameBO.charAt(i);
                if (Character.isUpperCase(c) && i > 0) {
                    tableName += "_" + c;
                } else {
                    tableName += c;
                }
            }

            String sql = "SELECT * FROM " + tableName.toLowerCase() + " WHERE id = :columnValue";
            Map<String, Object> mapParam = new HashMap<>();
            mapParam.put("columnValue", columnValue);
            return (T) jdbcTemplate.queryForObject(sql, mapParam, new BeanPropertyRowMapper(className));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public <T> T get(Class<T> className, String columnName, Object columnValue) {
        try {
            String arr[] = className.getName().replaceAll("BO", "").split("\\.");
            String tableNameBO = arr[arr.length - 1];
            String tableName = "";
            for (int i = 0; i < tableNameBO.length(); i++) {
                char c = tableNameBO.charAt(i);
                if (Character.isUpperCase(c) && i > 0) {
                    tableName += "_" + c;
                } else {
                    tableName += c;
                }
            }
            String sql = "SELECT * FROM " + tableName.toLowerCase() + " WHERE " + columnName + " = :columnValue";
            Map<String, Object> mapParam = new HashMap<>();
            mapParam.put("columnValue", columnValue);
            return (T) jdbcTemplate.queryForObject(sql, mapParam, new BeanPropertyRowMapper(className));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public <T> T get(Class<T> className, Object columnValue, String schema) {
        try {
            String arr[] = className.getName().replaceAll("BO", "").split("\\.");
            String tableNameBO = arr[arr.length - 1];
            String tableName = "";
            for (int i = 0; i < tableNameBO.length(); i++) {
                char c = tableNameBO.charAt(i);
                if (Character.isUpperCase(c) && i > 0) {
                    tableName += "_" + c;
                } else {
                    tableName += c;
                }
            }

            String sql = "SELECT * FROM " + schema + "." + tableName.toLowerCase() + " WHERE id = :columnValue";
            Map<String, Object> mapParam = new HashMap<>();
            mapParam.put("columnValue", columnValue);
            return (T) jdbcTemplate.queryForObject(sql, mapParam, new BeanPropertyRowMapper(className));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Long saveOrUpdate(Class className, Object object) throws IllegalArgumentException, IllegalAccessException {
        String arr[] = className.getName().replaceAll("BO", "").split("\\.");
        String tableNameBO = arr[arr.length - 1];
        String tableName = convertObjectNameToMysqlName(tableNameBO);
        Field[] fields = className.getDeclaredFields();
        Long id = null;
        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(object);
            if (value != null && "id".equals(field.getName())) {
                id = Long.valueOf(value.toString());
            }
        }
        if (id == null) {// neu la insert
            StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + "(");
            StringBuilder condtion = new StringBuilder(" VALUES(");
            Map<String, Object> mapParam = new HashMap<>();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(object);
                if (value != null) {
                    sql.append(convertObjectNameToMysqlName(field.getName())).append(",");
                    condtion.append(":").append(field.getName()).append(",");
                    mapParam.put(field.getName(), value);
                }
            }
            String sqlSelect = sql.toString();
            sqlSelect = sqlSelect.substring(0, sqlSelect.length() - 1) + ")";
            String values = condtion.toString();
            values = values.substring(0, values.length() - 1) + ")";

            SqlParameterSource sqlParameterSource = new MapSqlParameterSource(mapParam);
            GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(sqlSelect + values, sqlParameterSource, generatedKeyHolder);
            return generatedKeyHolder.getKey().longValue();
//            return jdbcTemplate.update(sqlSelect + values, mapParam);

        } else {
            StringBuilder sql = new StringBuilder("UPDATE " + tableName + " SET ");
            Map<String, Object> mapParam = new HashMap<>();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(object);
                sql.append(convertObjectNameToMysqlName(field.getName())).append(" = :").append(field.getName()).append(",");
                mapParam.put(field.getName(), value);
            }
            String sqlUpdate = sql.toString();
            sqlUpdate = sqlUpdate.substring(0, sqlUpdate.length() - 1);
            sqlUpdate += " WHERE id = :id";
            mapParam.put("id", id);
            jdbcTemplate.update(sqlUpdate, mapParam);
            return id;
        }
    }

    public int saveBatch(Class className, List listObject) throws IllegalArgumentException, IllegalAccessException {
        try {
            if (listObject == null || listObject.isEmpty()) {
                return 0;
            }
            String arr[] = className.getName().replaceAll("BO", "").split("\\.");
            String tableNameBO = arr[arr.length - 1];
            String tableName = convertObjectNameToMysqlName(tableNameBO);
            Field[] fields = className.getDeclaredFields();

            StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + "(");
            StringBuilder condtion = new StringBuilder(" VALUES(");
            // nhuoc diem k insert duoc null =>cau truc data phai giong nhau
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(listObject.get(0));
                if (value != null) {
                    sql.append(convertObjectNameToMysqlName(field.getName())).append(",");
                    condtion.append(":").append(field.getName()).append(",");
                }
            }
            String sqlInsert = sql.toString();
            sqlInsert = sqlInsert.substring(0, sqlInsert.length() - 1) + ")";
            String values = condtion.toString();
            values = values.substring(0, values.length() - 1) + ")";

            List<List<Object>> listPartion = CommonUtils.partition(listObject, 999);
            int successRecord = 0;
            for (List<Object> list : listPartion) {
                Map<String, Object>[] batchOfInputs = new HashMap[list.size()];
                int count = 0;
                for (Object object : list) {
                    Map<String, Object> mapParam = new HashMap<>();
                    for (Field field : fields) {
                        field.setAccessible(true);
                        Object value = field.get(object);
                        mapParam.put(field.getName(), value);
                    }
                    batchOfInputs[count++] = mapParam;
                }
                int[] result = jdbcTemplate.batchUpdate(sqlInsert + values, batchOfInputs);
                for (int i = 0; i < result.length; i++) {
                    int record = result[i];
                    if (record == 1) {
                        successRecord++;
                    } else {
                        LOGGER.info("INSERT FAIL|sqlInsert=" + sqlInsert + values + "|value=" + CommonUtils.toJson(batchOfInputs[i]));
                    }
                }
            }

            return successRecord;
        } catch (Exception ex) {
            LOGGER.error("ERROR=" + ex.getMessage(), ex);
        }
        return 0;

    }

    private String convertObjectNameToMysqlName(String name) {
        String result = "";
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                result += "_" + c;
            } else {
                result += c;
            }
        }
        return result.toLowerCase();
    }
}
