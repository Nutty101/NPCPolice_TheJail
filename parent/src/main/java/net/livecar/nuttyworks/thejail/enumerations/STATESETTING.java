package net.livecar.nuttyworks.thejail.enumerations;

public enum STATESETTING {
    TRUE, FALSE, NOTSET {
        @Override
        public STATESETTING next() {
            return null;
        }
    };

    public static boolean contains(String value) {
        for (STATESETTING ename : STATESETTING.values()) {
            if (ename.toString().equalsIgnoreCase(value))
                return true;
        }
        return false;
    }

    public STATESETTING next() {
        return values()[ordinal() + 1];
    }
}