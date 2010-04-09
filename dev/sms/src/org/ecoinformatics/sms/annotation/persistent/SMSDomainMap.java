package org.ecoinformatics.sms.annotation.persistent;

import org.ecoinformatics.sms.annotation.persistent.auto._SMSDomainMap;

public class SMSDomainMap extends _SMSDomainMap {

    private static SMSDomainMap instance;

    private SMSDomainMap() {}

    public static SMSDomainMap getInstance() {
        if(instance == null) {
            instance = new SMSDomainMap();
        }

        return instance;
    }
}
