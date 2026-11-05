public class Validation {

    public boolean checkNumber(int number) {
        if (number <= 0) {
            System.out.println("Ошибка! Число должно быть больше нуля.");
            return false;
        }

        return true;
    }

    public boolean checkName(String name) {
        if (name.isEmpty()) {
            System.out.println("Ошибка! Имя не может быть пустым.");
            return false;
        }

        return true;
    }
}