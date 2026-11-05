import java.util.Scanner;

public class Menu {
    private final Scanner scanner = new Scanner(System.in);
    private final DecisionSystem system = new DecisionSystem(scanner);

    public void showMenu() {
        while (true) {
            System.out.println("\n==============================");
            System.out.println("       SMART DECISION");
            System.out.println("==============================");
            System.out.println("1. Начать подбор развлечения");
            System.out.println("2. Информация о программе");
            System.out.println("3. Выход");
            System.out.println("4. Проверить статус пользователя");
            System.out.println("5. Проверить возраст");
            System.out.println("6. Проверить свободное время");
            System.out.println("7. Проверить активность");
            System.out.println("8. Рассчитать скидку");
            System.out.println("9. Финансовый анализ");
            System.out.println("10. Проверить баллы");
            System.out.println("11. Проверка безопасности");
            System.out.println("12. Получить рекомендацию");
            System.out.println("13. Рассчитать стоимость услуги");
            System.out.println("14. Комплексное решение");
            System.out.println("15. Анализ пользователя");
            System.out.println("16. Авторизация");
            System.out.println("17. Проверка развлечения");
            System.out.println("==============================");
            System.out.print("Выберите действие: ");
            if (!scanner.hasNextInt()) {
                System.out.println("Ошибка! Введите число.");
                scanner.next();
                continue;
            }

            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    system.chooseEntertainment();
                    break;
                case 2:
                    System.out.println("SmartDecision помогает выбрать занятие в свободное время.");
                    break;
                case 3:
                    System.out.println("Программа завершена!");
                    return;
                case 4:
                    System.out.print("Введите статус (1-3): ");
                    system.checkStatus(scanner.nextInt());
                    break;
                case 5:
                    System.out.print("Введите возраст: ");
                    system.checkAge(scanner.nextInt());
                    break;
                case 6:
                    System.out.print("Сколько свободных часов? ");
                    int freeHours = scanner.nextInt();
                    System.out.print("Настроение (1 - хорошее, 2 - усталое, 3 - скучное): ");
                    system.checkFreeTime(freeHours, scanner.nextInt());
                    break;
                case 7:
                    System.out.print("Сколько свободных часов? ");
                    int activityHours = scanner.nextInt();
                    System.out.print("Настроение (1 - хорошее, 2 - усталое, 3 - скучное): ");
                    system.checkActivity(activityHours, scanner.nextInt());
                    break;
                case 8:
                    System.out.print("Введите сумму покупки: ");
                    double purchase = scanner.nextDouble();
                    System.out.print("Вы премиум-клиент? (true/false): ");
                    system.calculateDiscount(purchase, scanner.nextBoolean());
                    break;
                case 9:
                    System.out.print("Введите доходы: ");
                    double income = scanner.nextDouble();
                    System.out.print("Введите расходы: ");
                    system.checkFinance(income, scanner.nextDouble());
                    break;
                case 10:
                    System.out.print("Введите количество баллов (0-100): ");
                    system.checkScore(scanner.nextInt());
                    break;
                case 11:
                    System.out.print("Введите возраст: ");
                    int age = scanner.nextInt();
                    System.out.print("Сколько часов свободного времени? ");
                    int hours = scanner.nextInt();
                    System.out.print("Пользователь заблокирован? (true/false): ");
                    system.checkSafety(age, hours, scanner.nextBoolean());
                    break;
                case 12:
                    System.out.println("1. Хорошее настроение");
                    System.out.println("2. Усталость");
                    System.out.println("3. Скука");
                    System.out.print("Выберите настроение: ");
                    int mood = scanner.nextInt();
                    System.out.print("Введите бюджет: ");
                    system.giveRecommendation(mood, scanner.nextInt());
                    break;
                case 13:
                    System.out.println("1. Обычный тариф");
                    System.out.println("2. Стандартный тариф");
                    System.out.println("3. Премиум тариф");
                    System.out.print("Выберите тариф: ");
                    int tariff = scanner.nextInt();
                    System.out.print("Дополнительная услуга? (1 - да, 0 - нет): ");
                    system.calculateService(tariff, scanner.nextInt());
                    break;
                case 14:
                    System.out.print("Введите возраст: ");
                    int decisionAge = scanner.nextInt();
                    System.out.print("Свободные часы: ");
                    int decisionHours = scanner.nextInt();
                    System.out.print("Настроение (1-3): ");
                    int decisionMood = scanner.nextInt();
                    System.out.print("Бюджет: ");
                    system.makeDecision(decisionAge, decisionHours, decisionMood, scanner.nextInt());
                    break;
                case 15:
                    System.out.print("Введите возраст: ");
                    int userAge = scanner.nextInt();
                    System.out.print("Свободные часы: ");
                    int userHours = scanner.nextInt();
                    System.out.print("Настроение (1 - хорошее, 2 - усталое, 3 - скука): ");
                    int userMood = scanner.nextInt();
                    System.out.print("Ваш бюджет: ");
                    int userBudget = scanner.nextInt();
                    System.out.print("Уровень энергии (1-10): ");
                    system.analyzeUser(userAge, userHours, userMood, userBudget, scanner.nextInt());
                    break;
                case 16:
                    system.login();
                    break;
                case 17:
                    System.out.print("Введите возраст: ");
                    int nestedAge = scanner.nextInt();

                    System.out.print("Свободные часы: ");
                    int nestedHours = scanner.nextInt();

                    System.out.print("Настроение (1-хорошее, 2-усталое, 3-скучное): ");
                    int nestedMood = scanner.nextInt();

                    system.nestedCheck(nestedAge, nestedHours, nestedMood);
                    break;
                default:
                    System.out.println("Ошибка! Такого пункта нет.");
                    System.out.println("Попробуйте ещё раз.");
            }
        }
    }

    private void entertainmentMenu() {
        System.out.println("\n=== РАЗДЕЛ РАЗВЛЕЧЕНИЙ ===");
        System.out.println("1. Игры");
        System.out.println("2. Фильмы");
        System.out.println("3. Рисование");
        System.out.println("4. Музыка");
        System.out.println("5. Прогулка");
        System.out.print("Выберите категорию: ");

        switch (scanner.nextInt()) {
            case 1:
                System.out.println("Вы выбрали компьютерные игры!");
                break;
            case 2:
                System.out.println("Вы выбрали просмотр фильмов!");
                break;
            case 3:
                System.out.println("Вы выбрали рисование!");
                break;
            case 4:
                System.out.println("Вы выбрали музыку!");
                break;
            case 5:
                System.out.println("Вы выбрали прогулку!");
                break;
            default:
                System.out.println("Такой категории нет!");
        }
    }
}
