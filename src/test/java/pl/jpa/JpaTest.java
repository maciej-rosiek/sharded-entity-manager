package pl.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;
import pl.jpa.entity.User;
import pl.jpa.persistence.ShardedEntityManager;

@ContextConfiguration(locations = {"classpath:context-test.xml"})
public class JpaTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private ShardedEntityManager sem;

    @Test
    public void test() {
        sem.createOrUpdate(new User("john", "John", "Kowalski"));
        User user = sem.find(User.class, "john");
        Assert.assertNotNull(user);
        Assert.assertEquals("John", user.getFirstName());
        Assert.assertEquals("Kowalski", user.getLastName());

        sem.createOrUpdate(new User("john1", "Test", "Testowski"));
        User user2 = sem.find(User.class, "john1");
        Assert.assertNotNull(user2);
        Assert.assertEquals("Test", user2.getFirstName());
        Assert.assertEquals("Testowski", user2.getLastName());
    }

}
