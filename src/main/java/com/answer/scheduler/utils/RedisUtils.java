package com.answer.scheduler.utils;

import com.alibaba.fastjson.JSONObject;
import com.answer.scheduler.config.SpringContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * redis utils
 */
public class RedisUtils {
    private static Logger LOGGER = LoggerFactory.getLogger(RedisUtils.class);

    /**
     * 指定缓存失效时间
     *
     * @param key
     * @param time
     * @return
     */
    public static boolean expire(String key, Long time) {
        try {
            if (time > 0) {
                getRedisTemplate().expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e1) {
            e1.printStackTrace();
            return false;
        }
    }

    /**
     * 获取key的过期时间
     *
     * @param key
     * @return
     */
    public static long getExpire(String key) {
        return getRedisTemplate().getExpire(key);
    }

    /**
     * 判断key是否存在
     *
     * @param key
     * @return
     */
    public static boolean hashKey(String key) {
        try {
            return getRedisTemplate().hasKey(key);
        } catch (Exception e2) {
            e2.printStackTrace();
            return false;
        }
    }

    /**
     * 判断key是否存在
     *
     * @param key
     * @return
     */
    public static boolean hasKey(String key) {
        try {
            return getRedisTemplate().hasKey(key);
        } catch (Exception e2) {
            e2.printStackTrace();
            return false;
        }
    }

    /**
     * 删除缓存
     *
     * @param key
     */
    public static void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                getRedisTemplate().delete(key[0]);
            } else {
                getRedisTemplate().delete(CollectionUtils.arrayToList(key));
            }
        }
    }
