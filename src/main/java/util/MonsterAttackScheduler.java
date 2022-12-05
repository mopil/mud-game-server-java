package util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Field;
import model.Monster;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.List;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MonsterAttackScheduler {

    public static class MonsterAttackJob implements Job {
        @Override
        public synchronized void execute(JobExecutionContext jobExecutionContext) {
            Field field = Field.getInstance();
            List<Monster> monsters = field.getMonsters();
            if (!monsters.isEmpty()) {
                monsters.forEach(Monster::attack);
                log.info("몬스터 공격 수행");
            }
        }
    }

    public static void start() {
        JobDetail jobDetail = JobBuilder.newJob(MonsterAttackJob.class)
                .withIdentity("MonsterAttackJob","group2")
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("MonsterAttackTrigger", "group2")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(5)
                        .repeatForever())
                .build();

        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();
        } catch (Exception e) {
            log.warn("몬스터 공격 스케쥴러 시작 실패 : {}", e.getMessage());
        }

    }
}
