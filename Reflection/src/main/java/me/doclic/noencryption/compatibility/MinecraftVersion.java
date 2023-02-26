package me.doclic.noencryption.compatibility;

public class MinecraftVersion {
    public final int number0;
    public final int number1;
    public final int number2;
    public final int singleNumber;
    private final String str;

    public MinecraftVersion(int singleNumber) {
        this.number0 = singleNumber / 1_00_00;
        this.number1 = (singleNumber / 1_00) % 1_00;
        this.number2 = singleNumber % 1_00;
        this.singleNumber = singleNumber;
        this.str = toString(number0, number1, number2);
    }

    public MinecraftVersion(int number0, int number1) { this(number0, number1, 0); }
    public MinecraftVersion(int number0, int number1, int number2) {
        this.number0 = number0;
        this.number1 = number1;
        this.number2 = number2;
        this.singleNumber = toSingleNumber(number0, number1, number2);
        this.str = toString(number0, number1, number2);
    }

    private static int toSingleNumber(int number0, int number1, int number2) {
        return number0 * 1_00_00 + (number1 % 100) * 1_00 + (number2 % 100);
    }

    private static String toString(int number0, int number1, int number2) {
        var str = number0 + "." + number1;
        if(number2 != 0) str += "." + number2;
        return str;
    }

    @Override
    public String toString() {
        return str;
    }
}
