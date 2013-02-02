package pl.jpa.persistence;

public interface ShardingStrategyImpl<ShardKeyType> {

    int getShardIndex(ShardKeyType shardKey);

}
