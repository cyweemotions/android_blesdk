package com.fitpolo.support.entity.setEntity;

public class NotifyType {
    public int toggle = 0;// 开关 0-开 1-关
    public int common = 0;// F0 - common
    public int facebook = 0;// F1 - facebook
    public int instagram = 0;// F2 - instagram
    public int kakaotalk = 0;// F3 - kakaotalk
    public int line = 0;// F4 - line
    public int linkedin = 0;// F5 - linkedin
    public int SMS = 0;// F6 - SMS
    public int QQ = 0;// F7 - QQ
    public int twitter = 0;// F8 - twitter
    public int viber = 0;// F9 - viber
    public int vkontaket = 0;// F10 - vkontaket
    public int whatsapp = 0;// F11 - whatsapp
    public int wechat = 0;// F12 - wechat
    public int other1 = 0;// F13 - other1
    public int other2 = 0;// F14 - other2
    public int other3 = 0;// F15 - other3

    @Override
    public String toString() {
        return "NotifyType{" +
            "toggle=" + toggle +
            ", common=" + common +
            ", facebook=" + facebook +
            ", instagram=" + instagram +
            ", kakaotalk=" + kakaotalk +
            ", line=" + line +
            ", linkedin=" + linkedin +
            ", SMS =" + SMS +
            ", QQ =" + QQ +
            ", twitter =" + twitter +
            ", viber =" + viber +
            ", vkontaket =" + vkontaket +
            ", whatsapp =" + whatsapp +
            ", wechat =" + wechat +
            ", other1 =" + other1 +
            ", other2 =" + other2 +
            ", other3 =" + other3 +
            '}';
    }
}
