package pl.jpa.entity;

import pl.jpa.persistence.ShardingStrategyImpl;

public class UsernameShardingStrategy implements ShardingStrategyImpl<String> {

    public static final int SHARDS_COUNT = 2;

    @Override
    public int getShardIndex(String username) {
        return username.length() % SHARDS_COUNT;
    }

}
