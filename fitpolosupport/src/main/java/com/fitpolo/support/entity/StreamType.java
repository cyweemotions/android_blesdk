package com.fitpolo.support.entity;

public enum StreamType {

            xon_frame_type_ack("xon_frame_type_ack","每包都需要回应ACK ", 0),
            xon_frame_type_mtu("xon_frame_type_mtu","每个包的最大长度 ", 1),
            xon_frame_type_once("xon_frame_type_once","单包数据 ", 2),
            xon_frame_type_multi_info("xon_frame_type_multi_info","多包信息 ", 3),
            xon_frame_type_multi_crc("xon_frame_type_multi_crc","多包总数居的CRC ", 4),
            xon_frame_type_multi_ack("xon_frame_type_multi_ack","多包每一包的ACK ", 5),
            xon_frame_type_multi("xon_frame_type_multi","多包数据 ", 6),
            ;
    public String typeName;
    public int typeValue;
    public String typeDesc;
    StreamType(String xonFrameTypeMtu, String s, int i) {
        this.typeDesc = s;
        this.typeName = xonFrameTypeMtu;
        this.typeValue = i;
    }
}
