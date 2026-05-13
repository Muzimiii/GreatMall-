local stock = redis.call('GET', KEYS[1])
if not stock then
    return -1
end

redis.call('INCR', KEYS[1])
redis.call('SREM', KEYS[2], ARGV[1])
return 1
