package pl.jpa.persistence;

public class DefaultShardingStrategy implements ShardingStrategyImpl {

    @Override
    public int getShardIndex(Object shardKey) {
        return 0;
    }

}
