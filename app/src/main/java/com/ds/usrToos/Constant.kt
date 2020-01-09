package com.ds.usrToos

object Constant {
    const val LOG = 0

    const val LOCK_FALL_STATUS = 13
    const val LOCK_RISE_STATUS = 14
    const val LOCK_MOVE_STATUS = 15
    const val LOCK_FIRST_STATUS = 16

    const val SEND_LOCK_RISE = 3
    const val SEND_LOCK_RISE_FEED_BACK = 4
    const val SEND_LOCK_RISE_SUCCESS = 5

    const val SEND_LOCK_FALL = 6
    const val SEND_LOCK_FALL_FEED_BACK = 7
    const val SEND_LOCK_FALL_SUCCESS = 8

    const val SEND_OPEN_LIGHT = 9
    const val SEND_CLOSE_LIGHT = 10

    const val DISTANCE_INFO = 11

    const val POWER_STATUS = 12

    //串口指令
    val riseBytes = byteArrayOf(0x5A, 0x00, 0x01, 0x01, 0x00, 0x57) //锁臂上升
    val riseFeedbackBytes = byteArrayOf(0x5A, 0x00, 0x10, 0x01, 0x00, 0x57) //地锁上升反馈
    val riseSuccessBytes = byteArrayOf(0x5A, 0x00, 0x10, 0x11, 0x00, 0x57)//地锁上升成功

    val fallBytes = byteArrayOf(0x5A, 0x00, 0x01, 0x02, 0x00, 0x57)   //锁臂下降
    val fallFeedbackBytes = byteArrayOf(0x5A, 0x00, 0x10, 0x02, 0x00, 0x57)//地锁下降反馈
    val fallSuccessBytes = byteArrayOf(0x5A, 0x00, 0x10, 0x22, 0x00, 0x57)//地锁下降成功
    /**地锁查询状态*/
    val queryLockStatus = byteArrayOf(0x5A, 0x00, 0x02, 0x01, 0x00, 0x57)//查询地锁状态指令
    val lockFallStatusBytes = byteArrayOf(0x5A, 0x00, 0x20, 0x00, 0x00, 0x57)//地锁下降状态
    val lockRiseStatusBytes = byteArrayOf(0x5A, 0x00, 0x20, 0x01, 0x00, 0x57)//地锁上升状态
    val lockMoveStatusBytes = byteArrayOf(0x5A, 0x00, 0x20, 0x06, 0x00, 0x57)//地锁运动状态
    val lockFirstStatusBytes = byteArrayOf(0x5A, 0x00, 0x20, 0x08, 0x00, 0x57)//地锁初始状态

    /**电量相关指令*/
    val queryPowerStatus = byteArrayOf(0x5A, 0x00, 0x03, 0x03, 0x00, 0x57)//查询地锁电量指令
    val powerFeedBack = byteArrayOf(0x5A, 0x00, 0x20, 0x00, 0x00, 0x57)//地锁下降状态 4为电压 5为小数点位

    /**补光灯指令*/
    val openLightBytes = byteArrayOf(0x5A)
    val closeLightBytes = byteArrayOf(0x5B)

    /**微波校准*/
    val wireJiaoZhun = byteArrayOf(0x5C)


}