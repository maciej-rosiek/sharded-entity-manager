package pl.jpa.entity;

import org.testng.Assert;
import org.testng.annotations.Test;

public class UsernameShardingStrategyTest {

    @Test
    public void test() {
        UsernameShardingStrategy shardingStrategy = new UsernameShardingStrategy();
        Assert.assertEquals(1, shardingStrategy.getShardIndex("tes"));
        Assert.assertEquals(0, shardingStrategy.getShardIndex("test"));
        Assert.assertEquals(1, shardingStrategy.getShardIndex("test1"));
    }

}
