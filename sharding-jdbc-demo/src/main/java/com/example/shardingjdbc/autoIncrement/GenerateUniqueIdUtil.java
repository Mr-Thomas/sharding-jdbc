package com.example.shardingjdbc.autoIncrement;

public class GenerateUniqueIdUtil {
    /**
     * 20位末尾的数字id
     */
    private static volatile int Guid = 100000;

    /**
     * <获取唯一id>
     *
     * @return 结果
     * @throws
     */
    public static synchronized Long getGuid() {
        GenerateUniqueIdUtil.Guid += 1;
        if (GenerateUniqueIdUtil.Guid > 999999) {
            GenerateUniqueIdUtil.Guid = 100000;
        }
        int ran = GenerateUniqueIdUtil.Guid;
        return Long.valueOf(String.valueOf(System.currentTimeMillis()) + ran);
    }
}