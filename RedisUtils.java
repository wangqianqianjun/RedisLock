@Component
public class RedisUtils<T> {

    /**
     * lua 脚本 加锁
     */
    private static final String lockScript = "local key     = KEYS[1]\n" +
            "local ttl     = KEYS[2]\n" +
            "local content = KEYS[3]\n" +
            "\n" +
            "local lockSet = redis.call('setnx', key, content)\n" +
            "\n" +
            "if lockSet == 1 then\n" +
            "    return  redis.call('pexpire', key, ttl)\n" +
            "end\n" +
            "    return nil";
    /**
     * lua 脚本 释放锁
     */
    private static final String unlockScript = "local key  = KEYS[1]\n" +
            "local signature   = KEYS[2]\n" +
            "if redis.call('get',key) == signature then\n" +
            "    redis.call('del',key)\n" +
            "    return true\n" +
            "else\n" +
            "    return nil\n" +
            "end";
    @Autowired
    private RedisTemplate redisTemplate;
    private TimeUnit timeUnit = TimeUnit.SECONDS;

    /**
     * 锁
     *
     * @param key
     * @param ttl
     * @return
     */
    public Object lock(String key, long ttl, String signature) {
        return redisTemplate.execute(connection -> ((Jedis) connection.getNativeConnection()).eval(lockScript, 3, key, ttl + "", signature)
                , false);
    }

    /**
     * 释放锁
     *
     * @param key
     * @return
     */
    public Object unlock(String key, String signature) {
        return redisTemplate.execute(connection -> ((Jedis) connection.getNativeConnection()).eval(unlockScript, 2, key, signature)
                , false);
    }

}
