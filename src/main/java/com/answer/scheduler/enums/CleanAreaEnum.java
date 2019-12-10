package com.answer.scheduler.enums;

/**
 * q清扫区域枚举
 *
 * @author zhaodong
 * @date 2019/10/12 16:01
 */
public enum CleanAreaEnum {
    /**
     * 卫生间
     */
    TOILET(1, "卫生间"),
    /**
     * 客厅
     */
    LIVINGROOM(2, "客厅"),

    /**
     * 厨房
     */
    KITCHEN(3, "厨房");

    private int code;

    private String msg;

    CleanAreaEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
