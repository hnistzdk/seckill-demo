--如果传入的key存在 执行下面的操作  不存在返回-1
if (redis.call('exists', KEYS[1]) == 1) then
    --先获取库存数量
    local stock = tonumber(redis.call('get', KEYS[1]));
    --如果库存大于0 则减库存
    if (stock > 0) then
        redis.call('incrby', KEYS[1], -1);
        return stock;
    end ;
    --没库存 返回-1
    return -1;
end ;
return -1;
