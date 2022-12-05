package util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Field;
import model.Monster;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MonsterRespawnScheduler {

    private static final int MAX_MONSTER_COUNT = 100;
    public static class MonsterRespawnJob implements Job {
        @Override
        public synchronized void execute(JobExecutionContext jobExecutionContext) {
            Field field = Field.getInstance();
            int curMonsterCount = field.countMonsters();
            if (curMonsterCount < MAX_MONSTER_COUNT) {
                Monster monster = field.generateMonster();
                log.info("몬스터 자동 생성 {x:" + monster.x + ", y:" + monster.y + "}" + " 현재 몬스터 수 : " + (curMonsterCount + 1));
            }
        }
    }

    public static void start() {
        JobDetail jobDetail = JobBuilder.newJob(MonsterRespawnJob.class)
                .withIdentity("MonsterRespawnJob","group1")
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("MonsterRespawnTrigger", "group1")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(1)
                        .repeatForever())
                .build();

        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();
        } catch (Exception e) {
            log.warn("몬스터 리스폰 스케쥴러 시작 에러 : {}", e.getMessage());
        }

    }
}
