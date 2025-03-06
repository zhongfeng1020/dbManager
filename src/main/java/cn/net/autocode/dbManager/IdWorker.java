package cn.net.autocode.dbManager;

import cn.net.autocode.dbManager.tools.ULID;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * twitter的snowflake算法 -- java实现
 *
 */
public class IdWorker{

    private static IdWorker idWorker;


    /**
     * 起始的时间戳
     */
    private final static long START_STMP = 1480166465631L;

    /**
     * 每一部分占用的位数
     */
    private final static long SEQUENCE_BIT = 12; //序列号占用的位数
    private final static long MACHINE_BIT = 5;   //机器标识占用的位数
    private final static long DATACENTER_BIT = 5;//数据中心占用的位数

    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    /**
     * 每一部分向左的位移
     */
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    private long sequence = 0L; //序列号
    private long lastStamp = -1L;//上一次时间戳

    private IdWorker() {

    }

    /**
     * 产生下一个ID
     */
    private synchronized long next() {
        long currStmp = getNewstmp();
        if (currStmp < lastStamp) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        }

        if (currStmp == lastStamp) {
            //相同毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currStmp = getNextMill();
            }
        } else {
            //不同毫秒内，序列号置为0
            sequence = 0L;
        }

        lastStamp = currStmp;

        //数据中心
        long datacenterId = 0;
        //机器标识
        long machineId = 0;

        return (currStmp - START_STMP) << TIMESTMP_LEFT //时间戳部分
                | datacenterId << DATACENTER_LEFT       //数据中心部分
                | machineId << MACHINE_LEFT             //机器标识部分
                | sequence;                             //序列号部分
    }

    private long getNextMill() {
        long mill = getNewstmp();
        while (mill <= lastStamp) {
            mill = getNewstmp();
        }
        return mill;
    }

    public static long getNewstmp() {
        return System.currentTimeMillis();
    }

    public static long nextId(){
        if(IdWorker.idWorker == null){
            IdWorker.idWorker = new IdWorker();
        }
        return  IdWorker.idWorker.next();
    }

    public static String nextStrId(){
        if(IdWorker.idWorker == null){
            IdWorker.idWorker = new IdWorker();
        }
        return  String.valueOf(IdWorker.idWorker.next());
    }

    /**
     * UUID主键,非互联网项目，建议使用ULID
     */
    public static String uuid(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String ulid(){
        return ULID.random();
    }

    /**
     * 文件时间戳
     */
    public static String fileTimeStamp(){
        // 精确到毫秒
        // 获取指定格式的时间
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return df.format(new Date());
    }
}
