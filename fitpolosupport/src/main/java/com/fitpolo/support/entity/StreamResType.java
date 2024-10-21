package com.fitpolo.support.entity;

public enum StreamResType {
    xon_frame_ack_type_ok("xon_frame_ack_type_ok","数据ok ", 0),
    xon_frame_ack_type_len_err("xon_frame_ack_type_len_err","包长度错误 ", 1),
    xon_frame_ack_type_head_err("xon_frame_ack_type_head_err","包头错误 ", 2),
    xon_frame_ack_type_crc_err("xon_frame_ack_type_crc_err","CRC校验错误 ", 3),
    xon_frame_ack_type_multi_index_err("xon_frame_ack_type_multi_index_err","多包传输丢包和跳包 ", 4),
    xon_frame_ack_type_multi_busy("xon_frame_ack_type_multi_busy","上一次多包传输模式未结束", 5),
    ;

    public String typeName;
    public String descName;
    public int typeValue;

    StreamResType(String xonFrameAckTypeOk, String s, int i) {
        this.descName = s;
        this.typeName = xonFrameAckTypeOk;
        this.typeValue = i;
    }
}
