package database.example;

import database.data.CreatorMapper;
import database.data.MainMapper;
import database.data.MapperFactory;
import database.model.BasicCost;
import database.model.Plan;
import database.model.User;
import database.model.usage.ServiceType;
import org.apache.ibatis.session.SqlSession;

import java.time.Instant;
import java.time.LocalDateTime;

public class InitExample {
    /**
     * 清空表里的所有数据，插入初始数据
     * 初始数据如下：
     * 1. 5种套餐，示例4种+霸王套餐，每月70，100分钟数，200短信，2G本地，2G国内
     * 2. 用户1订阅2, 3, 4套餐，2下月生效，3，4立刻生效；用户2订阅1,5套餐，都立刻生效
     * 3. 用户1打了300秒电话，算7分钟；发了一条短信；用了3G省内流量，1.5G国内流量
     */
    public static void insertInitData() {
        try (SqlSession session = MapperFactory.getSession()) {

            CreatorMapper creatorMapper = session.getMapper(CreatorMapper.class);
            MainMapper mainMapper = session.getMapper(MainMapper.class);

            creatorMapper.dropAllTables();
            creatorMapper.createAllTables();



            Plan plan1 = new Plan("话费套餐", 20, 100, 0, 0, 0);
            Plan plan2 = new Plan("短信套餐", 10, 0, 200, 0, 0);
            Plan plan3 = new Plan("本地流量套餐", 20, 0, 0, 2048, 0);
            Plan plan4 = new Plan("国内流量套餐", 30, 0, 0, 0, 2048);
            Plan plan5 = new Plan("霸王套餐", 70, 100, 200, 2048, 2048);

            creatorMapper.insertPlan(plan1);
            creatorMapper.insertPlan(plan2);
            creatorMapper.insertPlan(plan3);
            creatorMapper.insertPlan(plan4);
            creatorMapper.insertPlan(plan5);

            BasicCost basicCost = new BasicCost(0.5, 0.1, 2, 5);
            creatorMapper.insertBasicCost(basicCost);

            User user1 = new User("测试用户1", basicCost.getId());
            User user2 = new User("测试用户2", basicCost.getId());

            creatorMapper.insertUser(user1);
            creatorMapper.insertUser(user2);


            // 用户1订阅2, 3, 4套餐，2下月生效，3，4立刻生效
            mainMapper.orderPlan(user1.getId(), plan2.getId(), false);
            mainMapper.orderPlan(user1.getId(), plan3.getId(), true);
            mainMapper.orderPlan(user1.getId(), plan4.getId(), true);

            // 用户2订阅1,5套餐，都立刻生效
            mainMapper.orderPlan(user2.getId(), plan1.getId(), true);
            mainMapper.orderPlan(user2.getId(), plan5.getId(), true);


            // 用户1打了300秒电话，算7分钟；发了一条短信；用了3G省内流量，1.5G国内流量
            mainMapper.addUsage(user1.getId(), LocalDateTime.now(), 7, ServiceType.CALL);
            mainMapper.addUsage(user1.getId(), LocalDateTime.now(), 1, ServiceType.SMS);
            mainMapper.addUsage(user1.getId(), LocalDateTime.now(), 3*1024, ServiceType.LOCAL_DATA);
            mainMapper.addUsage(user1.getId(), LocalDateTime.now(), 1.5*1024, ServiceType.DOMESTIC_DATA);


        }
    }
}
