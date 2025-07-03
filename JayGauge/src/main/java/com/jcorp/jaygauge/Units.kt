package com.jcorp.jaygauge

enum class Units(val value: String, val valuePattern: String, val id:Int) {
    TEMPERATURE_C("°C", "00.0",0),
    TEMPERATURE_F("°F", "00.0",1),
    KPH("KPH", "000",2),
    KPH_KM_H("km/h", "000",3),
    MPH_MI_H("mi/h", "000",4),
    MPH("MPH", "000",5),
    GHZ("GHz", "0.00",6),
    MHZ("MHz", "0000",7),
    HZ("Hz", "00000",8),
    PERCENTAGE("%", "000",9),
    GB("GB", "00.0",10),
    MB("MB", "000",11),
    NONE("", "",12)
}