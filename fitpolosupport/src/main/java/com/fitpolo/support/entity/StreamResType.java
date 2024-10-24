package com.fitpolo.support.entity;

public enum StreamResType {
    xon_frame_ack_type_void("xon_frame_ack_type_void","数据ok ", 0),
    xon_frame_ack_type_ok("xon_frame_ack_type_ok","数据ok ", 1),
    xon_frame_ack_type_len_err("xon_frame_ack_type_len_err","包长度错误 ", 2),
    xon_frame_ack_type_head_err("xon_frame_ack_type_head_err","包头错误 ", 3),
    xon_frame_ack_type_crc_err("xon_frame_ack_type_crc_err","CRC校验错误 ", 4),
    xon_frame_ack_type_multi_index_err("xon_frame_ack_type_multi_index_err","多包传输丢包和跳包 ", 5),
    xon_frame_ack_type_multi_busy("xon_frame_ack_type_multi_busy","上一次多包传输模式未结束", 6),
    xon_frame_ack_type_multi_size_err("xon_frame_ack_type_multi_size_err","上一次多包传输模式未结束", 7),
    xon_frame_ack_type_multi_pack_num_err("xon_frame_ack_type_multi_pack_num_err","上一次多包传输模式未结束", 8),
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