//=====================================String========================================

    /**
     * 普通缓存获取
     *
     * @param key
     * @return
     */
    public static Object get(String key) {
        return key == null ? null : getRedisTemplate().opsForValue().get(key);
    }

    /**
     * 普通缓存获取(String)
     *
     * @param key
     * @return
     */
    public static String getStringVal(String key) {
        if (key == null) {
            return null;
        }
        Object objVal = getRedisTemplate().opsForValue().get(key);
        return objVal == null ? null : objVal.toString();
    }

    /**
     * 普通放入缓存
     *
     * @param key
     * @param value
     * @return
     */
    public static boolean set(String key, Object value) {
        try {
            getRedisTemplate().opsForValue().set(key, value);
            return true;
        } catch (Exception e3) {
            e3.printStackTrace();
            return false;
        }
    }

    /**
     * 普通放入缓存并设置时效
     *
     * @param key
     * @param value
     * @param time  time > 0 设置时效, time < 0 设置无限期
     * @return
     */
    public static boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                getRedisTemplate().opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                getRedisTemplate().opsForValue().set(key, value);
            }
            return true;
        } catch (Exception e4) {
            e4.printStackTrace();
            return false;
        }
    }

    /**
     * 递增
     *
     * @param key
     * @param data
     * @return
     */
    public static long incr(String key, long data) {
        if (data < 0) {
            throw new RuntimeException("递增因子 不能小于0");
        }
        return getRedisTemplate().opsForValue().increment(key, data);
    }

    public static long decr(String key, long data) {
        if (data < 0) {
            throw new RuntimeException("递减因子,不能小于0");
        }
        return getRedisTemplate().opsForValue().increment(key, -data);
    }
    //===================================Hash==================================

    /**
     * hashKey
     *
     * @param key
     * @param keyItem
     * @return
     */
    public static Object getHash(String key, String keyItem) {
        return getRedisTemplate().opsForHash().get(key, keyItem);
    }

    /**
     * 获取hashkey所有的键值
     *
     * @param key
     * @return
     */
    public static Map<Object, Object> hmget(String key) {
        return getRedisTemplate().opsForHash().entries(key);
    }

    /**
     * hashSet
     *
     * @param key
     * @param obj
     * @return
     */
    public static boolean hsset(String key, Map<? extends Object, ? extends Object> obj) {
        try {
            getRedisTemplate().opsForHash().putAll(key, obj);
            return true;
        } catch (Exception e4) {
            e4.printStackTrace();
            return false;
        }
    }

    /**
     * hashSet 设置时效
     *
     * @param key
     * @param map
     * @param time
     * @return
     */
    public static boolean hsset(String key, Map<? extends Object, ? extends Object> map, Long time) {
        try {
            getRedisTemplate().opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e5) {
            e5.printStackTrace();
            return false;
        }
    }

    /**
     * 向hash表中插入键值
     *
     * @param key
     * @param itemKey
     * @param value
     * @return
     */
    public static <T> boolean hset(String key, String itemKey, T value) {
        try {
            getRedisTemplate().opsForHash().put(key, itemKey, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向hash表中插入键值,设置时效
     *
     * @param key
     * @param itemKey
     * @param value
     * @param time
     * @return
     */
    public static <T> boolean hset(String key, String itemKey, T value, long time) {
        try {
            getRedisTemplate().opsForHash().put(key, itemKey, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除hash表中的项
     *
     * @param key  不能为null
     * @param item 不能为null,可以是多个
     */
    public static void hdel(String key, Object... item) {
        getRedisTemplate().opsForHash().delete(key, item);
    }

    /**
     * 判断hash表中有没有键,项
     *
     * @param key
     * @param item
     * @return
     */
    public static <T> boolean hHashKey(String key, T item) {
        return getRedisTemplate().opsForHash().hasKey(key, item);
    }

    /**
     * 递增,值如果不存在就会创建一个,并把创建的值返回
     *
     * @param key
     * @param item
     * @param by
     * @return
     */
    public static <T> double haIncr(String key, T item, double by) {
        if (by < 0) {
            throw new RuntimeException("递增因子不能小于0");
        }
        return getRedisTemplate().opsForHash().increment(key, item, by);
    }

    /**
     * 递减,值如果不存在就会创建一个,并把创建的值返回
     *
     * @param key
     * @param item
     * @param by
     * @return
     */
    public static <T> double haDenr(String key, T item, double by) {
        return getRedisTemplate().opsForHash().increment(key, item, -by);
    }
//===================================set============================================

    /**
     * 根据key获取set
     *
     * @param key
     * @return
     */
    public static Set<Object> sGet(String key) {
        return getRedisTemplate().opsForSet().members(key);
    }

    /**
     * 从一个set中查询value是否存在
     *
     * @param key
     * @param value
     * @return
     */
    public static <T> boolean sHashKey(String key, T value) {
        return getRedisTemplate().opsForSet().isMember(key, value);
    }

    /**
     * 将set数据放入缓存
     *
     * @param key
     * @param values
     * @return 成功的个数
     */
    public static long sSet(String key, Object... values) {
        try {
            return getRedisTemplate().opsForSet().add(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 批量String添加set数据
     *
     * @param key
     * @param list
     * @return
     */
    public static long batchStringSSet(String key, List<String> list) {
        long num = 0;
        try {
            SetOperations setOperation = getRedisTemplate().opsForSet();
            for (int i = 0; i < list.size(); i++) {
                setOperation.add(key, list.get(i));
                num++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return num;
    }

    /**
     * 批量Integer添加set数据
     *
     * @param key
     * @param list
     * @return
     */
    public static long batchIntegerSSet(String key, List<Integer> list) {
        long num = 0;
        try {
            SetOperations setOperation = getRedisTemplate().opsForSet();
            for (int i = 0; i < list.size(); i++) {
                setOperation.add(key, list.get(i));
                num++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return num;
    }

    /**
     * 批量Long添加set数据
     *
     * @param key
     * @param list
     * @return
     */
    public static long batchLongSSet(String key, List<Long> list) {
        long num = 0;
        try {
            SetOperations setOperation = getRedisTemplate().opsForSet();
            for (int i = 0; i < list.size(); i++) {
                setOperation.add(key, list.get(i));
                num++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return num;
    }

    /**
     * 将set数据放入缓存并给key设置时效
     *
     * @param key    键
     * @param time   key 时效
     * @param values 值,可以是多个
     * @return 成功的个数
     */
    public static long sSet(String key, Long time, Object... values) {
        try {
            Long count = getRedisTemplate().opsForSet().add(key, values);
            if (time > 0) {
                expire(key, time);
            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取set缓存长度
     *
     * @param key
     * @return
     */
    public static long sSize(String key) {
        try {
            return getRedisTemplate().opsForSet().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 移除set缓存中的值
     *
     * @param key    键
     * @param values 值可以是多个
     * @return 移除的个数
     */
    public static long sRemove(String key, Object... values) {
        try {
            return getRedisTemplate().opsForSet().remove(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
//======================================list=========================

    /**
     * 获取list缓存中的值
     *
     * @param key   键
     * @param start 开始
     * @param end   结束  从0到-1显示所有的值
     * @return
     */
    public static List<Object> lGet(String key, long start, long end) {
        try {
            return getRedisTemplate().opsForList().range(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取list缓存长度
     *
     * @param key
     * @return
     */
    public static long lSize(String key) {
        try {
            return getRedisTemplate().opsForList().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 根据index获取list缓存的值
     *
     * @param key   键
     * @param index -1代表表尾,-2代表倒数第二个元素 以此类推
     * @return
     */
    public static Object lIndex(String key, long index) {
        try {
            return getRedisTemplate().opsForList().index(key, index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 把list放入缓存
     *
     * @param key
     * @param value
     * @return
     */
    public static boolean lSet(String key, Object value) {
        try {
            getRedisTemplate().opsForList().leftPush(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 把list放入缓存,设置key的时效
     *
     * @param key
     * @param time
     * @param value
     * @return
     */
    public static boolean lSet(String key, long time, Object value) {
        try {
            getRedisTemplate().opsForList().leftPush(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据索引修改list缓存中具体值
     *
     * @param key
     * @param index
     * @param value
     * @return
     */
    public static boolean lUpdate(String key, Long index, Object value) {
        try {
            getRedisTemplate().opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 移除N个值为value
     *
     * @param key
     * @param count
     * @param value
     * @return
     */
    public static long lRomve(String key, Long count, Object value) {
        try {
            long remove = getRedisTemplate().opsForList().remove(key, count, value);
            return remove;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取redistemplate
     *
     * @return
     */
    public static RedisTemplate<String, Object> getRedisTemplate() {
        return SpringContextUtils.getBean("redisTemplate", RedisTemplate.class);
    }

    /**
     * 执行Lua脚本
     *
     * @param script
     * @param keys
     * @param params
     * @param <T>
     * @return
     */
    public static <T> T execute(RedisScript<T> script, List<String> keys, List<Object> params) {
        try {
            return getRedisTemplate().execute(script, keys, params.toArray());
        } catch (Exception e) {
            LOGGER.error("execute error", e);
            return null;
        }
    }

    /**
     * hash获取所有
     *
     * @param key
     * @return
     */
    public static Map<String, String> hgetall(String key) {
        return getRedisTemplate().execute((RedisCallback<Map<String, String>>) con -> {
            Map<byte[], byte[]> result = con.hGetAll(key.getBytes());
            if (CollectionUtils.isEmpty(result)) {
                return new HashMap<>(0);
            }

            Map<String, String> ans = new HashMap<>(result.size());
            for (Map.Entry<byte[], byte[]> entry : result.entrySet()) {
                ans.put(new String(entry.getKey()), new String(entry.getValue()));
            }
            return ans;
        });
    }

    /**
     * hash根据fields批量获取
     *
     * @param key
     * @param fields
     * @return
     */
    public static Map<String, String> hmget(String key, List<String> fields) {
        List<String> result = getRedisTemplate().<String, String>opsForHash().multiGet(key, fields);
        Map<String, String> ans = new HashMap<>(fields.size());
        int index = 0;
        for (String field : fields) {
            if (result.get(index) == null) {
                continue;
            }
            ans.put(field, result.get(index));
            index++;
        }
        return ans;
    }

    /**
     * hash获取value为list方法
     *
     * @param key
     * @param field
     * @return
     */
    public <T> List<T> hGetList(String key, String field, Class<T> obj) {
        Object value = getRedisTemplate().opsForHash().get(key, field);
        if (value != null) {
            return JSONObject.parseArray(value.toString(), obj);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * hash设置value为list
     *
     * @param key
     * @param field
     * @param values
     * @param <T>
     */
    public <T> void hSetList(String key, String field, List<T> values) {
        String v = JSONObject.toJSONString(values);
        getRedisTemplate().opsForHash().put(key, field, v);
    }


}