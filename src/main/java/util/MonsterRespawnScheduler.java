package util;

import model.Field;
import model.Monster;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

public class MonsterRespawnScheduler {

    private static final int MAX_MONSTER_COUNT = 10;
    public static class MonsterRespawnJob implements Job {
        @Override
        public void execute(JobExecutionContext jobExecutionContext) {
            Field field = Field.getInstance();
            int curMonsterCount = field.countMonsters();
            if (curMonsterCount < MAX_MONSTER_COUNT) {
                Monster monster = field.generateMonster();
                Logger.log("몬스터 자동 생성 {x:" + monster.x + ", y:" + monster.y + "}" + " 현재 몬스터 수 : " + (curMonsterCount + 1));
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
                        .withIntervalInSeconds(10)
                        .repeatForever())
                .build();

        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();
        } catch (Exception e) {
            Logger.log("스케쥴러 시작 실패.");
        }

    }
}
